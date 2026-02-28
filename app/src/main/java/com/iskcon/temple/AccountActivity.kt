package com.iskcon.temple

import android.content.Intent
import android.os.Bundle
import android.view.View
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

    private lateinit var editProfileCard: CardView
    private lateinit var notificationSettingsCard: CardView
    private lateinit var privacyPolicyCard: CardView
    private lateinit var chantingCard: CardView
    private lateinit var todayChantingCount: TextView
    private lateinit var adminUsersCard: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        initializeViews()
        setupToolbar()
        loadUserData()
        setupClickListeners()
        checkAdminAndShowButton()
        loadTodayChanting()
    }

    private fun initializeViews() {
        profileImage = findViewById(R.id.profile_image)
        userName = findViewById(R.id.user_name)
        userEmail = findViewById(R.id.user_email)
        memberSince = findViewById(R.id.member_since)
        signOutButton = findViewById(R.id.sign_out_button)
        editProfileCard = findViewById(R.id.card_edit_profile)
        notificationSettingsCard = findViewById(R.id.card_notification_settings)
        privacyPolicyCard = findViewById(R.id.card_privacy_policy)
        chantingCard = findViewById(R.id.card_chanting)
        todayChantingCount = findViewById(R.id.today_chanting_count)
        adminUsersCard = findViewById(R.id.card_admin_users)
    }

    private fun setupToolbar() {
        findViewById<ImageView>(R.id.back_button).setOnClickListener { finish() }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser ?: return

        val photoUrl = currentUser.photoUrl
        if (photoUrl != null) {
            Glide.with(this).load(photoUrl).placeholder(R.drawable.ic_default_profile).into(profileImage)
        }

        userEmail.text = currentUser.email ?: ""

        val creationTime = currentUser.metadata?.creationTimestamp
        if (creationTime != null) {
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            memberSince.text = "Member since ${dateFormat.format(Date(creationTime))}"
        }

        // Try Firestore, fallback silently — never show error
        firestore.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    userName.text = doc.getString("name") ?: currentUser.displayName ?: "Devotee"
                } else {
                    // Auto-create missing document for old users
                    userName.text = currentUser.displayName ?: "Devotee"
                    val userData = hashMapOf(
                        "uid" to currentUser.uid,
                        "name" to (currentUser.displayName ?: "Devotee"),
                        "email" to (currentUser.email ?: ""),
                        "phone" to "",
                        "role" to "regular",
                        "createdAt" to System.currentTimeMillis()
                    )
                    firestore.collection("users").document(currentUser.uid).set(userData)
                }
            }
            .addOnFailureListener {
                userName.text = currentUser.displayName ?: "Devotee" // Silently fallback
            }
    }

    private fun checkAdminAndShowButton() {
        val sharedPref = getSharedPreferences("AdminPrefs", MODE_PRIVATE)
        val isAdmin = sharedPref.getBoolean("isAdminLoggedIn", false)
        adminUsersCard.visibility = if (isAdmin) View.VISIBLE else View.GONE
        // Hide chanting card for admin — admin doesn't need to log chanting
        chantingCard.visibility = if (isAdmin) View.GONE else View.VISIBLE
    }

    private fun loadTodayChanting() {
        val uid = auth.currentUser?.uid ?: return
        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        firestore.collection("chanting_logs")
            .document(uid)
            .collection("logs")
            .document(today)
            .get()
            .addOnSuccessListener { doc ->
                val count = doc.getLong("count") ?: 0
                todayChantingCount.text = if (count > 0) "Today: $count rounds 🙏" else "Not logged yet"
            }
    }

    private fun setupClickListeners() {
        editProfileCard.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }

        notificationSettingsCard.setOnClickListener {
            startActivity(Intent(this, NotificationSettingsActivity::class.java))
        }

        privacyPolicyCard.setOnClickListener { showPrivacyPolicyDialog() }

        signOutButton.setOnClickListener { showSignOutDialog() }

        // Chanting card — user logs their rounds
        chantingCard.setOnClickListener { showChantingDialog() }

        // Admin Users button
        adminUsersCard.setOnClickListener {
            startActivity(Intent(this, AdminUserListActivity::class.java))
        }
    }

    private fun showChantingDialog() {
        val input = android.widget.EditText(this)
        input.hint = "Enter number of rounds"
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.setPadding(48, 32, 48, 32)

        AlertDialog.Builder(this)
            .setTitle("🙏 Today's Chanting")
            .setMessage("How many rounds did you chant today?")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val countText = input.text.toString().trim()
                if (countText.isEmpty()) {
                    Toast.makeText(this, "Please enter a number", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val count = countText.toLongOrNull()
                if (count == null || count <= 0) {
                    Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                saveChantingCount(count)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveChantingCount(count: Long) {
        val uid = auth.currentUser?.uid ?: return
        val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

        val data = hashMapOf(
            "count" to count,
            "updatedAt" to System.currentTimeMillis()
        )

        firestore.collection("chanting_logs")
            .document(uid)
            .collection("logs")
            .document(today)
            .set(data)
            .addOnSuccessListener {
                todayChantingCount.text = "Today: $count rounds 🙏"
                Toast.makeText(this, "Chanting count saved! Hare Krishna! 🙏", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save. Try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showPrivacyPolicyDialog() {
        AlertDialog.Builder(this)
            .setTitle("Privacy Policy")
            .setMessage("ISKCON Kopergaon respects your privacy and is committed to protecting your personal information.\n\nWe collect only necessary information to provide you with temple services and updates.\n\nYour data is never shared with third parties without your consent.\n\nFor more information, please contact the temple administration.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Yes") { _, _ -> signOut() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun signOut() {
        auth.signOut()
        val sharedPref = getSharedPreferences("AdminPrefs", MODE_PRIVATE)
        sharedPref.edit().putBoolean("isAdminLoggedIn", false).apply()
        Toast.makeText(this, "Signed out successfully 🙏", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ISKON_Sign_in::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}