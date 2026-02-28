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
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

class ISKON_Sign_up : AppCompatActivity() {

    private lateinit var signupGetName: EditText
    private lateinit var signupGetEmail: EditText
    private lateinit var signupGetPhone: EditText
    private lateinit var signupGetPassword: EditText
    private lateinit var signupClickSignup: Button
    private lateinit var signupClickSignIn: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_iskon_sign_up)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        signupGetName = findViewById(R.id.devotee_signup_name)
        signupGetEmail = findViewById(R.id.devotee_signup_email)
        signupGetPhone = findViewById(R.id.devotee_signup_phone)
        signupGetPassword = findViewById(R.id.devotee_signup_password)
        signupClickSignup = findViewById(R.id.devotee_signup)
        signupClickSignIn = findViewById(R.id.devotee_Signin_button)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        signupClickSignup.setOnClickListener {
            val name = signupGetName.text.toString().trim()
            val email = signupGetEmail.text.toString().trim()
            val phone = signupGetPhone.text.toString().trim()
            val password = signupGetPassword.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (phone.isEmpty()) {
                Toast.makeText(this, "Please enter your phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerUser(name, email, phone, password)
        }

        signupClickSignIn.setOnClickListener {
            val intent = Intent(this, ISKON_Sign_in::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun registerUser(name: String, email: String, phone: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser!!

                    // Update Firebase Auth display name
                    val profileUpdate = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    user.updateProfile(profileUpdate)

                    // Save user data to Firestore
                    val userData = hashMapOf(
                        "uid" to user.uid,
                        "name" to name,
                        "email" to email,
                        "phone" to phone,
                        "role" to "regular",
                        "createdAt" to System.currentTimeMillis()
                    )

                    firestore.collection("users")
                        .document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Sign up successful! Welcome! 🙏", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, ISKON_Sign_in::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error saving profile: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}