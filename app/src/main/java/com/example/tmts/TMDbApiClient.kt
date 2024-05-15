package com.example.tmts
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class TMDbApiClient {
    private var BASE_URL = "https://api.themoviedb.org/3/"
    private var API_KEY = "f96b9de197b7c040ba36ee92744ca1f1"
    private var apiInterface: TMDbApiInterface? = null

    fun getClient(): TMDbApiInterface {
        if (apiInterface == null) {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            apiInterface = retrofit.create(TMDbApiInterface::class.java)
        }
        return apiInterface!!
    }

    fun getApiKey(): String {
        return API_KEY
    }
}