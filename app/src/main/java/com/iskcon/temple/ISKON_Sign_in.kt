package com.iskcon.temple

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class ISKON_Sign_in : AppCompatActivity() {

    private lateinit var getUsername: EditText
    private lateinit var getPassword: EditText
    private lateinit var clickLogin: Button
    private lateinit var clickSignUp: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_iskon_sign_in)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        getUsername = findViewById(R.id.devotee_username)
        getPassword = findViewById(R.id.devotee_password)
        clickLogin = findViewById(R.id.devotee_login)
        clickSignUp = findViewById(R.id.devotee_signup_button)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Login button click listener
        clickLogin.setOnClickListener {
            val email = getUsername.text.toString().trim()
            val password = getPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        // Sign up button click listener
        clickSignUp.setOnClickListener {
            val intent = Intent(this, ISKON_Sign_up::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful! Hare Krishna! 🙏", Toast.LENGTH_SHORT).show()

                    // ✅ CHECK IF USER IS ADMIN
                    checkIfAdminAndSetFlag(email)

                    // Navigate to MainActivity (which hosts HomeFragment)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()  // Close sign-in activity
                } else {
                    Toast.makeText(
                        this,
                        "Login failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    // ✅ NEW FUNCTION: Check if user is admin
    private fun checkIfAdminAndSetFlag(email: String) {
        // Replace with YOUR actual admin email from Firebase Auth
        val isAdmin = email.lowercase() == "sudamapr05@gmail.com"  // ← CHANGE THIS

        val sharedPref = getSharedPreferences("AdminPrefs", MODE_PRIVATE)
        sharedPref.edit().apply {
            putBoolean("isAdminLoggedIn", isAdmin)
            apply()
        }

        if (isAdmin) {
            Toast.makeText(this, "🔑 Admin access granted", Toast.LENGTH_SHORT).show()
        }
    }
}