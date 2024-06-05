package com.example.tmts.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.tmts.MediaDetaisActivity
import com.example.tmts.R
import com.example.tmts.adapters.ExploreAdapter
import com.example.tmts.adapters.MediaAdapter
import com.example.tmts.beans.User

class ExploreFragment : Fragment() {
    private lateinit var exploreAdapter: ExploreAdapter
    private lateinit var bttExploreSearch: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)
        bttExploreSearch = view.findViewById(R.id.btt_search_explore)
        exploreAdapter = ExploreAdapter(requireContext(), emptyList()) { exploreItem ->
            val intent = Intent(requireContext(), User::class.java)
            intent.putExtra("userId", exploreItem.id)
            startActivity(intent)
        }
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }
}