package com.example.tmts

import PopularMovieAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var tmdbApiClient: TMDbApiClient
    private lateinit var popularMovieAdapter: PopularMovieAdapter
    private lateinit var popularSerieAdapter: PopularSerieAdapter
    private lateinit var etPopularSearch: EditText
    private lateinit var btnSearchPopular: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tmdbApiClient = TMDbApiClient()
        popularMovieAdapter = PopularMovieAdapter(this, emptyList())
        popularSerieAdapter = PopularSerieAdapter(this, emptyList())

        etPopularSearch = findViewById(R.id.et_search_popular)
        btnSearchPopular = findViewById(R.id.btn_search_popular)

        val rvPopularMovie: RecyclerView = findViewById(R.id.rv_popular_movies)
        rvPopularMovie.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvPopularMovie.adapter = popularMovieAdapter

        val rvPopularSerie: RecyclerView = findViewById(R.id.rv_popular_series)
        rvPopularSerie.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvPopularSerie.adapter = popularSerieAdapter

        loadPopularMovies()

        loadPopularSeries()

        etPopularSearch.setOnClickListener {
            val intent = Intent(this@MainActivity, SearchActivity::class.java)
            startActivity(intent)
        }

        btnSearchPopular.setOnClickListener {
            val intent = Intent(this@MainActivity, SearchActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadPopularSeries() {
        val call = tmdbApiClient.getClient().getPopularSeries(tmdbApiClient.getApiKey(), 1)

        call.enqueue(object: Callback<MediaResponse> {
            override fun onResponse(call: Call<MediaResponse>, response: Response<MediaResponse>) {
                if(response.isSuccessful) {
                    val serieResponse = response.body()
                    val series: List<Media>? = serieResponse?.results
                    if (series != null) {
                        popularSerieAdapter.updateSeries(series)
                    } else {
                        Log.e("API Call", "La risposta non contiene serie TV.")
                    }
                } else {
                    Log.e("API Call", "Errore nella chiamata API: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<MediaResponse>, t: Throwable) {
                Log.e("API Call", "Errore di rete: ${t.message}")
            }
        })
    }

    private fun loadPopularMovies() {
        val call = tmdbApiClient.getClient().getPopularMovies(tmdbApiClient.getApiKey(), 1)

        call.enqueue(object: Callback<MediaResponse> {
            override fun onResponse(call: Call<MediaResponse>, response: Response<MediaResponse>) {
                if (response.isSuccessful) {
                    val movieResponse = response.body()
                    val movies: List<Media>? = movieResponse?.results
                    if (movies != null) {
                        popularMovieAdapter.updateMovies(movies)
                    } else {
                        Log.e("API Call", "La risposta non contiene film.")
                    }
                } else {
                    Log.e("API Call", "Errore nella chiamata API: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<MediaResponse>, t: Throwable) {
                Log.e("API Call", "Errore di rete: ${t.message}")
            }
        })
    }
}