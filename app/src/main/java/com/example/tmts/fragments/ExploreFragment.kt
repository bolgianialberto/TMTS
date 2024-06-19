package com.example.tmts.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tmts.R

class ExploreFragment : Fragment() {
    private lateinit var bttExplore: Button
    private lateinit var bttChat: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        bttExplore = view.findViewById(R.id.btt_explore);
        bttChat = view.findViewById(R.id.btt_chat);

        toggleButtonColor(bttExplore)

        bttExplore.setOnClickListener {
            toggleButtonColor(bttExplore)
            replaceFragment(ExploreMoviesFragment())}
        bttChat.setOnClickListener {
            toggleButtonColor(bttChat)
            replaceFragment(SerieHomeFragment())
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        replaceFragment(ExploreMoviesFragment())
    }

    private fun toggleButtonColor(button: Button) {
        if (button.isSelected) return

        bttExplore.isSelected = false
        bttChat.isSelected = false

        button.isSelected = true
        button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.selectedColor))
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        val nonClickedButton = if (button.id == R.id.btn_film) bttExplore else bttChat
        nonClickedButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.unselectedColor))
        nonClickedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = parentFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fl_explore_fragment, fragment)
        fragmentTransaction.commit()
    }
}