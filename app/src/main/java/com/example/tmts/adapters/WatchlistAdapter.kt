package com.example.tmts.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.R
import com.example.tmts.beans.results.WatchlistDetailsResult
import com.example.tmts.interfaces.OnShowDetailsClickListener

class WatchlistAdapter(
    private val context: Context,
    private val showDetailsClickListener: OnShowDetailsClickListener,
    private var watchlists: ArrayList<WatchlistDetailsResult> = ArrayList()
) : RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder>() {

    inner class WatchlistViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val tvWatchlistName: TextView = itemView.findViewById(R.id.tv_watchlist_name)
        private val rvWatchlist: RecyclerView = itemView.findViewById(R.id.rv_watchlist_contents)

        fun bind(watchlist: WatchlistDetailsResult, showDetailsClickListener: OnShowDetailsClickListener) {
            tvWatchlistName.text = watchlist.name
            val mediaAdapter = WatchlistShowAdapter(
                context,
                showDetailsClickListener,
                ArrayList(watchlist.watchlist),
                66,
                100
            )
            rvWatchlist.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            rvWatchlist.adapter = mediaAdapter
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WatchlistViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.add_to_watchlist_item, parent, false)
        return WatchlistViewHolder(view)
    }

    override fun getItemCount(): Int {
        return watchlists.size
    }

    override fun onBindViewHolder(holder: WatchlistViewHolder, position: Int) {
        holder.bind(watchlists[position], showDetailsClickListener)
    }

    fun updateWatchlists(updatedWatchlists: ArrayList<WatchlistDetailsResult>) {
        watchlists = updatedWatchlists
        notifyDataSetChanged()
    }

}