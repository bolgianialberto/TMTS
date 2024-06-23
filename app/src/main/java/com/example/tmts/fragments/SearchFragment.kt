package com.example.tmts.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

class SearchFragment : Fragment() {
    private lateinit var popularMovieAdapter: MediaAdapter
    private lateinit var popularSerieAdapter: MediaAdapter
    private lateinit var btnSearchPopular: ImageView

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

        val rvPopularMovie: RecyclerView = view.findViewById(R.id.rv_popular_movies)
        rvPopularMovie.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvPopularMovie.adapter = popularMovieAdapter

        val rvPopularSerie: RecyclerView = view.findViewById(R.id.rv_popular_series)
        rvPopularSerie.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvPopularSerie.adapter = popularSerieAdapter

        MediaRepository.getPopularMovies(
            onSuccess = ::onPopularMoviesFetched,
            onError = ::onError
        )

        MediaRepository.getPopularSeries(
            onSuccess = ::onPopularSeriesFetched,
            onError = ::onError
        )

        btnSearchPopular.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }
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
