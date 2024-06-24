package com.example.tmts.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.FirebaseInteraction
import com.example.tmts.MediaRepository
import com.example.tmts.R
import com.example.tmts.adapters.CastAdapter
import com.example.tmts.adapters.SeasonAdapter
import com.example.tmts.beans.EpisodeDetails

class EpisodeDetailsActivity : AppCompatActivity() {
    private lateinit var ivBackdrop: ImageView
    private lateinit var btnBackSearch: Button
    private lateinit var btnWatchNotWatched: Button
    private lateinit var tvOverview: TextView
    private lateinit var tvSerieTitle: TextView
    private lateinit var tvEpisodeTitle: TextView
    private lateinit var tvEpisodeNumber: TextView
    private lateinit var tvReleaseDate: TextView
    private lateinit var tvRuntime: TextView
    private lateinit var rvCast: RecyclerView
    private var serieId: Int = 0
    private var seasonNumber: Int = 0
    private var episodeNumber: Int = 0
    private var serieName: String? = null
    private lateinit var castAdapter: CastAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode_details)

        val intent = intent
        serieId = intent.getIntExtra("serieId", -1)
        seasonNumber = intent.getIntExtra("seasonNumber", -1)
        episodeNumber = intent.getIntExtra("episodeNumber", -1)
        serieName = intent.getStringExtra("serieName")

        tvOverview = findViewById(R.id.tv_episode_details_overview)
        ivBackdrop = findViewById(R.id.iv_episode_details_backdrop)
        btnBackSearch = findViewById(R.id.iv_arrow_back_episode_details)
        btnWatchNotWatched = findViewById(R.id.btn_watch_watched)
        tvSerieTitle = findViewById(R.id.tv_episode_details_serie_title)
        tvEpisodeTitle = findViewById(R.id.tv_episode_details_episode_title)
        tvEpisodeNumber = findViewById(R.id.tv_episode_details_n_episode)
        tvReleaseDate = findViewById(R.id.tv_serie_details_release_date)
        tvRuntime = findViewById(R.id.tv_serie_details_origin_country)

        castAdapter = CastAdapter(this, emptyList())
        rvCast = findViewById(R.id.rv_cast)

        rvCast.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCast.adapter = castAdapter

        MediaRepository.getEpisodeDetails(
            serieId,
            seasonNumber,
            episodeNumber,
            onSuccess = ::updateUI,
            onError = ::onError,
        )

        btnBackSearch.setOnClickListener {
            onBackPressed()
        }

    }

    private fun updateUI(episode: EpisodeDetails){
        episode.posterPath?.let {
            Glide.with(this)
                .load("https://image.tmdb.org/t/p/w500$it")
                .placeholder(R.drawable.movie)
                .into(ivBackdrop)
        }

        tvSerieTitle.text = serieName

        tvEpisodeNumber.text = "S${episode.season_number} | E${episode.episode_number}"

        tvEpisodeTitle.text = episode.title

        tvOverview.text = episode.overview

        tvReleaseDate.text = episode.air_date

        tvRuntime.text = if (episode.runtime >= 60) {
            val hours = episode.runtime / 60
            val minutes = episode.runtime % 60
            "${hours}h ${minutes}m"
        } else {
            "${episode.runtime}m"
        }

        castAdapter.updateMedia(episode.stars)


        setButtonDrawable(episode, btnWatchNotWatched, this)


        btnWatchNotWatched.setOnClickListener{
            FirebaseInteraction.checkSerieExistanceInFollowing(
                serieId) { exists ->
                if (exists) {
                    Log.d("FirebaseCheck", "Serie exists: ${episode.serieId}")
                    // se esiste già devo mettere a true e in caso fare l'update di nextToSee
                    FirebaseInteraction.updateNextToSee(
                        serieId,
                        episode.season_number,
                        episode.episode_number
                    ) {
                        // Dopo aver aggiornato, aggiorna il drawable del pulsante
                        setButtonDrawable(episode, btnWatchNotWatched, this)
                    }

                } else {
                    // se non esiste devo seguirla e mettere a true l'episodio (mediaItem.seasonNumber, mediaItem.episodeNumber)
                    // e controllare nextToSee
                    FirebaseInteraction.addSerieToFollowing(serieId) {
                        FirebaseInteraction.updateNextToSee(
                            serieId,
                            episode.season_number,
                            episode.episode_number
                        ) {
                            // Dopo aver aggiornato, aggiorna il drawable del pulsante
                            setButtonDrawable(episode, btnWatchNotWatched, this)
                        }
                    }
                }
            }
        }
    }

    private fun setButtonDrawable(mediaItem: EpisodeDetails, btnCheck: Button, context: Context) {
        FirebaseInteraction.checkEpisodeValue(
            serieId,
            mediaItem.season_number,
            mediaItem.episode_number
        ) {isTrue ->
            if (isTrue == true) {
                btnCheck.background = ContextCompat.getDrawable(context, R.drawable.filledcheck)
            } else {
                btnCheck.background = ContextCompat.getDrawable(context, R.drawable.check)
            }
        }
    }

    private fun onError(){
        Log.e("EpisodeDetailsActivity", "Something went wrong")
    }
}