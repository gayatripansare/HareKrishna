package com.iskcon.temple

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
//import com.example.iskon.R

class PaymentSuccessActivity : AppCompatActivity() {

    private lateinit var tvPaymentId: TextView
    private lateinit var tvDonationType: TextView
    private lateinit var tvDonationAmount: TextView
    private lateinit var btnBackHome: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_success)

        supportActionBar?.hide()

        initializeViews()
        displayPaymentInfo()
        setupClickListeners()
    }

    private fun initializeViews() {
        tvPaymentId = findViewById(R.id.tvPaymentId)
        tvDonationType = findViewById(R.id.tvDonationType)
        tvDonationAmount = findViewById(R.id.tvDonationAmount)
        btnBackHome = findViewById(R.id.btnBackHome)
    }

    private fun displayPaymentInfo() {
        val paymentId = intent.getStringExtra("PAYMENT_ID") ?: "N/A"
        val donationType = intent.getStringExtra("DONATION_TYPE") ?: ""
        val donationAmount = intent.getStringExtra("DONATION_AMOUNT") ?: ""

        tvPaymentId.text = paymentId
        tvDonationType.text = donationType
        tvDonationAmount.text = "₹ $donationAmount"
    }

    private fun setupClickListeners() {
        btnBackHome.setOnClickListener {
            val intent = Intent(this, DonateActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {
        // Disable back button
        // User must click "Back to Home" button
    }
}