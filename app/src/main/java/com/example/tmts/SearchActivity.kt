package com.example.tmts

import SearchMovieAdapter
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchActivity : AppCompatActivity() {

    private lateinit var tmdbApiClient: TMDbApiClient
    private lateinit var movieAdapter: SearchMovieAdapter
    private lateinit var ivBackSearch: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        tmdbApiClient = TMDbApiClient()
        movieAdapter = SearchMovieAdapter(this, emptyList())
        ivBackSearch = findViewById(R.id.iv_arrow_back_search)

        val rvMovie: RecyclerView = findViewById(R.id.rv_movie)
        rvMovie.layoutManager = LinearLayoutManager(this)
        rvMovie.adapter = movieAdapter

        val etSearch: EditText = findViewById(R.id.et_search)
        etSearch.requestFocus()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Non utilizzato in questo caso
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Non utilizzato in questo caso
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    searchMovies(query)
                }
            }
        })

        ivBackSearch.setOnClickListener {
            val intent = Intent(this@SearchActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun searchMovies(query: String) {
        val call = tmdbApiClient.getClient().searchMovies(tmdbApiClient.getApiKey(), query, 1)

        call.enqueue(object: Callback<MediaResponse> {
            override fun onResponse(call: Call<MediaResponse>, response: Response<MediaResponse>) {
                if (response.isSuccessful) {
                    val movieResponse = response.body()
                    val movies: List<Media>? = movieResponse?.results
                    if (movies != null) {
                        movieAdapter.updateMovies(movies)
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