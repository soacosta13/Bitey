package com.example.bitey.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.bitey.data.model.Ingredient
import com.example.bitey.data.model.Meal
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/*stores recipes so they can be read offline*/
class MealDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION)
{
    companion object {
        private const val DATABASE_NAME = "bitey.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_FAVORITES = "favorites"
        private const val COL_ID = "id"
        private const val COL_NAME = "name"
        private const val COL_CATEGORY = "category"
        private const val COL_AREA = "area"
        private const val COL_INSTRUCTIONS = "instructions"
        private const val COL_THUMBNAIL = "thumbnail"
        private const val COL_INGREDIENTS = "ingredients"
    }

    private val gson = Gson()
    private val ingredientListType = object : TypeToken<List<Ingredient>>() {}.type

    override fun onCreate(db: SQLiteDatabase) {
        /*PK is text bc api supplies string ids*/
        db.execSQL(
            """
            CREATE TABLE $TABLE_FAVORITES (
                $COL_ID TEXT PRIMARY KEY,
                $COL_NAME TEXT NOT NULL,
                $COL_CATEGORY TEXT NOT NULL,
                $COL_AREA TEXT NOT NULL,
                $COL_INSTRUCTIONS TEXT NOT NULL,
                $COL_THUMBNAIL TEXT NOT NULL,
                $COL_INGREDIENTS TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FAVORITES")
        onCreate(db)
    }

    fun insertFavorite(meal: Meal): Long {
        val values = ContentValues().apply {
            put(COL_ID, meal.id)
            put(COL_NAME, meal.name)
            put(COL_CATEGORY, meal.category)
            put(COL_AREA, meal.area)
            put(COL_INSTRUCTIONS, meal.instructions)
            put(COL_THUMBNAIL, meal.thumbnail)
            put(COL_INGREDIENTS, gson.toJson(meal.ingredients))
        }
        return writableDatabase.insertWithOnConflict(
            TABLE_FAVORITES, null, values, SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    /*DELETE: number of deleted rows*/
    fun deleteFavorite(id: String): Int =
        writableDatabase.delete(TABLE_FAVORITES, "$COL_ID = ?", arrayOf(id))

    /*SELECT all saved recipes, sorted by name*/
    fun getAllFavorites(): List<Meal> {
        val favorites = mutableListOf<Meal>()
        readableDatabase.query(
            TABLE_FAVORITES, null, null, null, null, null, "$COL_NAME ASC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                favorites.add(readMeal(cursor))
            }
        }
        return favorites
    }

    /*SELECT one saved recipe and open details without wifi */
    fun getFavoriteById(id: String): Meal? =
        readableDatabase.query(
            TABLE_FAVORITES, null, "$COL_ID = ?", arrayOf(id), null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) readMeal(cursor) else null
        }

    /*checks if a meal ID is already saved*/
    fun isFavorite(id: String): Boolean =
        readableDatabase.query(
            TABLE_FAVORITES, arrayOf(COL_ID), "$COL_ID = ?", arrayOf(id),
            null, null, null
        ).use { cursor -> cursor.moveToFirst() }

    /*reconstructs full meal as JSON. */
    private fun readMeal(cursor: Cursor): Meal {
        val ingredientsJson = cursor.getString(cursor.getColumnIndexOrThrow(COL_INGREDIENTS))
        val ingredients: List<Ingredient> =
            gson.fromJson(ingredientsJson, ingredientListType) ?: emptyList()

        return Meal(
            id = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID)),
            name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
            category = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)),
            area = cursor.getString(cursor.getColumnIndexOrThrow(COL_AREA)),
            instructions = cursor.getString(cursor.getColumnIndexOrThrow(COL_INSTRUCTIONS)),
            thumbnail = cursor.getString(cursor.getColumnIndexOrThrow(COL_THUMBNAIL)),
            ingredients = ingredients
        )
    }
}
