package com.iskcon.temple

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class YouthForum : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_youth_forum)

        // Enable back button in action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Youth Forum"

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Register Button
        findViewById<Button>(R.id.registerButton)?.setOnClickListener {
            openRegistrationOptions()
        }

        // Lectures Button - Navigate to Screen3
        findViewById<ImageButton>(R.id.lecturesButton)?.setOnClickListener {
            try {
                val intent = Intent(this, Screen5::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to open Lectures", Toast.LENGTH_SHORT).show()
            }
        }

        // Images Button - Navigate to Screen5
        findViewById<ImageButton>(R.id.imagesButton)?.setOnClickListener {
            try {
                val intent = Intent(this, Screen6::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to open Images", Toast.LENGTH_SHORT).show()
            }
        }

        // Camps Button - Navigate to Screen6
        findViewById<ImageButton>(R.id.campsButton)?.setOnClickListener {
            try {
                val intent = Intent(this, Screen3::class.java)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to open Camps", Toast.LENGTH_SHORT).show()
            }
        }

        // Info grid items
        findViewById<ImageButton>(R.id.imageButton)?.setOnClickListener {
            Toast.makeText(this, "Communication Skills Development 🗣️", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.imageButton2)?.setOnClickListener {
            Toast.makeText(this, "Concentration & Focus Training 🧘", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.imageButton3)?.setOnClickListener {
            Toast.makeText(this, "Spiritual Prasadam 🍽️", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.imageButton4)?.setOnClickListener {
            Toast.makeText(this, "Chanting & Meditation 📿", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openRegistrationOptions() {
        // Show dialog with registration options
        val options = arrayOf("Call Temple", "Register via Google Form", "Cancel")

        android.app.AlertDialog.Builder(this)
            .setTitle("Register for Youth Forum")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openPhoneDialer()
                    1 -> openGoogleForm()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun openPhoneDialer() {
        try {
            val phoneNumber = "919876543210"
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse(phoneNumber)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open dialer", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGoogleForm() {
        try {
            val formUrl = "https://forms.gle/D6rRucLjoueXYbxw6"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open Google Form", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @SuppressLint("GestureBackNavigation")
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}
