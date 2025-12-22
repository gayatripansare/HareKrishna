package com.iskcon.temple

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AdminEventActivity : AppCompatActivity() {

    private lateinit var etEventName: TextInputEditText
    private lateinit var etEventDescription: TextInputEditText
    private lateinit var etEventDate: TextInputEditText
    private lateinit var btnSaveEvent: CardView
    private lateinit var btnBack: ImageView
    private lateinit var progressBar: ProgressBar

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_event)

        initViews()
        setupDatePicker()
        setupClickListeners()
    }

    private fun initViews() {
        etEventName = findViewById(R.id.et_event_name)
        etEventDescription = findViewById(R.id.et_event_description)
        etEventDate = findViewById(R.id.et_event_date)
        btnSaveEvent = findViewById(R.id.btn_save_event)
        btnBack = findViewById(R.id.btn_back)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupDatePicker() {
        etEventDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    etEventDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
            datePickerDialog.show()
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSaveEvent.setOnClickListener {
            validateAndSaveEvent()
        }
    }

    private fun validateAndSaveEvent() {
        val name = etEventName.text.toString().trim()
        val description = etEventDescription.text.toString().trim()
        val date = etEventDate.text.toString().trim()

        when {
            name.isEmpty() -> {
                etEventName.error = "Please enter event name"
                etEventName.requestFocus()
            }
            description.isEmpty() -> {
                etEventDescription.error = "Please enter description"
                etEventDescription.requestFocus()
            }
            date.isEmpty() -> {
                etEventDate.error = "Please select date"
                etEventDate.requestFocus()
            }
            else -> {
                saveEventToFirestore(name, description, date)
            }
        }
    }

    private fun saveEventToFirestore(name: String, description: String, date: String) {
        showProgress(true)

        val eventId = "event_${System.currentTimeMillis()}"
        val currentUser = auth.currentUser

        val event = Event(
            id = eventId,
            name = name,
            description = description,
            date = date,
            timestamp = calendar.timeInMillis,
            createdBy = currentUser?.uid ?: "",
            createdAt = System.currentTimeMillis()
        )

        firestore.collection("custom_events")
            .document(eventId)
            .set(event)
            .addOnSuccessListener {
                showProgress(false)
                Toast.makeText(this, "✅ Event added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                showProgress(false)
                Toast.makeText(this, "❌ Failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showProgress(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSaveEvent.isEnabled = !show
    }
}