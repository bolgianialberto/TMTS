package com.example.tmts.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.tmts.R
import com.example.tmts.beans.viewmodels.UsersViewModel

class ExploreFragment : Fragment() {

    private val exploreShowsSectionSelected = 1
    private val chatSectionSelected = 2
    private val userSearchSectionSelected = 3
    private var sectionSelected = -1

    private val sharedViewModel: UsersViewModel by activityViewModels()

    // private var standardHeight = -1

    private lateinit var exploreLayoutView: View
    private lateinit var llTopNavButtons: LinearLayout
    private lateinit var edtExplore: EditText
    private lateinit var bttExplore: Button
    private lateinit var bttChat: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_explore, container, false)

        exploreLayoutView = view.findViewById(R.id.main_explore_layout)
        llTopNavButtons = view.findViewById(R.id.ll_top_navigation_view)

        bttExplore = view.findViewById(R.id.btt_explore)
        bttChat = view.findViewById(R.id.btt_chat)

        toggleButtonColor(bttExplore)

        bttExplore.setOnClickListener {
            sectionSelected = exploreShowsSectionSelected
            edtExplore.text.clear()
            toggleButtonColor(bttExplore)
            replaceFragment(ExploreShowsFragment())}
        bttChat.setOnClickListener {
            sectionSelected = chatSectionSelected
            edtExplore.text.clear()
            toggleButtonColor(bttChat)
            replaceFragment(ChatListFragment())
        }



        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        edtExplore = view.findViewById(R.id.edt_search_explore)
        edtExplore.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (sectionSelected == userSearchSectionSelected && edtExplore.text.toString().isEmpty()) {
                    sectionSelected = exploreShowsSectionSelected
                    llTopNavButtons.visibility = View.VISIBLE
                    replaceFragment(ExploreShowsFragment())
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (sectionSelected == exploreShowsSectionSelected && edtExplore.text.toString().isEmpty()) {
                    sectionSelected = userSearchSectionSelected
                    llTopNavButtons.visibility = View.GONE
                    replaceFragment(UserSearchFragment())
                }
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sharedViewModel.setText(s.toString())
            }
        })

        sectionSelected = exploreShowsSectionSelected
        replaceFragment(ExploreShowsFragment())
    }

    private fun toggleButtonColor(button: Button) {
        if (button.isSelected) return

        bttExplore.isSelected = false
        bttChat.isSelected = false

        button.isSelected = true
        button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.selectedColor))
        button.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        val nonClickedButton = if (button.id == R.id.btt_chat) bttExplore else bttChat
        nonClickedButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.unselectedColor))
        nonClickedButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray))
    }

    private fun replaceFragment(fragment: Fragment) {

        val fragmentManager = childFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fl_explore_fragment, fragment)
        fragmentTransaction.commit()
    }



}