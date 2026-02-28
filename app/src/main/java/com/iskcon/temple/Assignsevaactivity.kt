package com.iskcon.temple

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class AssignSevaActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assign_seva)

        firestore = FirebaseFirestore.getInstance()

        userId = intent.getStringExtra("userId") ?: ""
        userName = intent.getStringExtra("userName") ?: "Devotee"

        if (userId.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<ImageView>(R.id.back_button).setOnClickListener { finish() }
        findViewById<TextView>(R.id.seva_user_name).text = "Assigning seva to: $userName"

        val sevaTitle = findViewById<EditText>(R.id.seva_title)
        val sevaDescription = findViewById<EditText>(R.id.seva_description)
        val assignButton = findViewById<Button>(R.id.btn_send_seva)

        assignButton.setOnClickListener {
            val title = sevaTitle.text.toString().trim()
            val description = sevaDescription.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter seva title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (description.isEmpty()) {
                Toast.makeText(this, "Please enter seva details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            assignSeva(title, description)
        }
    }

    private fun assignSeva(title: String, description: String) {
        val notification = hashMapOf(
            "userId" to userId,
            "title" to "🙏 New Seva Assigned: $title",
            "message" to description,
            "timestamp" to System.currentTimeMillis(),
            "read" to false,
            "type" to "seva"
        )

        android.util.Log.d("AssignSeva", "Saving notification for userId: $userId")

        firestore.collection("notifications")
            .add(notification)
            .addOnSuccessListener { docRef ->
                android.util.Log.d("AssignSeva", "Notification saved: ${docRef.id}")
                Toast.makeText(this, "Seva assigned to $userName! 🙏", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                android.util.Log.e("AssignSeva", "Error: ${e.message}")
                Toast.makeText(this, "Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}