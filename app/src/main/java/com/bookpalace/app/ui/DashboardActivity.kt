package com.bookpalace.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bookpalace.app.R
import com.bookpalace.app.ui.librarian.AddBookDetail
import com.bookpalace.app.ui.librarian.LibrarianMainFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Default Fragment
        if (savedInstanceState == null) {
            loadFragment(LibrarianMainFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_home -> LibrarianMainFragment()
                R.id.nav_rec -> LibrarianMainFragment() // Adjust as needed
                R.id.nav_borrowbooklist -> LibrarianMainFragment() // Adjust as needed
                R.id.nav_profile -> LibrarianMainFragment() // Adjust as needed
                else -> LibrarianMainFragment()
            }
            loadFragment(selectedFragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
