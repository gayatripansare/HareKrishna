package com.iskcon.temple

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var profileImage: ImageView
    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()

        initializeViews()
        setupToolbar()
        loadCurrentUserData()
        setupClickListeners()
    }

    private fun initializeViews() {
        profileImage = findViewById(R.id.edit_profile_image)
        editName = findViewById(R.id.edit_name)
        editEmail = findViewById(R.id.edit_email)
        saveButton = findViewById(R.id.save_button)
    }

    private fun setupToolbar() {
        findViewById<ImageView>(R.id.back_button).setOnClickListener {
            finish()
        }
    }

    private fun loadCurrentUserData() {
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

            // Load current data
            editName.setText(currentUser.displayName ?: "")
            editEmail.setText(currentUser.email ?: "")
            editEmail.isEnabled = false // Email cannot be changed easily
        }
    }

    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            saveProfile()
        }
    }

    private fun saveProfile() {
        val newName = editName.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(newName)
                .build()

            currentUser.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Profile updated successfully! 🙏", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to update profile: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}