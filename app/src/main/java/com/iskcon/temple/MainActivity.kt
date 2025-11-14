package com.iskcon.temple

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private var currentSelectedTab = R.id.nav_home_custom

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        setupCustomBottomNavigation()
    }

    private fun setupCustomBottomNavigation() {
        val navHome = findViewById<LinearLayout>(R.id.nav_home_custom)
        val navDarshan = findViewById<LinearLayout>(R.id.nav_darshan_custom)
        val navSchedule = findViewById<LinearLayout>(R.id.nav_schedule_custom)
        val navServices = findViewById<LinearLayout>(R.id.nav_services_custom)
        val navMore = findViewById<LinearLayout>(R.id.nav_more_custom)

        navHome.setOnClickListener {
            selectTab(R.id.nav_home_custom)
            clearBackStack()
            loadFragment(HomeFragment())
        }

        navDarshan.setOnClickListener {
            selectTab(R.id.nav_darshan_custom)
            clearBackStack()
            loadFragment(DarshanFragment())
        }

        navSchedule.setOnClickListener {
            selectTab(R.id.nav_schedule_custom)
            clearBackStack()
            loadFragment(ScheduleFragment())
        }

        navServices.setOnClickListener {
            selectTab(R.id.nav_services_custom)
            clearBackStack()
            loadFragment(ServicesFragment())
        }

        navMore.setOnClickListener {
            selectTab(R.id.nav_more_custom)
            clearBackStack()
            loadFragment(MoreFragment())
        }

        selectTab(R.id.nav_home_custom)
    }

    private fun selectTab(tabId: Int) {
        updateTabColor(R.id.nav_home_custom, false)
        updateTabColor(R.id.nav_darshan_custom, false)
        updateTabColor(R.id.nav_schedule_custom, false)
        updateTabColor(R.id.nav_services_custom, false)
        updateTabColor(R.id.nav_more_custom, false)

        updateTabColor(tabId, true)
        currentSelectedTab = tabId
    }

    private fun updateTabColor(tabId: Int, isSelected: Boolean) {
        val tab = findViewById<LinearLayout>(tabId)
        val textView = when (tabId) {
            R.id.nav_home_custom -> findViewById<TextView>(R.id.text_home)
            R.id.nav_darshan_custom -> findViewById<TextView>(R.id.text_darshan)
            R.id.nav_schedule_custom -> findViewById<TextView>(R.id.text_schedule)
            R.id.nav_services_custom -> findViewById<TextView>(R.id.text_services)
            R.id.nav_more_custom -> findViewById<TextView>(R.id.text_more)
            else -> null
        }

        if (isSelected) {
            textView?.setTextColor(ContextCompat.getColor(this, R.color.bhagwa_saffron))
            textView?.typeface = android.graphics.Typeface.DEFAULT_BOLD
        } else {
            textView?.setTextColor(ContextCompat.getColor(this, R.color.gray_text))
            textView?.typeface = android.graphics.Typeface.DEFAULT
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun clearBackStack() {
        for (i in 0 until supportFragmentManager.backStackEntryCount) {
            supportFragmentManager.popBackStack()
        }
    }

    override fun onBackPressed() {
        // If there's a backstack (like from Gallery), pop it
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        }
        // If not on home, go to home
        else if (currentSelectedTab != R.id.nav_home_custom) {
            findViewById<LinearLayout>(R.id.nav_home_custom)?.performClick()
        }
        // If on home, exit app
        else {
            super.onBackPressed()
        }
    }
}