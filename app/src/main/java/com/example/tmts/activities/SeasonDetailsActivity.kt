package com.example.tmts.activities

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.MediaRepository
import com.example.tmts.R
import com.example.tmts.adapters.SeasonAdapter
import com.example.tmts.beans.SeasonDetails
import com.example.tmts.beans.SerieDetails

class SeasonDetailsActivity : AppCompatActivity() {
    private lateinit var rvSeasons: RecyclerView
    private lateinit var seasonAdapter: SeasonAdapter
    private lateinit var btnBackArrow: Button
    private lateinit var tvSerieTitle: TextView
    val seasonsList = mutableListOf<SeasonDetails>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_season_details)

        val intent = intent
        val serieId = intent.getIntExtra("serieId", -1)
        val serieTitle = intent.getStringExtra("serieTitle")

        btnBackArrow = findViewById(R.id.iv_arrow_back_season_details)
        tvSerieTitle = findViewById(R.id.tv_season_details_serie_title)

        seasonAdapter = SeasonAdapter(this, emptyList())
        rvSeasons = findViewById(R.id.rv_seasons)

        rvSeasons.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvSeasons.adapter = seasonAdapter

        MediaRepository.getSerieDetails(
            serieId,
            onSuccess = ::onFetchedSerieDetails,
            onError = ::onError
        )

        btnBackArrow.setOnClickListener{
            onBackPressed()
        }
    }

    private fun onFetchedSerieDetails(serie: SerieDetails){
        tvSerieTitle.text = serie.title

        for (nSeason in 1..serie.number_of_seasons) {
            MediaRepository.getSeasonDetails(
                serie.id,
                nSeason,
                onSuccess = {season ->
                    seasonsList.add(season)
                    season.serieId = serie.id
                    season.serieName = serie.title
                    if (seasonsList.size == serie.number_of_seasons) {
                        val sortedSeasonList = seasonsList.sortedBy { it.season_number }
                        seasonAdapter.updateMedia(sortedSeasonList)
                    }
                },
                onError = {
                    Log.e("SerieDetailsActivity", "Something went wrong")
                }
            )
        }

    }
    private fun onError(){
        Log.e("SeasonDetailsActivity", "Something went wrong")
    }
}