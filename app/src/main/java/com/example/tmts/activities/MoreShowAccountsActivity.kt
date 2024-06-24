package com.example.tmts.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.FirebaseInteraction
import com.example.tmts.MediaRepository
import com.example.tmts.R
import com.example.tmts.adapters.MoreShowAccountsAdapter
import com.example.tmts.beans.MovieDetails
import com.example.tmts.beans.SerieDetails
import com.example.tmts.interfaces.OnChatClickListener
import kotlin.math.min

class MoreShowAccountsActivity : AppCompatActivity(), OnChatClickListener {

    private val MAX_USERS: Int = 30
    private var movieDetails: MovieDetails? = null
    private var serieDetails: SerieDetails? = null
    private lateinit var followers: List<String>
    private lateinit var rvAccounts: RecyclerView
    private lateinit var moreShowAccountsAdapter: MoreShowAccountsAdapter
    private lateinit var ivShowImage: ImageView
    private lateinit var tvShowTitle: TextView
    private lateinit var tvShowOverview: TextView
    private lateinit var bttBack: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_more_show_accounts)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val showType = intent.getStringExtra("showType")!!
        val showId = intent.getStringExtra("showId")!!
        followers = intent.getStringArrayListExtra("retrievedFollowers")!!
        val loadedFollowers = intent.getStringArrayListExtra("loadedUsers")
        moreShowAccountsAdapter = MoreShowAccountsAdapter(this, ArrayList(), this)
        rvAccounts = findViewById(R.id.rv_more_show_accounts)
        rvAccounts.layoutManager = LinearLayoutManager(this)
        rvAccounts.adapter = moreShowAccountsAdapter
        ivShowImage = findViewById(R.id.iv_more_account_show_backdrop)
        tvShowTitle = findViewById(R.id.tv_more_account_show_title)
        tvShowOverview = findViewById(R.id.tv_more_account_show_description)
        bttBack = findViewById(R.id.btt_arrow_back_explore)
        bttBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }
        loadData(showType, showId)
    }

    private fun loadData(showType: String, showId: String) {
        when (showType) {
            "MOV" -> {
                MediaRepository.getMovieDetails(
                    showId.toInt(),
                    onSuccess = ::onMovieDetailsFetched,
                    onError = FirebaseInteraction::onError
                )
            }
            "SER" -> {
                MediaRepository.getSerieDetails(
                    showId.toInt(),
                    onSuccess = ::onSerieDetailsFetched,
                    onError = FirebaseInteraction::onError
                )
            }
        }
    }

    private fun onSerieDetailsFetched(serieDetailsResult: SerieDetails) {
        serieDetails = serieDetailsResult
        Glide.with(this)
            .load("https://image.tmdb.org/t/p/w500${serieDetails!!.posterPath}")
            .placeholder(R.drawable.movie)
            .into(ivShowImage)
        tvShowTitle.text = serieDetails!!.title
        tvShowOverview.text = serieDetails!!.overview
        fetchUsers()
    }

    private fun onMovieDetailsFetched(movieDetailsResult: MovieDetails) {
        movieDetails = movieDetailsResult
        Glide.with(this)
            .load("https://image.tmdb.org/t/p/w500${movieDetails!!.posterPath}")
            .placeholder(R.drawable.movie)
            .into(ivShowImage)
        tvShowTitle.text = movieDetails!!.title
        tvShowOverview.text = movieDetails!!.overview
        fetchUsers()
    }

    private fun fetchUsers() {
        val nUsersShowed = min(MAX_USERS, followers.size)
        for (i in 0..<nUsersShowed) {
            val it = followers[i]
            FirebaseInteraction.getUserInfo(it,
                onSuccess = { user ->
                    moreShowAccountsAdapter.updateUsers(user)
                },
                onFailure = {
                    Log.e("ERROR", "Something went wrong")
                }
            )
        }
    }

    override fun onChatClickListener(userId: String) {
        Log.d("CHAT LOG", "chat w/ $userId")
    }


}