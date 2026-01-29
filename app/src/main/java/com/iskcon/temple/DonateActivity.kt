package com.iskcon.temple

import kotlin.text.contains



import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

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

        supportActionBar?.title = "ISKCON Donation"

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
                startUpiPayment(amount, "Annadaan")
            }
        }

        btnDonateGitadaan.setOnClickListener {
            val amount = etGitadaanAmount.text.toString().trim()
            if (validateAmount(amount)) {
                startUpiPayment(amount, "Gitadaan")
            }
        }

        btnDonateJanmashtami.setOnClickListener {
            val amount = etJanmashtamiAmount.text.toString().trim()
            if (validateAmount(amount)) {
                startUpiPayment(amount, "Janmashtami Donation")
            }
        }
    }

    private fun validateAmount(amount: String): Boolean {
        val value = amount.toDoubleOrNull()

        return when {
            amount.isEmpty() -> {
                toast("Please enter donation amount")
                false
            }
            value == null -> {
                toast("Enter a valid number")
                false
            }
            value < 30 -> {
                toast("Minimum donation is ₹30")
                false
            }
            else -> true
        }
    }

    private fun startUpiPayment(amount: String, purpose: String) {

        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", "paytm.s1lei5z@pty")   // ✅ Receiver UPI ID
            .appendQueryParameter("pn", "ISKCON Donation")
            .appendQueryParameter("tn", purpose)
            .appendQueryParameter("am", amount)
            .appendQueryParameter("cu", "INR")
            .build()

        val intent = Intent(Intent.ACTION_VIEW, uri)
        val chooser = Intent.createChooser(intent, "Pay using UPI")

        try {
            upiLauncher.launch(chooser)
        } catch (e: Exception) {
            toast("No UPI app found")
        }
    }

    private val upiLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val response = result.data!!.getStringExtra("response")
                if (response != null && response.contains("SUCCESS", true)) {
                    toast("Donation Successful 🙏")
                } else {
                    toast("Payment Cancelled or Failed")
                }
            } else {
                toast("Payment Cancelled")
            }
        }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
