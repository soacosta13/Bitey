
package com.example.bitey.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.bitey.R
import com.example.bitey.data.model.Category
import com.example.bitey.data.model.MealSummary
import com.example.bitey.data.repository.MealRepository
import kotlinx.coroutines.launch
import java.io.IOException
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.map
import com.example.bitey.data.model.Meal
import com.example.bitey.data.model.toMeal
import com.example.bitey.data.model.toSummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


/*ViewModel for main screens*/
class MainViewModel(application: Application) : AndroidViewModel(application)
{
    private val repository = MealRepository(application)

    /*internal mutable state*/
    private val _categories = MutableLiveData<List<Category>>(emptyList())
    private val _selectedCategory = MutableLiveData<String?>(null)

    private val _apiMeals = MutableLiveData<List<MealSummary>>(emptyList())
    private val _favoriteMeals = MutableLiveData<List<Meal>>(emptyList())
    private val _toastMessage = MutableLiveData<String?>(null)

    /*avoids repeated taps on the same fav*/
    private val pendingFavoriteIds = mutableSetOf<String>()
    private val _isLoading = MutableLiveData(false)
    private val _listMessage = MutableLiveData<String?>(null)

    /*search state*/
    private val _isSearchMode = MutableLiveData(false)
    private val _searchQuery = MutableLiveData<String?>(null)

    /*public read only*/
    val categories: LiveData<List<Category>> = _categories
    val selectedCategory: LiveData<String?> = _selectedCategory


    /*meals with their current fav state*/
    val meals: LiveData<List<MealSummary>> =
        MediatorLiveData<List<MealSummary>>().apply {
            addSource(_apiMeals) {
                value = mergeFavoriteState()
            }
            addSource(_favoriteMeals) {
                value = mergeFavoriteState()
            }
        }

    /*recipes saved*/
    val favorites: LiveData<List<MealSummary>> =
        _favoriteMeals.map { savedMeals ->
            savedMeals.map { meal ->
                meal.toSummary().copy(isFavorite = true)
            }
        }

    val toastMessage: LiveData<String?> = _toastMessage


    val isLoading: LiveData<Boolean> = _isLoading
    val listMessage: LiveData<String?> = _listMessage

    val isSearchMode: LiveData<Boolean> = _isSearchMode

    /*prevents older responses from replacing new ones*/
    private var requestId = 0

    /*compose detail state*/
    private val _detailState = MutableStateFlow(MealDetailUiState())
    val detailState: StateFlow<MealDetailUiState> = _detailState

    /*indicates if the current list message represents a retryable error*/
    private val _canRetry = MutableLiveData(false)
    val canRetry: LiveData<Boolean> = _canRetry

    /*prevents old detail requests from replacing new selection*/
    private var detailRequestId = 0


    init {
        loadCategories()
        loadFavorites()
    }

    /*user selects a different category*/
    fun selectCategory(category: String) {
        if (category == _selectedCategory.value) return

        val exists = _categories.value.orEmpty().any {
            it.name == category
        }
        if (!exists) return

        _selectedCategory.value = category
        loadMealsByCategory(category)
    }

    /*retries the operation that could not be completed*/
    fun retry() {
        val query = _searchQuery.value

        if (_isSearchMode.value == true && !query.isNullOrBlank()) {
            searchMeals(query)
            return
        }

        val category = _selectedCategory.value

        if (category == null) {
            loadCategories()
        } else {
            loadMealsByCategory(category)
        }
    }

    /*selects a recipe for the detail screen, if the recipe is saved, is loaded locally before trying the api*/
    fun selectMeal(summary: MealSummary) {
        val currentRequest = ++detailRequestId

        _detailState.value = MealDetailUiState(
            requestedMeal = summary,
            isLoading = true
        )

        viewModelScope.launch {
            try {
                // Prefer SQLite so saved recipes work offline.
                val localMeal = repository.getFavoriteById(summary.id)

                if (currentRequest != detailRequestId) return@launch

                val meal = if (localMeal != null) {
                    localMeal
                } else {
                    // Recipes from the API list require full details.
                    val response = repository.getMealById(summary.id)

                    if (currentRequest != detailRequestId) return@launch

                    if (!response.isSuccessful) {
                        _detailState.value = MealDetailUiState(
                            requestedMeal = summary,
                            error = text(R.string.error_server, response.code())
                        )
                        return@launch
                    }

                    response.body()?.meals?.firstOrNull()?.toMeal()
                }

                if (currentRequest != detailRequestId) return@launch

                if (meal == null) {
                    _detailState.value = MealDetailUiState(
                        requestedMeal = summary,
                        error = text(R.string.detail_not_found)
                    )
                } else {
                    val saved = localMeal != null ||
                            _favoriteMeals.value.orEmpty().any {
                                it.id == meal.id
                            }

                    _detailState.value = MealDetailUiState(
                        requestedMeal = summary,
                        meal = meal,
                        isFavorite = saved
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                if (currentRequest == detailRequestId) {
                    _detailState.value = MealDetailUiState(
                        requestedMeal = summary,
                        error = text(R.string.error_connection)
                    )
                }
            } catch (e: Exception) {
                if (currentRequest == detailRequestId) {
                    _detailState.value = MealDetailUiState(
                        requestedMeal = summary,
                        error = text(R.string.error_unexpected)
                    )
                }
            }
        }
    }

    /*retries loading the selected recipe*/
    fun retryMealDetail() {
        _detailState.value.requestedMeal?.let { summary ->
            selectMeal(summary)
        }
    }

    /*searches by meal name and ingredient, merging results by ID*/
    fun searchMeals(query: String) {
        val searchTerm = query.trim()

        if (searchTerm.isEmpty()) {
            showCategoryMeals()
            return
        }

        _isSearchMode.value = true
        _searchQuery.value = searchTerm

        val currentRequest = ++requestId
        _isLoading.value = true
        _listMessage.value = null
        _canRetry.value = false
        _apiMeals.value = emptyList()

        viewModelScope.launch {
            try {
                // First: search complete recipes by name.
                val nameResponse =
                    repository.searchMealsByName(searchTerm)

                if (currentRequest != requestId) return@launch

                // TheMealDB uses underscores for multi-word ingredients.
                val ingredient = searchTerm.replace(
                    Regex("\\s+"), "_"
                )

                // Second: search summaries by ingredient.
                val ingredientResponse =
                    repository.searchMealsByIngredient(ingredient)

                if (currentRequest != requestId) return@launch

                if (nameResponse.isSuccessful &&
                    ingredientResponse.isSuccessful
                ) {
                    val nameResults = nameResponse.body()
                        ?.meals.orEmpty()
                        .map { it.toMeal().toSummary() }

                    val ingredientResults = ingredientResponse.body()
                        ?.meals.orEmpty()

                    // Each recipe must appear only once.
                    val results = (nameResults + ingredientResults)
                        .distinctBy { it.id }

                    _apiMeals.value = results

                    _listMessage.value = if (results.isEmpty()) {
                        text(R.string.empty_search)
                    } else {
                        null
                    }
                } else {
                    val code = if (!nameResponse.isSuccessful) {
                        nameResponse.code()
                    } else {
                        ingredientResponse.code()
                    }

                    showLoadError(
                        text(R.string.error_server, code)
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                if (currentRequest == requestId) {
                    showLoadError(text(R.string.error_connection))
                }
            } catch (e: Exception) {
                if (currentRequest == requestId) {
                    showLoadError(text(R.string.error_unexpected))
                }
            } finally {
                if (currentRequest == requestId) {
                    _isLoading.value = false
                }
            }
        }
    }

    /*leaves search mode and restores the selected category*/
    fun showCategoryMeals() {
        _isSearchMode.value = false
        _searchQuery.value = null

        val category = _selectedCategory.value

        if (category != null) {
            loadMealsByCategory(category)
        } else {
            loadCategories()
        }
    }

    /*loads categories and selects the first one*/
    private fun loadCategories() {
        val currentRequest = ++requestId
        _isLoading.value = true
        _listMessage.value = null
        _canRetry.value = false

        viewModelScope.launch {
            try {
                val response = repository.getCategories()
                if (currentRequest != requestId) return@launch

                val body = response.body()

                if (response.isSuccessful && body != null) {
                    _categories.value = body.categories

                    val firstCategory = body.categories.firstOrNull()?.name

                    if (firstCategory == null) {
                        _listMessage.value = text(R.string.empty_categories)
                    } else {
                        _selectedCategory.value = firstCategory
                        loadMealsByCategory(firstCategory)
                    }
                } else {
                    val error = if (response.isSuccessful) {
                        text(R.string.error_unexpected)
                    } else {
                        text(R.string.error_server, response.code())
                    }
                    showLoadError(error)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                if (currentRequest == requestId) {
                    showLoadError(text(R.string.error_connection))
                }
            } catch (e: Exception) {
                if (currentRequest == requestId) {
                    showLoadError(text(R.string.error_unexpected))
                }
            } finally {
                if (currentRequest == requestId) {
                    _isLoading.value = false
                }
            }
        }
    }

    /*lads the selected category recipes*/
    private fun loadMealsByCategory(category: String) {
        val currentRequest = ++requestId
        _isLoading.value = true
        _listMessage.value = null
        _canRetry.value = false
        _apiMeals.value = emptyList()

        viewModelScope.launch {
            try {
                val response = repository.getMealsByCategory(category)
                if (currentRequest != requestId) return@launch

                val body = response.body()

                if (response.isSuccessful && body != null) {
                    val result = body.meals.orEmpty()
                    _apiMeals.value = result

                    _listMessage.value = if (result.isEmpty()) {
                        text(R.string.empty_meals)
                    } else {
                        null
                    }
                } else {
                    val error = if (response.isSuccessful) {
                        text(R.string.error_unexpected)
                    } else {
                        text(R.string.error_server, response.code())
                    }
                    showLoadError(error)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                if (currentRequest == requestId) {
                    showLoadError(text(R.string.error_connection))
                }
            } catch (e: Exception) {
                if (currentRequest == requestId) {
                    showLoadError(text(R.string.error_unexpected))
                }
            } finally {
                if (currentRequest == requestId) {
                    _isLoading.value = false
                }
            }
        }
    }

    /*recoverable error, enables retry*/
    private fun showLoadError(error: String) {
        _canRetry.value = true
        _listMessage.value = text(R.string.error_with_retry, error)
    }

    /*saves or removes a recipe using sqlite*/
    fun toggleFavorite(
        summary: MealSummary,
        fullMeal: Meal? = null
    ) {
        if (!pendingFavoriteIds.add(summary.id)) return

        viewModelScope.launch {
            try {
                val success = if (repository.isFavorite(summary.id)) {
                    // Already saved: remove it locally.
                    repository.removeFavorite(summary.id)
                } else {
                    // Use the complete recipe if it was opened in the detail.
                    val completeMeal = fullMeal ?: run {
                        val response = repository.getMealById(summary.id)

                        if (response.isSuccessful) {
                            response.body()?.meals?.firstOrNull()?.toMeal()
                        } else {
                            null
                        }
                    }

                    if (completeMeal != null) {
                        repository.addFavorite(completeMeal)
                    } else {
                        false
                    }
                }

                if (success) {
                    loadFavoritesSuspend()
                } else {
                    _toastMessage.value = text(R.string.error_favorite)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _toastMessage.value = text(R.string.error_favorite)
            } finally {
                pendingFavoriteIds.remove(summary.id)
            }
        }
    }

    /*clears a msg after the UI shows it*/
    fun onToastShown() {
        _toastMessage.value = null
    }

    /*loads stored recipes when the ViewModel starts*/
    private fun loadFavorites() {
        viewModelScope.launch {
            try {
                loadFavoritesSuspend()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _toastMessage.value = text(R.string.error_load_favorites)
            }
        }
    }

    /*refreshes shared state after database op*/
    private suspend fun loadFavoritesSuspend() {
        val savedMeals = repository.getFavorites()
        _favoriteMeals.value = savedMeals

        /*update the detail star whenever favs change*/
        val current = _detailState.value

        _detailState.value = current.copy(
            isFavorite = current.meal?.let { meal ->
                savedMeals.any { it.id == meal.id }
            } ?: false
        )
    }


    /*adds fav flags to the meals downloaded*/
    private fun mergeFavoriteState(): List<MealSummary> {
        val favoriteIds = _favoriteMeals.value.orEmpty()
            .map { it.id }
            .toSet()

        return _apiMeals.value.orEmpty().map { meal ->
            meal.copy(isFavorite = meal.id in favoriteIds)
        }
    }

    private fun text(resId: Int, vararg args: Any): String =
        getApplication<Application>().getString(resId, *args)
}

/*describes everything the screen needs to render*/
data class MealDetailUiState(
    val requestedMeal: MealSummary? = null, //identifies chosen recipe
    val meal: Meal? = null, //contains full recipe
    val isLoading: Boolean = false, //if details are downloading
    val error: String? = null, //error msg or null
    val isFavorite: Boolean = false //if the recipe is saved
    //represents interface status
)

