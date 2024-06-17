package com.example.tmts.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R
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
    private var mediaItems: List<EpisodeDetails>
) : RecyclerView.Adapter<EpisodeAdapter.ViewHolder>() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference
    private lateinit var currentUser: FirebaseUser
    private lateinit var followingSeriesRef: DatabaseReference
    private lateinit var serieRef: DatabaseReference
    private lateinit var episodeRef: DatabaseReference
    private lateinit var tvEpisodeTitle: TextView
    private lateinit var btnCheck: Button

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.episode_item, parent, false)

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
        if (currentUser == null) {
            Log.e("FirebaseCheck", "User is not authenticated")
            return ViewHolder(view)
        }

        mDbRef = FirebaseDatabase.getInstance().getReference()
        followingSeriesRef = mDbRef.child("users").child(currentUser.uid).child("following_series")

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

        fun bind(mediaItem: EpisodeDetails) {
            mediaItem.serieId = serieId

            Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500${mediaItem.posterPath}")
                .placeholder(R.drawable.movie)
                .into(ivPoster)

            tvEpisodeNumber.text = "S${mediaItem.season_number} | E${mediaItem.episode_number}"
            tvEpisodeTitle.text = mediaItem.title

            Log.d("FirebaseCheck", "Binding episode: ${mediaItem.episode_number}")

            // se già seguo la serie allora aggiorno il colore del check per gli episodi già visti
            FirebaseInteraction.checkSerieExistance(
                serieId,) { exists ->
                if (exists) {
                    Log.d("FirebaseCheck", "Serie exists: ${mediaItem.serieId}")
                    setButtonDrawable(mediaItem, btnCheck, context)
                } else {
                    Log.d("FirebaseCheck", "Serie does not exist: ${mediaItem.serieId}")
                }
            }

            // se clicco sul check devo iniziare a seguire la serie se prima non la seguivo
            btnCheck.setOnClickListener {
                FirebaseInteraction.checkSerieExistance(
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

        val serieRef = followingSeriesRef.child(mediaItem.serieId.toString())
        val episodeRef = serieRef
            .child("seasons")
            .child(mediaItem.season_number.toString())
            .child("episodes")
            .child(mediaItem.episode_number.toString())

        episodeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(episodeSnapshot: DataSnapshot) {
                if (episodeSnapshot.exists()) {
                    val episodeValue = episodeSnapshot.getValue(Boolean::class.java)
                    Log.d("FirebaseCheck", "Episode value: $episodeValue")
                    if (episodeValue == true) {
                    } else {
                    }
                } else {
                    Log.d("FirebaseCheck", "Episode does not exist")
                    btnCheck.background = ContextCompat.getDrawable(context, R.drawable.check)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FirebaseCheck", "loadEpisode:onCancelled", databaseError.toException())
            }
        })
    }
}
