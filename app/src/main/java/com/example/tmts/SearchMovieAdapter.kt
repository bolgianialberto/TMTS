import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.Media
import com.example.tmts.R

class SearchMovieAdapter(private val context: Context, private var movies: List<Media>) :
    RecyclerView.Adapter<SearchMovieAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.search_movie_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movie = movies[position]
        holder.bind(movie)
    }

    override fun getItemCount(): Int {
        return movies.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageViewPoster: ImageView = itemView.findViewById(R.id.iv_search)
        private val textViewTitle: TextView = itemView.findViewById(R.id.tv_search)

        fun bind(movie: Media) {
            textViewTitle.text = movie.title
            Glide.with(context)
                .load("https://image.tmdb.org/t/p/w500${movie.posterPath}")
                .placeholder(R.drawable.movie)
                .into(imageViewPoster)
        }
    }

    fun updateMovies(newMovies: List<Media>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}
