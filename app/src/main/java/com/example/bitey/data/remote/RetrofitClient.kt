
package com.example.bitey.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/*provides one retrofit service instance for the entire app*/
object RetrofitClient
{
    private const val BASE_URL =
        "https://www.themealdb.com/api/json/v1/1/"
    val apiService: MealApiService by lazy { /*by lazy: delays creation of the service till its first use */
        Retrofit.Builder() /*starts config of retrofit*/
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealApiService::class.java)
    }
}
