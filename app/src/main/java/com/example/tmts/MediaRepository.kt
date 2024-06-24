package com.example.tmts

import android.util.Log
import com.example.tmts.beans.EpisodeDetails
import com.example.tmts.beans.Genre
import com.example.tmts.beans.GenreResponse
import com.example.tmts.beans.Media
import com.example.tmts.beans.MediaDetails
import com.example.tmts.beans.MediaResponse
import com.example.tmts.beans.MovieDetails
import com.example.tmts.beans.SeasonDetails
import com.example.tmts.beans.SerieDetails
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object MediaRepository {
    private var tmdbApiClient = TMDbApiClient()

    fun getMovieGenres(
        onSuccess: (genres: List<Genre>) -> Unit,
        onError: () -> Unit
    ) {
        val call = tmdbApiClient.getClient().getMovieGenres(tmdbApiClient.getApiKey())
        call.enqueue(object : Callback<GenreResponse> {
            override fun onResponse(call: Call<GenreResponse>, response: Response<GenreResponse>) {
                if (response.isSuccessful) {
                    val genreResponse = response.body()
                    val genres = genreResponse!!.results
                    if (genres != null) {
                        onSuccess.invoke(genres)
                    } else {
                        Log.e("TMDB_API", "Genres list is null")
                        onError.invoke()
                    }
                } else {
                    Log.e("TMDB_API", "Unsuccessful response: ${response.errorBody()?.string()}")
                    onError.invoke()
                }
            }

            override fun onFailure(call: Call<GenreResponse>, t: Throwable) {
                Log.e("TMDB_API", "Call failed: ${t.message}")
                onError.invoke()
            }
        })
    }

    fun getSerieGenres(
        onSuccess: (genres: List<Genre>) -> Unit,
        onError: () -> Unit
    ) {
        val call = tmdbApiClient.getClient().getSerieGenres(tmdbApiClient.getApiKey())
        call.enqueue(object : Callback<GenreResponse> {
            override fun onResponse(call: Call<GenreResponse>, response: Response<GenreResponse>) {
                if (response.isSuccessful) {
                    val genreResponse = response.body()
                    val genres = genreResponse!!.results
                    if (genres != null) {
                        onSuccess.invoke(genres)
                    } else {
                        Log.e("TMDB_API", "Genres list is null")
                        onError.invoke()
                    }
                } else {
                    Log.e("TMDB_API", "Unsuccessful response: ${response.errorBody()?.string()}")
                    onError.invoke()
                }
            }

            override fun onFailure(call: Call<GenreResponse>, t: Throwable) {
                Log.e("TMDB_API", "Call failed: ${t.message}")
                onError.invoke()
            }
        })
    }

    fun discoverMoviesByGenre(
        genreId: Int,
        onSuccess: (movies: List<Media>) -> Unit,
        onError: () -> Unit
    ) {
        val call = tmdbApiClient.getClient().discoverMoviesByGenre(tmdbApiClient.getApiKey(), genreId, 1)
        call.enqueue(object : Callback<MediaResponse> {
            override fun onResponse(call: Call<MediaResponse>, response: Response<MediaResponse>) {
                if (response.isSuccessful) {
                    val mediaResponse = response.body()
                    val mediaItems: List<Media>? = mediaResponse?.results
                    if (mediaItems != null) {
                        onSuccess.invoke(mediaItems)
                    } else {
                        Log.e("TMDB_API", "mediaItems is null")
                        onError.invoke()
                    }
                } else {
                    Log.e("TMDB_API", "Unsuccessful response: ${response.errorBody()?.string()}")
                    onError.invoke()
                }
            }

            override fun onFailure(call: Call<MediaResponse>, t: Throwable) {
                Log.e("TMDB_API", "Call failed: ${t.message}")
                onError.invoke()
            }
        })
    }

    fun discoverSeriesByGenre(
        genreId: Int,
        onSuccess: (series: List<Media>) -> Unit,
        onError: () -> Unit
    ) {
        val call = tmdbApiClient.getClient().discoverSeriesByGenre(tmdbApiClient.getApiKey(), genreId, 1)
        call.enqueue(object : Callback<MediaResponse> {
            override fun onResponse(call: Call<MediaResponse>, response: Response<MediaResponse>) {
                if (response.isSuccessful) {
                    val mediaResponse = response.body()
                    val mediaItems: List<Media>? = mediaResponse?.results
                    if (mediaItems != null) {
                        onSuccess.invoke(mediaItems)
                    } else {
                        Log.e("TMDB_API", "mediaItems is null")
                        onError.invoke()
                    }
                } else {
                    Log.e("TMDB_API", "Unsuccessful response: ${response.errorBody()?.string()}")
                    onError.invoke()
                }
            }

            override fun onFailure(call: Call<MediaResponse>, t: Throwable) {
                Log.e("TMDB_API", "Call failed: ${t.message}")
                onError.invoke()
            }
        })
    }

    fun getPopularMovies(
        onSuccess: (movies: List<Media>) -> Unit,
        onError: () -> Unit
    ) {
        val call = tmdbApiClient.getClient().getPopularMovies(tmdbApiClient.getApiKey(), 1)
        call.enqueue(object : Callback<MediaResponse> {
            override fun onResponse(call: Call<MediaResponse>, response: Response<MediaResponse>) {
                if (response.isSuccessful) {
                    val mediaResponse = response.body()
                    val mediaItems: List<Media>? = mediaResponse?.results
                    if (mediaItems != null) {
                        onSuccess.invoke(mediaItems)
                    } else {
                        Log.e("TMDB_API", "mediaItems è nullo")
                        onError.invoke()
                    }
                } else {
                    Log.e("TMDB_API", "Risposta non riuscita: ${response.errorBody()?.string()}")
                    onError.invoke()
                }
            }

            override fun onFailure(call: Call<MediaResponse>, t: Throwable) {
                Log.e("TMDB_API", "Chiamata fallita: ${t.message}")
                onError.invoke()
            }
        })
    }


    fun getPopularSeries(
        onSuccess: (movies: List<Media>) -> Unit,
        onError: () -> Unit
    ){
        val call = tmdbApiClient.getClient().getPopularSeries(tmdbApiClient.getApiKey(), 1)
        call.enqueue(object : Callback<MediaResponse> {
            override fun onResponse(call: Call<MediaResponse>, response: Response<MediaResponse>) {
                if (response.isSuccessful) {
                    val mediaResponse = response.body()
                    val mediaItems: List<Media>? = mediaResponse?.results
                    if (mediaItems != null) {
                        onSuccess.invoke(mediaItems)
                    } else {
                        onError.invoke()
                    }
                } else {
                    onError.invoke()
                }
            }
            override fun onFailure(call: Call<MediaResponse>, t: Throwable) {
                onError.invoke()
            }
        })
    }

    fun getMediaDetails(
        mediaId: Int,
        mediaType: String,
        onSuccess: (MediaDetails) -> Unit,
        onError: () -> Unit
    ) {
        when (mediaType) {
            "movie" -> getMovieDetails(mediaId, onSuccess, onError)
            "serie" -> getSerieDetails(mediaId, onSuccess, onError)
        }
    }

    fun getMovieDetails(
        movieId: Int,
        onSuccess: (movie: MovieDetails) -> Unit,
        onError: () -> Unit
    ) {
        val call = tmdbApiClient.getClient().getMovieDetails((movieId).toInt(), tmdbApiClient.getApiKey())
        call.enqueue(object: Callback<MovieDetails> {
            override fun onResponse(
                call: Call<MovieDetails>,
                response: Response<MovieDetails>
            ) {
                if (response.isSuccessful) {
                    val movie = response.body()
                    if (movie != null) {
                        onSuccess.invoke(movie)
                    } else {
                        onError.invoke()
                    }
                } else {
                    onError.invoke()
                }
            }

            override fun onFailure(call: Call<MovieDetails>, t: Throwable) {
                onError.invoke()
            }
        })
    }

    fun getSerieDetails(
        serieId: Int,
        onSuccess: (serie: SerieDetails) -> Unit,
        onError: () -> Unit
    ) {
        val call = tmdbApiClient.getClient().getSerieDetails((serieId).toInt(), tmdbApiClient.getApiKey())
        call.enqueue(object: Callback<SerieDetails> {
            override fun onResponse(
                call: Call<SerieDetails>,
                response: Response<SerieDetails>
            ) {
                if (response.isSuccessful) {
                    val serie = response.body()
                    if (serie != null) {
                        onSuccess.invoke(serie)
                    } else {
                        onError.invoke()
                    }
                } else {
                    onError.invoke()
                }
            }

            override fun onFailure(call: Call<SerieDetails>, t: Throwable) {
                onError.invoke()
            }
        })
    }

    fun getSeasonDetails(
        serieId: Int,
        seasonNumber: Int,
        onSuccess: (season: SeasonDetails) -> Unit,
        onError: () -> Unit
    ) {
        val call = tmdbApiClient.getClient().getSeasonDetails((serieId).toInt(), seasonNumber, tmdbApiClient.getApiKey())
        call.enqueue(object: Callback<SeasonDetails> {
            override fun onResponse(
                call: Call<SeasonDetails>,
                response: Response<SeasonDetails>
            ) {
                if (response.isSuccessful) {
                    val season = response.body()
                    if (season != null) {
                        onSuccess.invoke(season)
                    } else {
                        onError.invoke()
                    }
                } else {
                    onError.invoke()
                }
            }

            override fun onFailure(call: Call<SeasonDetails>, t: Throwable) {
                onError.invoke()
            }
        })
    }

    fun getEpisodeDetails(
        serieId: Int,
        seasonNumber: Int,
        episodeNumber: Int,
        onSuccess: (episode: EpisodeDetails) -> Unit,
        onError: () -> Unit
    ) {
        val call = tmdbApiClient.getClient().getEpisodeDetails((serieId).toInt(), seasonNumber, episodeNumber, tmdbApiClient.getApiKey())
        call.enqueue(object: Callback<EpisodeDetails> {
            override fun onResponse(
                call: Call<EpisodeDetails>,
                response: Response<EpisodeDetails>
            ) {
                if (response.isSuccessful) {
                    val episode = response.body()
                    episode!!.serieId = serieId //assegno qui l'id della serie
                    if (episode != null) {
                        onSuccess.invoke(episode)
                    } else {
                        onError.invoke()
                    }
                } else {
                    onError.invoke()
                }
            }

            override fun onFailure(call: Call<EpisodeDetails>, t: Throwable) {
                onError.invoke()
            }
        })
    }

    fun searchSeriesByTitle(
        query: String,
        onSuccess: (series: MediaResponse, query: String) -> Unit,
        onError: () -> Unit
    ){
        val call = tmdbApiClient.getClient().searchSerie(tmdbApiClient.getApiKey(), query, 1)

        call.enqueue(object: Callback<MediaResponse> {
            override fun onResponse(call: Call<MediaResponse>, response: Response<MediaResponse>) {
                if (response.isSuccessful) {
                    val series = response.body()
                    if (series!= null) {
                        onSuccess.invoke(series, query)
                    } else {
                        onError.invoke()
                    }
                } else {
                    onError.invoke()
                }
            }

            override fun onFailure(call: Call<MediaResponse>, t: Throwable) {
                onError.invoke()
            }
        })
    }

    fun searchMoviesByTitle(
        query: String,
        onSuccess: (movies: MediaResponse, query: String) -> Unit,
        onError: () -> Unit
    ){
        val call = tmdbApiClient.getClient().searchMovies(tmdbApiClient.getApiKey(), query, 1)
        call.enqueue(object: Callback<MediaResponse> {
            override fun onResponse(
                call: Call<MediaResponse>,
                response: Response<MediaResponse>
            ) {
                if (response.isSuccessful) {
                    val movies = response.body()
                    if ( movies != null ) {
                        onSuccess.invoke(movies, query)
                    } else {
                        onError.invoke()                    }
                } else {
                    onError.invoke()                }
            }

            override fun onFailure(call: Call<MediaResponse>, t: Throwable) {
                onError.invoke()            }
        })
    }
}