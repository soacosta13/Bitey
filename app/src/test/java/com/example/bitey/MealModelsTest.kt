package com.example.bitey

import com.example.bitey.data.model.MealDetailResponse
import com.example.bitey.data.model.MealSummaryResponse
import com.example.bitey.data.model.toMeal
import com.example.bitey.data.model.toSummary
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MealModelsTest {

    private val gson = Gson()

    @Test
    fun detailJson_isParsed_andEmptyIngredientSlotsAreDiscarded() {
        // Trimmed copy of the real search.php?s=Arrabiata response
        val json = """
            {"meals":[{"idMeal":"52771","strMeal":"Spicy Arrabiata Penne",
            "strCategory":"Vegetarian","strArea":"Italian",
            "strInstructions":"Boil the pasta.\r\nMake the sauce.",
            "strMealThumb":"https:\/\/www.themealdb.com\/images\/media\/meals\/ustsqw1468250014.jpg",
            "strIngredient1":"penne rigate","strIngredient2":"olive oil",
            "strIngredient3":"","strIngredient4":null,
            "strMeasure1":"1 pound","strMeasure2":"1\/4 cup",
            "strMeasure3":"","strMeasure4":null}]}
        """.trimIndent()

        val meal = gson.fromJson(json, MealDetailResponse::class.java).meals!!.first().toMeal()

        assertEquals("52771", meal.id)
        assertEquals("Italian", meal.area)
        assertEquals(2, meal.ingredients.size) // the "" and null slots were dropped
        assertEquals("olive oil", meal.ingredients[1].name)
        assertEquals("1/4 cup", meal.ingredients[1].measure)
        assertEquals(meal.id, meal.toSummary().id)
    }

    @Test
    fun noResults_metaIsNull() {
        val response = gson.fromJson("""{"meals":null}""", MealSummaryResponse::class.java)
        assertNull(response.meals)
    }

    @Test
    fun filterJson_isParsedIntoSummaries() {
        val json = """
            {"meals":[{"strMeal":"Escovitch Fish",
            "strMealThumb":"https:\/\/www.themealdb.com\/images\/media\/meals\/1520084413.jpg",
            "idMeal":"52944"}]}
        """.trimIndent()

        val meal = gson.fromJson(json, MealSummaryResponse::class.java).meals!!.first()

        assertEquals("52944", meal.id)
        assertEquals("Escovitch Fish", meal.name)
        assertEquals(false, meal.isFavorite)
    }
}