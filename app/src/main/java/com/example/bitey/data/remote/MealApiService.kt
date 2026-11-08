
package com.example.bitey.data.remote

import com.example.bitey.data.model.CategoryResponse
import com.example.bitey.data.model.MealDetailResponse
import com.example.bitey.data.model.MealSummaryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/* endpoints to communicate with TheMealDB*/
interface MealApiService
{
    /*all available meal categories*/
    @GET("categories.php")
    suspend fun getCategories(): Response<CategoryResponse>

    /*meal summaries filtered by category*/
    @GET("filter.php")
    suspend fun getMealsByCategory(
        @Query("c") category: String
    ): Response<MealSummaryResponse>

    /*eal summaries filtered by ingredient*/
    @GET("filter.php")
    suspend fun getMealsByIngredient(
        @Query("i") ingredient: String
    ): Response<MealSummaryResponse>

    /*full recipes by meal name*/
    @GET("search.php")
    suspend fun searchMealsByName(
        @Query("s") name: String
    ): Response<MealDetailResponse>

    /*complete recipe by ID*/
    @GET("lookup.php")
    suspend fun getMealById(
        @Query("i") id: String
    ): Response<MealDetailResponse>
}
