package com.example.tmts.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.tmts.R
import com.example.tmts.fragments.AccountFragment
import com.example.tmts.fragments.ExploreFragment
import com.example.tmts.fragments.HomeFragment
import com.example.tmts.fragments.SearchFragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        val searchFragment = SearchFragment()
        val homeFragment = HomeFragment()
        val accountFragment = AccountFragment()
        val exploreFragment = ExploreFragment()

        setCurrentFragment(homeFragment)

        bottomNavigationView.setOnNavigationItemSelectedListener {
            when(it.itemId){
                R.id.search -> setCurrentFragment(searchFragment)
                R.id.home -> setCurrentFragment(homeFragment)
                R.id.account -> setCurrentFragment(accountFragment)
                R.id.explore -> setCurrentFragment(exploreFragment)
            }
            true
        }
    }

    private fun setCurrentFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment,fragment)
            commit()
        }
}