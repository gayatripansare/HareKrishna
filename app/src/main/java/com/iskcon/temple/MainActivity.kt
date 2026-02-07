package com.iskcon.temple

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.cloudinary.android.MediaManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity() {

    private var currentSelectedTab = R.id.nav_home_custom
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

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

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize Cloudinary
        initCloudinary()

        // Request notification permission
        requestNotificationPermission()

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        setupCustomBottomNavigation()

        // Handle back press with OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
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
                    finish()
                }
            }
        })

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
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

    // ✅ NEW METHOD: Called from MoreFragment to perform sign-out
    fun performSignOut() {
        // Sign out from Firebase
        auth.signOut()

        // Sign out from Google (clears account cache)
        googleSignInClient.signOut().addOnCompleteListener(this) {
            Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()

            // Navigate to Sign-In screen
            val intent = Intent(this, ISKON_Sign_in::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}