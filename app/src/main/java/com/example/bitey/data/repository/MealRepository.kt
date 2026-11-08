package com.example.bitey.data.repository

import android.content.Context
import com.example.bitey.data.local.MealDbHelper
import com.example.bitey.data.model.CategoryResponse
import com.example.bitey.data.model.Meal
import com.example.bitey.data.model.MealSummaryResponse
import com.example.bitey.data.remote.MealApiService
import com.example.bitey.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import com.example.bitey.data.model.MealDetailResponse

/*entry point for remote api data and local sqlite favs*/
class MealRepository(context: Context)
{
    private val api: MealApiService = RetrofitClient.apiService
    private val dbHelper = MealDbHelper(context.applicationContext)

    /*remote op*/
    suspend fun getCategories(): Response<CategoryResponse> =
        withContext(Dispatchers.IO) { api.getCategories() }

    suspend fun getMealsByCategory(category: String): Response<MealSummaryResponse> =
        withContext(Dispatchers.IO) { api.getMealsByCategory(category) }

    /*downloads complete recipe before saving it*/
    suspend fun getMealById(id: String): Response<MealDetailResponse> =
        withContext(Dispatchers.IO) {
            api.getMealById(id)
        }

    /*local op*/
    suspend fun getFavorites(): List<Meal> =
        withContext(Dispatchers.IO) { dbHelper.getAllFavorites() }

    suspend fun getFavoriteById(id: String): Meal? =
        withContext(Dispatchers.IO) { dbHelper.getFavoriteById(id) }

    suspend fun isFavorite(id: String): Boolean =
        withContext(Dispatchers.IO) { dbHelper.isFavorite(id) }

    suspend fun addFavorite(meal: Meal): Boolean =
        withContext(Dispatchers.IO) { dbHelper.insertFavorite(meal) != -1L }

    suspend fun removeFavorite(id: String): Boolean =
        withContext(Dispatchers.IO) { dbHelper.deleteFavorite(id) > 0 }


    /*searches full recipes by meal name*/
    suspend fun searchMealsByName(
        name: String
    ): Response<MealDetailResponse> =
        withContext(Dispatchers.IO) {
            api.searchMealsByName(name)
        }

    /*searches meal summaries by main ingredient*/
    suspend fun searchMealsByIngredient(
        ingredient: String
    ): Response<MealSummaryResponse> =
        withContext(Dispatchers.IO) {
            api.getMealsByIngredient(ingredient)
        }

}
