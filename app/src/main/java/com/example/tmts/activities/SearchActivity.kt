package com.example.tmts.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.tmts.R
import com.example.tmts.TMDbApiClient
import com.example.tmts.fragments.SearchMovieFragment
import com.example.tmts.fragments.SearchSerieFragment

class SearchActivity : AppCompatActivity() {

    private lateinit var tmdbApiClient: TMDbApiClient
    private lateinit var ivBackSearch: ImageView
    private lateinit var btnFilm: Button
    private lateinit var btnSerie: Button
    private var currentFragment: Fragment? = null
    private lateinit var etSearch: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        tmdbApiClient = TMDbApiClient()
        ivBackSearch = findViewById(R.id.iv_arrow_back_search)

        btnFilm = findViewById(R.id.btn_film)
        btnSerie = findViewById(R.id.btn_serie)

        etSearch = findViewById(R.id.et_search)
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
                performSearch(query)
            }
        })

        btnFilm.setOnClickListener {
            replaceFragment(SearchMovieFragment())
        }

        btnSerie.setOnClickListener {
            replaceFragment(SearchSerieFragment())
        }

        ivBackSearch.setOnClickListener {
            onBackPressed()
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        currentFragment = fragment
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fl_search_page_fragment, fragment)
        fragmentTransaction.runOnCommit {
            val query = etSearch.text.toString().trim()
            performSearch(query)
        }
        fragmentTransaction.commit()
    }

    private fun performSearch(query: String) {
        if (query.isNotEmpty()) {
            if (currentFragment is SearchMovieFragment) {
                (currentFragment as SearchMovieFragment).searchMovies(query)
            } else if (currentFragment is SearchSerieFragment) {
                (currentFragment as SearchSerieFragment).searchSeries(query)
            }
        } else {
            // Se il testo di ricerca è vuoto, cancella i risultati
            if (currentFragment is SearchMovieFragment) {
                (currentFragment as SearchMovieFragment).clearResults()
            } else if (currentFragment is SearchSerieFragment) {
                (currentFragment as SearchSerieFragment).clearResults()
            }
        }
    }
}