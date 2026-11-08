package com.example.bitey.data.model

import com.google.gson.annotations.SerializedName

/** GET categories.php -> {"categories": [ {...}, {...} ]} */
data class CategoryResponse(
    @SerializedName("categories") val categories: List<Category>
)


data class Category(
    @SerializedName("strCategory") val name: String
)

data class MealSummaryResponse(
    @SerializedName("meals") val meals: List<MealSummary>?
)

data class MealDetailResponse(
    @SerializedName("meals") val meals: List<MealDto>?
)

data class MealSummary(
    @SerializedName("idMeal") val id: String,
    @SerializedName("strMeal") val name: String,
    @SerializedName("strMealThumb") val thumbnail: String,
    @Transient val isFavorite: Boolean = false
)

/*full recipe*/
data class MealDto(
    val idMeal: String,
    val strMeal: String,
    val strCategory: String?,
    val strArea: String?,
    val strInstructions: String?,
    val strMealThumb: String?,
    val strIngredient1: String?,
    val strIngredient2: String?,
    val strIngredient3: String?,
    val strIngredient4: String?,
    val strIngredient5: String?,
    val strIngredient6: String?,
    val strIngredient7: String?,
    val strIngredient8: String?,
    val strIngredient9: String?,
    val strIngredient10: String?,
    val strIngredient11: String?,
    val strIngredient12: String?,
    val strIngredient13: String?,
    val strIngredient14: String?,
    val strIngredient15: String?,
    val strIngredient16: String?,
    val strIngredient17: String?,
    val strIngredient18: String?,
    val strIngredient19: String?,
    val strIngredient20: String?,
    val strMeasure1: String?,
    val strMeasure2: String?,
    val strMeasure3: String?,
    val strMeasure4: String?,
    val strMeasure5: String?,
    val strMeasure6: String?,
    val strMeasure7: String?,
    val strMeasure8: String?,
    val strMeasure9: String?,
    val strMeasure10: String?,
    val strMeasure11: String?,
    val strMeasure12: String?,
    val strMeasure13: String?,
    val strMeasure14: String?,
    val strMeasure15: String?,
    val strMeasure16: String?,
    val strMeasure17: String?,
    val strMeasure18: String?,
    val strMeasure19: String?,
    val strMeasure20: String?
)

/*one line of the ingredient list*/
data class Ingredient(
    val name: String,
    val measure: String
)

/*saved recipes readable offline*/
data class Meal(
    val id: String,
    val name: String,
    val category: String,
    val area: String,
    val instructions: String,
    val thumbnail: String,
    val ingredients: List<Ingredient>
)

fun MealDto.toMeal(): Meal {
    val slots = listOf(
        strIngredient1 to strMeasure1, strIngredient2 to strMeasure2,
        strIngredient3 to strMeasure3, strIngredient4 to strMeasure4,
        strIngredient5 to strMeasure5, strIngredient6 to strMeasure6,
        strIngredient7 to strMeasure7, strIngredient8 to strMeasure8,
        strIngredient9 to strMeasure9, strIngredient10 to strMeasure10,
        strIngredient11 to strMeasure11, strIngredient12 to strMeasure12,
        strIngredient13 to strMeasure13, strIngredient14 to strMeasure14,
        strIngredient15 to strMeasure15, strIngredient16 to strMeasure16,
        strIngredient17 to strMeasure17, strIngredient18 to strMeasure18,
        strIngredient19 to strMeasure19, strIngredient20 to strMeasure20
    )

    val ingredients = slots.mapNotNull { (name, measure) ->
        if (name.isNullOrBlank()) null
        else Ingredient(name = name.trim(), measure = measure.orEmpty().trim())
    }

    return Meal(
        id = idMeal,
        name = strMeal,
        category = strCategory.orEmpty(),
        area = strArea.orEmpty(),
        instructions = strInstructions.orEmpty(),
        thumbnail = strMealThumb.orEmpty(),
        ingredients = ingredients
    )
}

/* full recipe to the list-row version */
fun Meal.toSummary(): MealSummary =
    MealSummary(id = id, name = name, thumbnail = thumbnail)