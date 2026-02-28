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
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var editName: EditText
    private lateinit var editEmail: EditText
    private lateinit var editPhone: EditText
    private lateinit var saveButton: Button
    private lateinit var profileImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        editName = findViewById(R.id.edit_name)
        editEmail = findViewById(R.id.edit_email)
        editPhone = findViewById(R.id.edit_phone)
        saveButton = findViewById(R.id.save_button)
        profileImage = findViewById(R.id.edit_profile_image)

        findViewById<ImageView>(R.id.back_button).setOnClickListener { finish() }

        loadCurrentData()

        saveButton.setOnClickListener {
            val name = editName.text.toString().trim()
            val phone = editPhone.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            saveProfile(name, phone)
        }
    }

    private fun loadCurrentData() {
        val user = auth.currentUser ?: return

        if (user.photoUrl != null) {
            Glide.with(this).load(user.photoUrl).placeholder(R.drawable.ic_default_profile).into(profileImage)
        }

        editEmail.setText(user.email)

        // Load from Firestore for name and phone
        firestore.collection("users").document(user.uid).get()
            .addOnSuccessListener { doc ->
                editName.setText(doc.getString("name") ?: user.displayName ?: "")
                editPhone.setText(doc.getString("phone") ?: "")
            }
    }

    private fun saveProfile(name: String, phone: String) {
        val user = auth.currentUser ?: return

        // Update Firebase Auth display name
        val profileUpdate = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        user.updateProfile(profileUpdate)

        // Update Firestore
        firestore.collection("users").document(user.uid)
            .update(mapOf("name" to name, "phone" to phone))
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated! 🙏", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                // If document doesn't exist yet, create it
                val data = hashMapOf(
                    "uid" to user.uid,
                    "name" to name,
                    "email" to (user.email ?: ""),
                    "phone" to phone,
                    "role" to "regular",
                    "createdAt" to System.currentTimeMillis()
                )
                firestore.collection("users").document(user.uid).set(data)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile updated! 🙏", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    }
            }
    }
}