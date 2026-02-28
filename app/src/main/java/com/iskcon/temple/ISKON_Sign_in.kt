package com.iskcon.temple

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class ISKON_Sign_in : AppCompatActivity() {

    private lateinit var getUsername: EditText
    private lateinit var getPassword: EditText
    private lateinit var clickLogin: Button
    private lateinit var clickSignUp: Button
    private lateinit var btnGoogleSignIn: CardView
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // ✅ Modern way to handle Activity Results
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

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
        btnGoogleSignIn = findViewById(R.id.btn_google_signin)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // ✅ Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // From google-services.json
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Email/Password Login
        clickLogin.setOnClickListener {
            val email = getUsername.text.toString().trim()
            val password = getPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            loginUser(email, password)
        }

        // Navigate to Sign Up
        clickSignUp.setOnClickListener {
            startActivity(Intent(this, ISKON_Sign_up::class.java))
        }

        // ✅ Google Sign-In Button Click
        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        // Sign out first to allow account chooser to show every time
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val email = auth.currentUser?.email ?: ""
                    Toast.makeText(this, "Google Sign-In successful! Hare Krishna! 🙏", Toast.LENGTH_SHORT).show()
                    checkIfAdminAndSetFlag(email)
                    navigateToMain()
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful! Hare Krishna! 🙏", Toast.LENGTH_SHORT).show()
                    checkIfAdminAndSetFlag(email)
                    navigateToMain()
                } else {
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun checkIfAdminAndSetFlag(email: String) {
        val isAdmin = email.lowercase() == "sudamapr05@gmail.com"
        val sharedPref = getSharedPreferences("AdminPrefs", MODE_PRIVATE)
        sharedPref.edit().putBoolean("isAdminLoggedIn", isAdmin).apply()
        if (isAdmin) {
            Toast.makeText(this, "🔑 Admin access granted", Toast.LENGTH_SHORT).show()
        }
    }
}