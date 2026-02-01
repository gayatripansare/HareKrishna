package com.iskcon.temple

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class ISKON_Sign_up : BaseActivity() {

    private lateinit var signupGetEmail: EditText
    private lateinit var signupGetPassword: EditText
    private lateinit var signupClickSignup: Button
    private lateinit var signupClickSignIn: Button
    private lateinit var btnGoogleSignUp: CardView
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var progressDialog: ProgressDialog
    private val firestore = FirebaseFirestore.getInstance()

    // Google Sign-In launcher
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        handleGoogleSignInResult(task)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_iskon_sign_up)

        // Hide donation FAB on sign-up screen
        hideDonationFab()

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
        btnGoogleSignUp = findViewById(R.id.btn_google_signup)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Progress Dialog
        progressDialog = ProgressDialog(this).apply {
            setMessage("Creating account...")
            setCancelable(false)
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("688972813339-dj29ghhnfhveni3nhcet9346kva21ogs.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // ✅ Sign out from Google when sign-up screen opens (ensures account picker shows)
        googleSignInClient.signOut()

        // Email/Password Sign up button
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

        // Sign in button
        signupClickSignIn.setOnClickListener {
            val intent = Intent(this, ISKON_Sign_in::class.java)
            startActivity(intent)
            finish()
        }

        // Google Sign-Up button
        btnGoogleSignUp.setOnClickListener {
            signUpWithGoogle()
        }
    }

    private fun signUpWithGoogle() {
        // ✅ Sign out first to ensure account picker shows
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d("GoogleSignUp", "firebaseAuthWithGoogle: ${account.id}")
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            Log.w("GoogleSignUp", "Google sign up failed", e)
            Toast.makeText(this, "Google sign up failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        progressDialog.show()

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                progressDialog.dismiss()

                if (task.isSuccessful) {
                    Log.d("GoogleSignUp", "signInWithCredential:success")
                    val user = auth.currentUser

                    // Create user document in Firestore
                    user?.let {
                        createUserDocument(it.uid, it.email ?: "", "regular")
                    }

                    Toast.makeText(this, "Account created successfully! Welcome! 🙏", Toast.LENGTH_SHORT).show()
                    navigateToSignIn()
                } else {
                    Log.w("GoogleSignUp", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun registerUser(email: String, password: String) {
        progressDialog.show()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                progressDialog.dismiss()

                if (task.isSuccessful) {
                    val user = auth.currentUser

                    // Create user document in Firestore
                    user?.let {
                        createUserDocument(it.uid, email, "regular")
                    }

                    Toast.makeText(
                        this,
                        "Sign up successful! Welcome! 🙏",
                        Toast.LENGTH_SHORT
                    ).show()

                    navigateToSignIn()
                } else {
                    Toast.makeText(
                        this,
                        "Sign up failed: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun createUserDocument(uid: String, email: String, role: String) {
        val userData = hashMapOf(
            "email" to email,
            "role" to role,
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(uid)
            .set(userData)
            .addOnSuccessListener {
                Log.d("Firestore", "User document created successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error creating user document: ${e.message}")
            }
    }

    private fun navigateToSignIn() {
        val intent = Intent(this, ISKON_Sign_in::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
