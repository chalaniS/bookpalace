package com.bookpalace.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bookpalace.app.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Default fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, BooksFragment())
            .commit()

        bottomNav.setOnItemSelectedListener { item ->

            val selectedFragment = when (item.itemId) {

                R.id.nav_home -> BooksFragment()
                R.id.nav_borrowbooklist -> BooksFragment()
                R.id.nav_rec -> BooksFragment()
                R.id.nav_profile -> BooksFragment()

                else -> BooksFragment()
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, selectedFragment)
                .commit()

            true
        }
    }
}