package com.bookpalace.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bookpalace.app.R
import com.bookpalace.app.ui.librarian.LibrarianMainFragment
import com.bookpalace.app.ui.librarian.SendNotificationFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
                R.id.nav_rec -> LibrarianMainFragment() // Books list
                R.id.nav_borrowbooklist -> SendNotificationFragment() // Notification (Mapping it here for now)
                R.id.nav_profile -> LibrarianMainFragment() // Placeholder for Profile
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
