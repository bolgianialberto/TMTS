import android.content.Context
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
import com.example.tmts.beans.results.ShowDetailsResult
import com.example.tmts.interfaces.OnMoreAccountClickListener
import com.example.tmts.interfaces.OnShowDetailsClickListener
import com.example.tmts.interfaces.OnUserClickListener
import java.lang.ref.WeakReference

class ExploreShowsAdapter(
    context: Context,
    private val mediaItems: ArrayList<ShowDetailsResult> = ArrayList(),
    private val moreAccountsClickListener: OnMoreAccountClickListener,
    private val showDetailsClickListener: OnShowDetailsClickListener,
    private val userClickListener: OnUserClickListener
) : RecyclerView.Adapter<ExploreShowsAdapter.ExploreMovieViewHolder>() {

    private val contextRef: WeakReference<Context> = WeakReference(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreMovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.explore_movie_item, parent, false)
        return ExploreMovieViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mediaItems.size
    }

    override fun onBindViewHolder(holder: ExploreMovieViewHolder, position: Int) {
        val mediaItem = mediaItems[position]
        holder.bind(mediaItem)
    }

    fun updateShows(showDetailsResult: ShowDetailsResult) {
        val showPair = mediaItems.find {
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
        notifyDataSetChanged()
    }

    inner class ExploreMovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val N_USERS: Int = 4

        private val tvTitle: TextView = itemView.findViewById(R.id.tv_explore_show_title)
        private val ivMovie: ImageView = itemView.findViewById(R.id.iv_explore_movie_backdrop)
        private val cvsUser = Array<CardView?>(N_USERS) { null }
        private val ivsUser = Array<ImageView?>(N_USERS) { null }
        private val tvsUser = Array<TextView?>(N_USERS) { null }

        init {
            initObjects()
        }

        private fun initObjects() {
            for (i in 0 until N_USERS) {
                cvsUser[i] = itemView.findViewById(R.id.cv_explore_show_user_1 + i)
                ivsUser[i] = itemView.findViewById(R.id.iv_explore_show_user_1 + i)
                tvsUser[i] = itemView.findViewById(R.id.tv_explore_show_user_1 + i)
                cvsUser[i]?.visibility = View.GONE
            }
        }

        fun bind(showInfo: ShowDetailsResult) {
            val context = contextRef.get()
            if (context != null) {
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
                    showDetailsClickListener.onShowDetailsClickListener(showInfo)
                }
                tvTitle.setOnClickListener {
                    showDetailsClickListener.onShowDetailsClickListener(showInfo)
                }

                for (index in 0 until N_USERS) {
                    if (index < showInfo.loadedUsers.size) {
                        val user = showInfo.loadedUsers[index]
                        FirebaseInteraction.getUserProfileImageRef(
                            user.id,
                            onSuccess = { snapshot ->
                                snapshot.downloadUrl.addOnSuccessListener { uri ->
                                    Glide.with(context)
                                        .load(uri)
                                        .placeholder(R.drawable.account)
                                        .into(ivsUser[index]!!)
                                }.addOnFailureListener {
                                    Glide.with(context)
                                        .load(R.drawable.account)
                                        .into(ivsUser[index]!!)
                                }
                            },
                            onFailure = {
                                Glide.with(context)
                                    .load(R.drawable.account)
                                    .into(ivsUser[index]!!)
                            }
                        )

                        tvsUser[index]?.text = user.name
                        cvsUser[index]?.setOnClickListener {
                            userClickListener.onUserClickListener(user)
                        }
                        cvsUser[index]?.visibility = View.VISIBLE
                    } else {
                        cvsUser[index]?.visibility = View.GONE
                    }
                }

                if (showInfo.retrievedUsers.size > N_USERS) {
                    Glide.with(context)
                        .load(R.drawable.add)
                        .into(ivsUser[N_USERS - 1]!!)
                    ivsUser[N_USERS - 1]?.setOnClickListener {
                        moreAccountsClickListener.onMoreAccountClickListener(showInfo)
                    }
                    tvsUser[N_USERS - 1]?.text = "Explore more"
                    tvsUser[N_USERS - 1]?.setOnClickListener {
                        moreAccountsClickListener.onMoreAccountClickListener(showInfo)
                    }
                    cvsUser[N_USERS - 1]?.visibility = View.VISIBLE
                } else {
                    cvsUser[N_USERS - 1]?.visibility = View.GONE
                }
            }
        }
    }
}
