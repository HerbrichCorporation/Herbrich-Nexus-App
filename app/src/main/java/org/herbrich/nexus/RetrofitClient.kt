package org.herbrich.nexus

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.herbrich.org/"

    val instance: HerbrichApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Verwandelt JSON in deine Klassen
            .build()
            .create(HerbrichApiService::class.java)
    }
}
