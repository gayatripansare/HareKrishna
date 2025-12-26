package com.iskcon.temple // CORRECTED: Package name is fixed

// CORRECTED: Removed invalid imports and cleaned up the list
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ISKON_Sign_in : AppCompatActivity() {

    private lateinit var getUsername: EditText
    private lateinit var getPassword: EditText
    private lateinit var clickLogin: Button
    private lateinit var clickSignUp: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var roleManager: UserRoleManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_iskon_sign_in)

        // Initialize views
        getUsername = findViewById(R.id.devotee_username)
        getPassword = findViewById(R.id.devotee_password)
        clickLogin = findViewById(R.id.devotee_login)
        clickSignUp = findViewById(R.id.devotee_signup_button)

        // Initialize Firebase Auth and the helper Role Manager
        auth = FirebaseAuth.getInstance()
        roleManager = UserRoleManager() // Correctly initialized

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
        clickLogin.isEnabled = false
        clickLogin.text = "Signing In..."

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login was successful, now fetch the user's role from Firestore
                    fetchRoleAndNavigate()
                } else {
                    // Login failed
                    clickLogin.isEnabled = true
                    clickLogin.text = "Sign In"
                    Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun fetchRoleAndNavigate() {
        // Call the updated fetchUserRole function
        roleManager.fetchUserRole { result ->
            // This 'result' block is executed when the role is fetched (or fails)

            // Re-enable the button regardless of outcome
            clickLogin.isEnabled = true
            clickLogin.text = "Sign In"

            result.onSuccess { role ->
                // Successfully fetched the role from Firestore
                val welcomeMessage = if (role == UserRoleManager.ROLE_ADMIN) {
                    "Welcome Admin! Hare Krishna! 🙏"
                    // Navigate to MainActivity (which hosts HomeFragment)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()  // Close sign-in activity
                } else {
                    "Login successful! Hare Krishna! 🙏"
                }
                Toast.makeText(this, welcomeMessage, Toast.LENGTH_SHORT).show()
                navigateToHome()

            }.onFailure { exception ->
                // This block runs if Firestore access failed (e.g., no internet, permissions issue)
                // We can still let the user in, but show a warning.
                Toast.makeText(this, "Login successful, but couldn't verify role: ${exception.message}", Toast.LENGTH_LONG).show()
                navigateToHome() // Navigate to home with default user access
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        // This flag combination clears the previous screens, so the user can't press "back" to get to the login screen.
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Close the login activity
    }
}
