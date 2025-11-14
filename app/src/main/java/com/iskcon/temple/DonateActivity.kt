package com.iskcon.temple

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
//import com.example.iskon.R

class DonateActivity : AppCompatActivity() {

    private lateinit var etAnnadaanAmount: EditText
    private lateinit var etGitadaanAmount: EditText
    private lateinit var etJanmashtamiAmount: EditText
    private lateinit var btnDonateAnnadaan: Button
    private lateinit var btnDonateGitadaan: Button
    private lateinit var btnDonateJanmashtami: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        supportActionBar?.apply {
            title = "Donate"
            setDisplayHomeAsUpEnabled(true)
        }

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        etAnnadaanAmount = findViewById(R.id.etAnnadaanAmount)
        etGitadaanAmount = findViewById(R.id.etGitadaanAmount)
        etJanmashtamiAmount = findViewById(R.id.etJanmashtamiAmount)
        btnDonateAnnadaan = findViewById(R.id.btnDonateAnnadaan)
        btnDonateGitadaan = findViewById(R.id.btnDonateGitadaan)
        btnDonateJanmashtami = findViewById(R.id.btnDonateJanmashtami)
    }

    private fun setupClickListeners() {
        btnDonateAnnadaan.setOnClickListener {
            val amount = etAnnadaanAmount.text.toString().trim()
            if (validateAmount(amount)) {
                navigateToPayment("Annadaan", amount)
            }
        }

        btnDonateGitadaan.setOnClickListener {
            val amount = etGitadaanAmount.text.toString().trim()
            if (validateAmount(amount)) {
                navigateToPayment("Gitadaan", amount)
            }
        }

        btnDonateJanmashtami.setOnClickListener {
            val amount = etJanmashtamiAmount.text.toString().trim()
            if (validateAmount(amount)) {
                navigateToPayment("Temple Donation", amount)
            }
        }
    }

    private fun validateAmount(amount: String): Boolean {
        return when {
            amount.isEmpty() -> {
                Toast.makeText(this, "Please enter donation amount", Toast.LENGTH_SHORT).show()
                false
            }
            amount.toDoubleOrNull() == null -> {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                false
            }
            amount.toDouble() <= 0 -> {
                Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show()
                false
            }
            amount.toDouble() < 100 -> {
                Toast.makeText(this, "Minimum donation amount is ₹100", Toast.LENGTH_SHORT).show()
                false
            }
            else -> true
        }
    }

    private fun navigateToPayment(donationType: String, amount: String) {
        val intent = Intent(this, PaymentActivity::class.java)
        intent.putExtra("DONATION_TYPE", donationType)
        intent.putExtra("DONATION_AMOUNT", amount)
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}