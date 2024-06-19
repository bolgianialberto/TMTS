package com.example.tmts.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.tmts.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ExploreFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ExploreFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ExploreFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}