package com.iskcon.temple

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminFestivalActivity : AppCompatActivity() {

    private lateinit var rvFestivals: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnBack: ImageView
    private lateinit var adapter: AdminFestivalAdapter

    private val firestore = FirebaseFirestore.getInstance()
    private val festivalsList = mutableListOf<Festival>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_festival)

        initViews()
        setupRecyclerView()
        loadFestivals()

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun initViews() {
        rvFestivals = findViewById(R.id.rv_festivals)
        progressBar = findViewById(R.id.progress_bar)
        btnBack = findViewById(R.id.btn_back)
    }

    private fun setupRecyclerView() {
        adapter = AdminFestivalAdapter(festivalsList) { festival ->
            showEditDialog(festival)
        }
        rvFestivals.adapter = adapter
        rvFestivals.layoutManager = LinearLayoutManager(this)
    }

    private fun loadFestivals() {
        progressBar.visibility = View.VISIBLE

        firestore.collection("festivals")
            .get()
            .addOnSuccessListener { documents ->
                festivalsList.clear()

                for (document in documents) {
                    try {
                        val festival = document.toObject(Festival::class.java)
                        festivalsList.add(festival)
                    } catch (e: Exception) {
                        Log.e("AdminFestivalActivity", "Error parsing festival: ${e.message}")
                    }
                }

                // Sort by year, month, day
                festivalsList.sortWith(compareBy({ it.year }, { it.month }, { it.day }))

                adapter.notifyDataSetChanged()
                progressBar.visibility = View.GONE

                Log.d("AdminFestivalActivity", "✅ Loaded ${festivalsList.size} festivals")
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("AdminFestivalActivity", "❌ Error: ${e.message}")
            }
    }

    private fun showEditDialog(festival: Festival) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_festival, null)
        val etDate = dialogView.findViewById<android.widget.EditText>(R.id.et_festival_date)

        etDate.setText(festival.date)

        AlertDialog.Builder(this)
            .setTitle("Edit ${festival.name}")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newDate = etDate.text.toString().trim()
                if (newDate.isNotEmpty() && newDate.matches(Regex("\\d{2}-\\d{2}-\\d{4}"))) {
                    updateFestivalDate(festival, newDate)
                } else {
                    Toast.makeText(this, "Invalid date format. Use DD-MM-YYYY", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateFestivalDate(festival: Festival, newDate: String) {
        progressBar.visibility = View.VISIBLE

        val dateParts = newDate.split("-")

        // ✅ FIX: Create a map with updated values
        val updates = hashMapOf<String, Any>(
            "date" to newDate,
            "day" to dateParts[0].toInt(),
            "month" to dateParts[1].toInt(),
            "year" to dateParts[2].toInt()
        )

        firestore.collection("festivals")
            .document(festival.id)
            .update(updates)
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "✅ Festival updated!", Toast.LENGTH_SHORT).show()
                loadFestivals() // Reload the list
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "❌ Failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}