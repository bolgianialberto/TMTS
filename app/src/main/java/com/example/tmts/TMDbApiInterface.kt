package com.example.tmts

import com.example.tmts.beans.EpisodeDetails
import com.example.tmts.beans.MediaResponse
import com.example.tmts.beans.MovieDetails
import com.example.tmts.beans.SeasonDetails
import com.example.tmts.beans.SerieDetails
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

    @GET("search/tv")
    fun searchSerie(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int
    ): Call<MediaResponse>

    @GET("movie/{movie_id}")
    fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String

    ): Call<MovieDetails>

    @GET("tv/{serie_id}")
    fun getSerieDetails(
        @Path("serie_id") serieId: Int,
        @Query("api_key") apiKey: String

    ): Call<SerieDetails>

    @GET("tv/{serie_id}/season/{season_number}")
    fun getSeasonDetails(
        @Path("serie_id") serieId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("api_key") apiKey: String
    ): Call<SeasonDetails>

    @GET("tv/{serie_id}/season/{season_number}/episode/{episode_number}")
    fun getEpisodeDetails(
        @Path("serie_id") serieId: Int,
        @Path("season_number") seasonNumber: Int,
        @Path("episode_number") episodeNumber: Int,
        @Query("api_key") apiKey: String
    ): Call<EpisodeDetails>

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