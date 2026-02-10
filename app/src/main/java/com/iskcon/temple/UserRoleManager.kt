package com.iskcon.temple

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// This is NOT an Activity. It's a simple helper class.
class UserRoleManager {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    companion object {
        const val ROLE_ADMIN = "admin"
        const val ROLE_USER = "user"
    }

    // This function fetches the role and uses a single callback
    // to return a Result, which can be either a success or a failure.
    fun fetchUserRole(onResult: (Result<String>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onResult(Result.failure(Exception("User not logged in.")))
            return
        }

        val userId = currentUser.uid
        // Look in the 'users' collection for a document with the user's ID
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Get the 'role' field. If it's missing, default to "user".
                    val role = document.getString("role") ?: ROLE_USER
                    onResult(Result.success(role))
                } else {
                    // Document doesn't exist. This can happen if Firestore save failed during signup.
                    // Defaulting to "user" is a safe fallback.
                    onResult(Result.success(ROLE_USER))
                }
            }
            .addOnFailureListener { exception ->
                // An error occurred (e.g., no internet, permissions error).
                onResult(Result.failure(exception))
            }
    }
}
