package com.example.tmts.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.OnCheckButtonClickListener
import com.example.tmts.R
import com.example.tmts.activities.EpisodeDetailsActivity
import com.example.tmts.activities.SerieDetailsActivity
import com.example.tmts.beans.EpisodeDetails
import com.example.tmts.beans.MovieDetails
import com.example.tmts.beans.SerieDetails

class HomeSerieAdapter(
    private val context: Context,
    private var mediaItems: List<EpisodeDetails>,
    private val clickListener: OnCheckButtonClickListener
) : RecyclerView.Adapter<HomeSerieAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.home_serie_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaItem = mediaItems[position]
        holder.bind(mediaItem, clickListener)
    }

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    fun updateEpisodes(mediaItems: List<EpisodeDetails>) {
        this.mediaItems = mediaItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewBackdrop: ImageView = itemView.findViewById(R.id.iv_home_serie_backdrop)
        private val textViewSerieTitle: TextView = itemView.findViewById(R.id.tv_home_serie_title)
        private val textViewEpisodeNumber: TextView = itemView.findViewById(R.id.tv_home_serie_episode_number)
        private val textViewEpisodeName: TextView = itemView.findViewById(R.id.tv_home_serie_episode_title)
        private val buttonCheck: Button = itemView.findViewById(R.id.btn_home_serie_check)
        private val llSerieInfo: LinearLayout = itemView.findViewById(R.id.ll_serie_info)

        fun bind(episode: EpisodeDetails, clickListener: OnCheckButtonClickListener) {
            Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500${episode.seriePosterPath}")
                .placeholder(R.drawable.movie)
                .into(imageViewBackdrop)

            textViewSerieTitle.text = episode.serieName ?: ""

            textViewEpisodeNumber.text = "S${episode.season_number} | E${episode.episode_number}"

            textViewEpisodeName.text = episode.title

            llSerieInfo.setOnClickListener {
                val intent = Intent(context, EpisodeDetailsActivity::class.java)
                intent.putExtra("serieId", episode.serieId)
                intent.putExtra("seasonNumber", episode.season_number)
                intent.putExtra("episodeNumber", episode.episode_number)
                intent.putExtra("serieName", episode.serieName)
                context.startActivity(intent)
            }

            buttonCheck.setOnClickListener {
                val newNextToSee = "${episode.season_number}_${episode.episode_number}"
                clickListener.onCheckButtonClicked(episode.serieId.toString(), episode.serieName!!)
            }
        }
    }
}
