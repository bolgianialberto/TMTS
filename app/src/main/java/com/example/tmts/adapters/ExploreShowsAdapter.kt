package com.example.tmts.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R
import com.example.tmts.activities.MovieDetailsActivity
import com.example.tmts.activities.SerieDetailsActivity
import com.example.tmts.beans.results.ShowDetailsResult
import com.example.tmts.interfaces.OnMoreAccountClickListener

class ExploreShowsAdapter (
    private val context: Context,
    private val mediaItems: ArrayList<ShowDetailsResult> = ArrayList(),
    private val moreAccountsClickListener: OnMoreAccountClickListener
) : RecyclerView.Adapter<ExploreShowsAdapter.ExploreMovieViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreMovieViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.explore_movie_item, parent, false)
        return ExploreMovieViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    override fun onBindViewHolder(holder: ExploreShowsAdapter.ExploreMovieViewHolder, position: Int) {
        val mediaItem = mediaItems[position]
        holder.bind(mediaItem)
    }

    fun updateShows(showDetailsResult: ShowDetailsResult) {
        val showPair = mediaItems
            .find {
                showDetailsResult.showTypeId == it.showTypeId &&
                showDetailsResult.movieDetails?.id == it.movieDetails?.id &&
                showDetailsResult.serieDetails?.id == it.serieDetails?.id
            }
        if (showPair == null) {
            mediaItems.add(showDetailsResult)
            notifyItemInserted(mediaItems.size - 1)
        } else {
            showPair.loadedUsers.shuffle()
            notifyItemChanged(mediaItems.indexOf(showPair))
        }
    }

    fun clearShows() {
        mediaItems.clear()
        notifyItemRangeRemoved(0, mediaItems.size)
    }

    inner class ExploreMovieViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val N_USERS: Int = 4

        private val tvTitle: TextView = itemView.findViewById(R.id.tv_explore_show_title)
        private val ivMovie: ImageView = itemView.findViewById(R.id.iv_explore_movie_backdrop)
        private val cvsUser = arrayOfNulls<CardView>(N_USERS)
        private val ivsUser = arrayOfNulls<ImageView>(N_USERS)
        private val tvsUser = arrayOfNulls<TextView>(N_USERS)

        private fun initObjects() {
            cvsUser[0] = itemView.findViewById(R.id.cv_explore_show_user_1)
            cvsUser[1] = itemView.findViewById(R.id.cv_explore_show_user_2)
            cvsUser[2] = itemView.findViewById(R.id.cv_explore_show_user_3)
            cvsUser[3] = itemView.findViewById(R.id.cv_explore_show_user_4)
            tvsUser[0] = itemView.findViewById(R.id.tv_explore_show_user_1)
            tvsUser[1] = itemView.findViewById(R.id.tv_explore_show_user_2)
            tvsUser[2] = itemView.findViewById(R.id.tv_explore_show_user_3)
            tvsUser[3] = itemView.findViewById(R.id.tv_explore_show_user_4)
            ivsUser[0] = itemView.findViewById(R.id.iv_explore_show_user_1)
            ivsUser[1] = itemView.findViewById(R.id.iv_explore_show_user_2)
            ivsUser[2] = itemView.findViewById(R.id.iv_explore_show_user_3)
            ivsUser[3] = itemView.findViewById(R.id.iv_explore_show_user_4)
            for (cv: CardView? in cvsUser) {
                if (cv != null) {
                    cv.visibility = View.GONE
                }
            }
        }

        fun bind(showInfo: ShowDetailsResult) {
            initObjects()
            when (showInfo.showTypeId) {
                "MOV" -> {
                    val movieInfo = showInfo.movieDetails!!
                    tvTitle.text = movieInfo.title
                    Glide.with(context)
                        .load("https://image.tmdb.org/t/p/w500${movieInfo.posterPath}")
                        .placeholder(R.drawable.movie)
                        .into(ivMovie)
                }
                "SER" -> {
                    val serieInfo = showInfo.serieDetails!!
                    tvTitle.text = serieInfo.title
                    Glide.with(context)
                        .load("https://image.tmdb.org/t/p/w500${serieInfo.posterPath}")
                        .placeholder(R.drawable.movie)
                        .into(ivMovie)
                }
            }
            ivMovie.setOnClickListener {
                var showIntent: Intent? = null
                when (showInfo.showTypeId) {
                    "MOV" -> {
                        showIntent = Intent(context, MovieDetailsActivity::class.java)
                        showIntent.putExtra("movieId", showInfo.movieDetails!!.id.toInt())
                    }
                    "SER" -> {
                        showIntent = Intent(context, SerieDetailsActivity::class.java)
                        showIntent.putExtra("serieId", showInfo.serieDetails!!.id.toInt())
                    }
                }
                context.startActivity(showIntent!!)
            }
            for (index: Int in showInfo.loadedUsers.indices) {
                val user = showInfo.loadedUsers[index]
                FirebaseInteraction.getUserProfileImageRef(
                    user.id,
                    onSuccess = {
                        it.downloadUrl.addOnSuccessListener { uri ->
                            Glide.with(context)
                                .load(uri)
                                .into(ivsUser[index]!!)
                        }.addOnFailureListener{ exc ->
                            Log.e("STORAGE DOWNLOAD", "Error: $exc")
                        }
                    }, onFailure = {
                        Log.e("IMAGE ERROR", it)
                    })

                tvsUser[index]!!.text = user.name
                cvsUser[index]!!.visibility = View.VISIBLE
            }
            if (showInfo.retrievedUsers.size > 4) {
                Glide.with(context)
                    .load(R.drawable.add)
                    .into(ivsUser[N_USERS-1]!!)
                ivsUser[N_USERS-1]!!.setOnClickListener {
                    moreAccountsClickListener.onMoreAccountClickListener(showInfo)
                }
                tvsUser[N_USERS-1]!!.text = "Explore more"
                tvsUser[N_USERS-1]!!.setOnClickListener {
                    moreAccountsClickListener.onMoreAccountClickListener(showInfo)
                }
                cvsUser[N_USERS-1]!!.visibility = View.VISIBLE
            }
        }
    }

}