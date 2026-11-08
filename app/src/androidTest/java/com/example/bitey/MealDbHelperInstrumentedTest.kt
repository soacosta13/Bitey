package com.example.bitey

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.bitey.data.local.MealDbHelper
import com.example.bitey.data.model.Ingredient
import com.example.bitey.data.model.Meal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Tests SQLite on an Android device without making network requests. */
@RunWith(AndroidJUnit4::class)
class MealDbHelperInstrumentedTest {

    @Test
    fun savedMealSurvivesReopeningAndCanBeDeleted() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val meal = Meal(
            id = "__bitey_sqlite_test__",
            name = "SQLite test meal",
            category = "Test",
            area = "Test",
            instructions = "Step one.\nStep two.",
            thumbnail = "https://example.com/test.jpg",
            ingredients = listOf(Ingredient("Flour", "1 cup"), Ingredient("Water", "2 cups"))
        )

        val first = MealDbHelper(context)
        try {
            // Cleanup in case an earlier test run stopped unexpectedly.
            first.deleteFavorite(meal.id)
            assertTrue(first.insertFavorite(meal) != -1L)
            assertTrue(first.isFavorite(meal.id))
        } finally {
            first.close()
        }

        // A new helper reads the same data from the database on disk.
        val reopened = MealDbHelper(context)
        try {
            assertEquals(meal, reopened.getFavoriteById(meal.id))
            assertTrue(reopened.getAllFavorites().contains(meal))
            assertEquals(1, reopened.deleteFavorite(meal.id))
            assertFalse(reopened.isFavorite(meal.id))
        } finally {
            reopened.deleteFavorite(meal.id)
            reopened.close()
        }
    }
}
