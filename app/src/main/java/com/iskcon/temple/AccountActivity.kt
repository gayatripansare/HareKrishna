package com.iskcon.temple

import android.content.Intent
import android.os.Bundle
import android.widget.Button

import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.ImageView
import java.text.SimpleDateFormat
import java.util.*

class AccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var profileImage: ImageView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var memberSince: TextView
    private lateinit var signOutButton: Button

    // Settings Cards
    private lateinit var editProfileCard: CardView
    private lateinit var notificationSettingsCard: CardView
    private lateinit var privacyPolicyCard: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initializeViews()
        setupToolbar()
        loadUserData()
        setupClickListeners()
    }

    private fun initializeViews() {
        profileImage = findViewById(R.id.profile_image)
        userName = findViewById(R.id.user_name)
        userEmail = findViewById(R.id.user_email)
        memberSince = findViewById(R.id.member_since)
        signOutButton = findViewById(R.id.sign_out_button)

        // Initialize cards
        editProfileCard = findViewById(R.id.card_edit_profile)
        notificationSettingsCard = findViewById(R.id.card_notification_settings)
        privacyPolicyCard = findViewById(R.id.card_privacy_policy)
    }

    private fun setupToolbar() {
        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Load profile image
            val photoUrl = currentUser.photoUrl
            if (photoUrl != null) {
                Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_default_profile)
                    .into(profileImage)
            }

            // Load user name
            userName.text = currentUser.displayName ?: "Devotee"

            // Load email
            userEmail.text = currentUser.email ?: ""

            // Calculate member since
            val creationTime = currentUser.metadata?.creationTimestamp
            if (creationTime != null) {
                val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                memberSince.text = "Member since ${dateFormat.format(Date(creationTime))}"
            }
        }
    }

    private fun setupClickListeners() {
        // Edit Profile Click
        editProfileCard.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Notification Settings Click
        notificationSettingsCard.setOnClickListener {
            val intent = Intent(this, NotificationSettingsActivity::class.java)
            startActivity(intent)
        }

        // Privacy Policy Click
        privacyPolicyCard.setOnClickListener {
            showPrivacyPolicyDialog()
        }

        // Sign Out Button
        signOutButton.setOnClickListener {
            showSignOutDialog()
        }
    }

    private fun showPrivacyPolicyDialog() {
        AlertDialog.Builder(this)
            .setTitle("Privacy Policy")
            .setMessage("ISKCON Kopergaon respects your privacy and is committed to protecting your personal information.\n\n" +
                    "We collect only necessary information to provide you with temple services and updates.\n\n" +
                    "Your data is never shared with third parties without your consent.\n\n" +
                    "For more information, please contact the temple administration.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Yes") { _, _ ->
                signOut()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun signOut() {
        auth.signOut()

        // Clear admin preferences
        val sharedPref = getSharedPreferences("AdminPrefs", MODE_PRIVATE)
        sharedPref.edit().apply {
            putBoolean("isAdminLoggedIn", false)
            apply()
        }

        Toast.makeText(this, "Signed out successfully 🙏", Toast.LENGTH_SHORT).show()

        // Navigate to ISKON_Sign_in activity
        val intent = Intent(this, ISKON_Sign_in::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}