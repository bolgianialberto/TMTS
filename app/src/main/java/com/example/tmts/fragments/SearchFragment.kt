package com.example.tmts.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.MediaRepository
import com.example.tmts.beans.Media
import com.example.tmts.R
import com.example.tmts.activities.SearchActivity
import com.example.tmts.activities.MovieDetailsActivity
import com.example.tmts.activities.SerieDetailsActivity
import com.example.tmts.adapters.MediaAdapter
import com.example.tmts.beans.Genre

class SearchFragment : Fragment() {
    private lateinit var popularMovieAdapter: MediaAdapter
    private lateinit var popularSerieAdapter: MediaAdapter
    private lateinit var genresMovieAdapter: MediaAdapter
    private lateinit var genresSerieAdapter: MediaAdapter
    private lateinit var btnSearchPopular: ImageView
    private lateinit var spinnerMovieGenre: Spinner
    private lateinit var spinnerSerieGenre: Spinner
    private var genresAdapter: ArrayAdapter<String>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSearchPopular = view.findViewById(R.id.btn_search_popular)
        spinnerMovieGenre = view.findViewById(R.id.spinner_movie_genre)
        spinnerSerieGenre = view.findViewById(R.id.spinner_serie_genre)

        popularMovieAdapter = MediaAdapter(requireContext(), emptyList()) { movie ->
            val intent = Intent(requireContext(), MovieDetailsActivity::class.java)
            intent.putExtra("movieId", movie.id)
            startActivity(intent)
        }
        popularSerieAdapter = MediaAdapter(requireContext(), emptyList()) {serie ->
            val intent = Intent(requireContext(), SerieDetailsActivity::class.java)
            intent.putExtra("serieId", serie.id)
            startActivity(intent)
        }
        genresMovieAdapter = MediaAdapter(requireContext(), emptyList()) {movie ->
            val intent = Intent(requireContext(), MovieDetailsActivity::class.java)
            intent.putExtra("movieId", movie.id)
            startActivity(intent)
        }
        genresSerieAdapter = MediaAdapter(requireContext(), emptyList()) {serie ->
            val intent = Intent(requireContext(), SerieDetailsActivity::class.java)
            intent.putExtra("serieId", serie.id)
            startActivity(intent)
        }

        val rvPopularMovie: RecyclerView = view.findViewById(R.id.rv_popular_movies)
        rvPopularMovie.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvPopularMovie.adapter = popularMovieAdapter

        val rvPopularSerie: RecyclerView = view.findViewById(R.id.rv_popular_series)
        rvPopularSerie.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvPopularSerie.adapter = popularSerieAdapter

        val rvGenresMovie: RecyclerView = view.findViewById(R.id.rv_movie_by_genre)
        rvGenresMovie.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvGenresMovie.adapter = genresMovieAdapter

        val rvSeriesMovie: RecyclerView = view.findViewById(R.id.rv_serie_by_genre)
        rvSeriesMovie.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvSeriesMovie.adapter = genresSerieAdapter

        btnSearchPopular.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }

        MediaRepository.getMovieGenres(
            onSuccess = ::onMovieGenresFetched,
            onError = ::onError
        )

        MediaRepository.getSerieGenres(
            onSuccess = ::onSerieGenresFetched,
            onError = ::onError
        )
    }

    override fun onResume() {
        super.onResume()
        loadData()
    }

    private fun loadData(){
        MediaRepository.getPopularMovies(
            onSuccess = ::onPopularMoviesFetched,
            onError = ::onError
        )

        MediaRepository.getPopularSeries(
            onSuccess = ::onPopularSeriesFetched,
            onError = ::onError
        )
    }

    private fun onSerieGenresFetched(genres: List<Genre>){
        genresAdapter = context?.let { ArrayAdapter<String>(it, R.layout.custom_spinner_dropdown_item) }
        spinnerSerieGenre.adapter = genresAdapter
        val genreNames = genres.map {it.name}
        genresAdapter?.addAll(genreNames)
        genresAdapter?.notifyDataSetChanged()

        spinnerSerieGenre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Ottieni il genere selezionato
                val selectedGenre = genres[position]

                loadSeriesByGenre(selectedGenre.id)

                // Esegui le azioni desiderate con il genere selezionato
                Log.d("TMDB_API", "Genere selezionato: ${selectedGenre.name}")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Gestisci il caso in cui non viene selezionato nulla
            }
        }
    }

    private fun onMovieGenresFetched(genres: List<Genre>){
        genresAdapter = context?.let { ArrayAdapter<String>(it, R.layout.custom_spinner_dropdown_item) }
        spinnerMovieGenre.adapter = genresAdapter
        val genreNames = genres.map {it.name}
        genresAdapter?.addAll(genreNames)
        genresAdapter?.notifyDataSetChanged()

        spinnerMovieGenre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Ottieni il genere selezionato
                val selectedGenre = genres[position]

                loadMoviesByGenre(selectedGenre.id)

                // Esegui le azioni desiderate con il genere selezionato
                Log.d("TMDB_API", "Genere selezionato: ${selectedGenre.name}")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Gestisci il caso in cui non viene selezionato nulla
            }
        }
    }

    private fun loadMoviesByGenre(genreId: Int){
        MediaRepository.discoverMoviesByGenre(
            genreId,
            onSuccess = {movies ->
                genresMovieAdapter.updateMedia(movies)
            },
            onError = ::onError
        )
    }

    private fun loadSeriesByGenre(genreId: Int){
        MediaRepository.discoverSeriesByGenre(
            genreId,
            onSuccess = {movies ->
                genresSerieAdapter.updateMedia(movies)
            },
            onError = ::onError
        )
    }

    private fun onPopularMoviesFetched(movies: List<Media>){
        popularMovieAdapter.updateMedia(movies)
    }

    private fun onPopularSeriesFetched(movies: List<Media>){
        popularSerieAdapter.updateMedia(movies)
    }

    private fun onError(){
        Log.e("API call", "errore")
    }
}
