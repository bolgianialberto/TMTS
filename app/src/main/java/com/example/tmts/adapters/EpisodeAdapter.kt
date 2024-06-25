package com.example.tmts.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R
import com.example.tmts.Utils
import com.example.tmts.activities.EpisodeDetailsActivity
import com.example.tmts.beans.EpisodeDetails
import com.example.tmts.beans.Network
import com.example.tmts.beans.SeasonDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EpisodeAdapter(
    private val context: Context,
    private val serieId: Int,
    private val serieName: String?,
    private var mediaItems: List<EpisodeDetails>,
    private val onSwipeRight: (() -> Unit)?
) : RecyclerView.Adapter<EpisodeAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.episode_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mediaItem = mediaItems[position]
        holder.bind(mediaItem)
    }

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    fun updateMedia(mediaItems: List<EpisodeDetails>) {
        this.mediaItems = mediaItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPoster: ImageView = itemView.findViewById(R.id.iv_single_episode_poster)
        private val tvEpisodeNumber: TextView = itemView.findViewById(R.id.tv_episode_number)
        private val tvEpisodeTitle: TextView = itemView.findViewById(R.id.tv_episode_title)
        private val btnCheck: Button = itemView.findViewById(R.id.btn_episode_check)
        private val llEpisodeInfo: LinearLayout = itemView.findViewById(R.id.ll_episode_info)

        init{
            itemView.setOnTouchListener(Utils.detectSwipe(context){ direction ->
                when (direction) {
                    "MOVE_RIGHT" -> {
                        onSwipeRight?.let { it() }
                    }
                }
            })
        }
        fun bind(mediaItem: EpisodeDetails) {
            mediaItem.serieId = serieId
            mediaItem.serieName = serieName

            Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500${mediaItem.posterPath}")
                .placeholder(R.drawable.movie)
                .into(ivPoster)

            tvEpisodeNumber.text = "S${mediaItem.season_number} | E${mediaItem.episode_number}"
            tvEpisodeTitle.text = mediaItem.title

            Log.d("FirebaseCheck", "Binding episode: ${mediaItem.episode_number}")

            // se già seguo la serie allora aggiorno il colore del check per gli episodi già visti
            setButtonDrawable(mediaItem, btnCheck, context)

            // se clicco sul check devo iniziare a seguire la serie se prima non la seguivo
            btnCheck.setOnClickListener {
                FirebaseInteraction.checkSerieExistanceInFollowing(
                    serieId) { exists ->
                        if (exists) {
                            Log.d("FirebaseCheck", "Serie exists: ${mediaItem.serieId}")
                            // se esiste già devo mettere a true e in caso fare l'update di nextToSee
                            FirebaseInteraction.updateNextToSee(
                                serieId,
                                mediaItem.season_number,
                                mediaItem.episode_number
                            ) {
                                // Dopo aver aggiornato, aggiorna il drawable del pulsante
                                setButtonDrawable(mediaItem, btnCheck, context)
                            }

                        } else {
                            // se non esiste devo seguirla e mettere a true l'episodio (mediaItem.seasonNumber, mediaItem.episodeNumber)
                            // e controllare nextToSee
                            FirebaseInteraction.addSerieToFollowing(serieId) {
                                FirebaseInteraction.updateNextToSee(
                                    serieId,
                                    mediaItem.season_number,
                                    mediaItem.episode_number
                                ) {
                                    // Dopo aver aggiornato, aggiorna il drawable del pulsante
                                    setButtonDrawable(mediaItem, btnCheck, context)
                                }
                                FirebaseInteraction.addFollowerToSeries(serieId)
                            }
                        }
                }
            }

            llEpisodeInfo.setOnClickListener {
                val intent = Intent(context, EpisodeDetailsActivity::class.java).apply {
                    putExtra("serieId", serieId)
                    putExtra("seasonNumber", mediaItem.season_number)
                    putExtra("episodeNumber", mediaItem.episode_number)
                    putExtra("serieName", serieName)
                }
                context.startActivity(intent)
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
}
