package com.example.tmts.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.example.tmts.MediaDetaisActivity
import com.example.tmts.R
import com.example.tmts.adapters.ExploreAdapter

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
            val intent = Intent(requireContext(), MediaDetaisActivity::class.java)
            intent.putExtra("userId", exploreItem.id)
            startActivity(intent)
        }
        return inflater.inflate(R.layout.fragment_explore, container, false)
    }
}