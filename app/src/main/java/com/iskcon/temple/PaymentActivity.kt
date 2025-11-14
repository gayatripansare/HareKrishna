package com.iskcon.temple

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import com.example.iskon.R

class PaymentActivity : AppCompatActivity() {

    private lateinit var tvDonationType: TextView
    private lateinit var tvDonationAmount: TextView
    private lateinit var etDonorName: EditText
    private lateinit var etDonorEmail: EditText
    private lateinit var etDonorPhone: EditText
    private lateinit var etDonorAddress: EditText
    private lateinit var btnProceedPayment: Button

    private var donationType: String = ""
    private var donationAmount: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        supportActionBar?.apply {
            title = "Payment Details"
            setDisplayHomeAsUpEnabled(true)
        }

        // Get data from intent
        donationType = intent.getStringExtra("DONATION_TYPE") ?: ""
        donationAmount = intent.getStringExtra("DONATION_AMOUNT") ?: ""

        initializeViews()
        displayDonationInfo()
        setupClickListeners()
    }

    private fun initializeViews() {
        tvDonationType = findViewById(R.id.tvDonationType)
        tvDonationAmount = findViewById(R.id.tvDonationAmount)
        etDonorName = findViewById(R.id.etDonorName)
        etDonorEmail = findViewById(R.id.etDonorEmail)
        etDonorPhone = findViewById(R.id.etDonorPhone)
        etDonorAddress = findViewById(R.id.etDonorAddress)
        btnProceedPayment = findViewById(R.id.btnProceedPayment)
    }

    private fun displayDonationInfo() {
        tvDonationType.text = donationType
        tvDonationAmount.text = "₹ $donationAmount"
    }

    private fun setupClickListeners() {
        btnProceedPayment.setOnClickListener {
            if (validateInputs()) {
                processPayment()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val name = etDonorName.text.toString().trim()
        val email = etDonorEmail.text.toString().trim()
        val phone = etDonorPhone.text.toString().trim()

        when {
            name.isEmpty() -> {
                etDonorName.error = "Please enter your name"
                etDonorName.requestFocus()
                return false
            }
            email.isEmpty() -> {
                etDonorEmail.error = "Please enter your email"
                etDonorEmail.requestFocus()
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etDonorEmail.error = "Please enter a valid email"
                etDonorEmail.requestFocus()
                return false
            }
            phone.isEmpty() -> {
                etDonorPhone.error = "Please enter your phone number"
                etDonorPhone.requestFocus()
                return false
            }
            phone.length < 10 -> {
                etDonorPhone.error = "Please enter a valid 10-digit phone number"
                etDonorPhone.requestFocus()
                return false
            }
        }
        return true
    }

    private fun processPayment() {
        val name = etDonorName.text.toString().trim()
        val email = etDonorEmail.text.toString().trim()
        val phone = etDonorPhone.text.toString().trim()
        val address = etDonorAddress.text.toString().trim()

        // Here you would integrate with payment gateway
        Toast.makeText(
            this,
            "Processing payment for $donationType\nAmount: ₹$donationAmount\nDonor: $name",
            Toast.LENGTH_LONG
        ).show()

        // Example: Start Razorpay
        // startRazorpayPayment(name, email, phone)

        // After successful payment, navigate to success screen
        // startActivity(Intent(this, PaymentSuccessActivity::class.java))
        // finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}