package com.iskcon.temple

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cloudinary.android.MediaManager
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    private var currentSelectedTab = R.id.nav_home_custom
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var notificationBadge: TextView
    private lateinit var profileImage: ImageView
    private lateinit var notificationContainer: FrameLayout
    private lateinit var accountContainer: FrameLayout
    private lateinit var appBarLayout: AppBarLayout // Add this

    // Notification permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Festival notifications enabled! 🙏", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please enable notifications for festival reminders", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize Views
        initializeToolbarViews()

        // Initialize Cloudinary
        initCloudinary()

        // Request notification permission
        requestNotificationPermission()

        // Load user profile
        loadUserProfile()

        // Setup click listeners
        setupToolbarClicks()

        // Load notification count
        loadNotificationCount()

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            showToolbar(true) // Show toolbar for home
        }

        setupCustomBottomNavigation()
    }

    private fun initializeToolbarViews() {
        appBarLayout = findViewById(R.id.app_bar_layout) // Add this
        notificationBadge = findViewById(R.id.notification_badge)
        profileImage = findViewById(R.id.toolbar_profile_image)
        notificationContainer = findViewById(R.id.notification_icon_container)
        accountContainer = findViewById(R.id.account_icon_container)
    }

    private fun setupToolbarClicks() {
        // Notification Click
        notificationContainer.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }

        // Account Click
        accountContainer.setOnClickListener {
            val intent = Intent(this, AccountActivity::class.java)
            startActivity(intent)
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
        }
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Load profile picture from Firebase or use default
            val photoUrl = currentUser.photoUrl
            if (photoUrl != null) {
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_default_profile)
                    .into(profileImage)
            }
        }
    }

    private fun loadNotificationCount() {
        val currentUser = auth.currentUser ?: return

        firestore.collection("notifications")
            .whereEqualTo("userId", currentUser.uid)
            .whereEqualTo("read", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val unreadCount = snapshot?.size() ?: 0
                if (unreadCount > 0) {
                    notificationBadge.visibility = View.VISIBLE
                    notificationBadge.text = if (unreadCount > 9) "9+" else unreadCount.toString()
                } else {
                    notificationBadge.visibility = View.GONE
                }
            }
    }

    // Add this new method to show/hide toolbar
    private fun showToolbar(show: Boolean) {
        appBarLayout.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun initCloudinary() {
        try {
            MediaManager.get()
        } catch (e: IllegalStateException) {
            val config = hashMapOf<String, Any>(
                "cloud_name" to CloudinaryConfig.CLOUD_NAME
            )
            MediaManager.init(this, config)
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
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
            showToolbar(true) // Show toolbar
        }

        navDarshan.setOnClickListener {
            selectTab(R.id.nav_darshan_custom)
            clearBackStack()
            loadFragment(DarshanFragment())
            showToolbar(false) // Hide toolbar
        }

        navSchedule.setOnClickListener {
            selectTab(R.id.nav_schedule_custom)
            clearBackStack()
            loadFragment(ScheduleFragment())
            showToolbar(false) // Hide toolbar
        }

        navServices.setOnClickListener {
            selectTab(R.id.nav_services_custom)
            clearBackStack()
            loadFragment(ServicesFragment())
            showToolbar(false) // Hide toolbar
        }

        navMore.setOnClickListener {
            selectTab(R.id.nav_more_custom)
            clearBackStack()
            loadFragment(MoreFragment())
            showToolbar(false) // Hide toolbar
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
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            // Check which fragment is showing after popping
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            showToolbar(currentFragment is HomeFragment)
        } else if (currentSelectedTab != R.id.nav_home_custom) {
            findViewById<LinearLayout>(R.id.nav_home_custom)?.performClick()
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        loadNotificationCount() // Refresh notification count when returning to activity
        loadUserProfile() // Refresh profile picture
    }
}