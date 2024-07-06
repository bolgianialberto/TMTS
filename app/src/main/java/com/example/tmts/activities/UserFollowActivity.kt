package com.example.tmts.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.tmts.R
import com.example.tmts.fragments.UserFollowersFragment
import com.example.tmts.fragments.UserFollowingFragment

class UserFollowActivity : AppCompatActivity() {

    private lateinit var bttBack: Button
    private lateinit var edtFollowSearch: EditText
    private lateinit var bttFollowers: Button
    private lateinit var bttFollowing: Button

    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_follow_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bttBack = findViewById(R.id.btt_arrow_back_search_follow)
        edtFollowSearch = findViewById(R.id.edt_search_follow)
        bttFollowers = findViewById(R.id.btt_followers)
        bttFollowing = findViewById(R.id.btt_following)

        bttBack.setOnClickListener {
            finish()
        }

        edtFollowSearch.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                performSearch(query)
            }

        })

        bttFollowers.setOnClickListener {
            toggleButtonColor(bttFollowers)
            replaceFragment(UserFollowersFragment())
        }

        bttFollowing.setOnClickListener {
            toggleButtonColor(bttFollowing)
            replaceFragment(UserFollowingFragment())
        }

        toggleButtonColor(bttFollowers)
        replaceFragment(UserFollowersFragment())

    }

    private fun toggleButtonColor(button: Button) {
        if (button.isSelected) return

        bttFollowers.isSelected = false
        bttFollowing.isSelected = false

        button.isSelected = true
        button.setBackgroundColor(ContextCompat.getColor(this, R.color.selectedColor))
        button.setTextColor(ContextCompat.getColor(this, R.color.black))

        val nonClickedButton = if (button.id == R.id.btn_film) bttFollowers else bttFollowing
        nonClickedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.unselectedColor))
        nonClickedButton.setTextColor(ContextCompat.getColor(this, R.color.gray))
    }

    private fun replaceFragment(fragment: Fragment){
        currentFragment = fragment
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fl_search_follow_fragment, fragment)
        fragmentTransaction.runOnCommit {
            val query = edtFollowSearch.text.toString().trim()
            performSearch(query)
        }
        fragmentTransaction.commit()
    }

    private fun performSearch(query: String) {
        if (currentFragment is UserFollowersFragment) {
            (currentFragment as UserFollowersFragment).performFilter(query)
        }
        if (currentFragment is UserFollowingFragment) {
            (currentFragment as UserFollowingFragment).performFilter(query)
        }
    }
}