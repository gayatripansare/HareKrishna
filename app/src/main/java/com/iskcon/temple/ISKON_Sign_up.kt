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

class ISKON_Sign_up : AppCompatActivity() {

    private lateinit var signupGetEmail: EditText
    private lateinit var signupGetPassword: EditText
    private lateinit var signupClickSignup: Button
    private lateinit var signupClickSignIn: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_iskon_sign_up)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        signupGetEmail = findViewById(R.id.devotee_signup_email)
        signupGetPassword = findViewById(R.id.devotee_signup_password)
        signupClickSignup = findViewById(R.id.devotee_signup)
        signupClickSignIn = findViewById(R.id.devotee_Signin_button)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Sign up button click listener
        signupClickSignup.setOnClickListener {
            val email = signupGetEmail.text.toString().trim()
            val password = signupGetPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(email, password)
        }

        // Sign in button click listener
        signupClickSignIn.setOnClickListener {
            val intent = Intent(this, ISKON_Sign_in::class.java)
            startActivity(intent)
            finish() // Close sign up activity
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Sign up successful! Welcome! 🙏",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Navigate to sign in page
                    val intent = Intent(this, ISKON_Sign_in::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Sign up failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}