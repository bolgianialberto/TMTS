package com.example.tmts

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMDbApiInterface {
    @GET("search/movie")
    fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int
    ): Call<MediaResponse>

    @GET("movie/{movie_id}")
    fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String

    ): Call<MovieDetails>

    @GET("movie/popular")
    fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): Call<MediaResponse>

    @GET("tv/popular")
    fun getPopularSeries(
        @Query("api_key") apiKey: String,
        @Query("page") page: Int = 1
    ): Call<MediaResponse>
}