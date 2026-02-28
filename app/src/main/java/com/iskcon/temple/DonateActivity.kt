package com.iskcon.temple

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class DonateActivity : AppCompatActivity() {

    private lateinit var btnDonateAnnadaan: Button
    private lateinit var btnDonateGitadaan: Button
    private lateinit var btnDonateJanmashtami: Button
    private lateinit var btnHistory: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        supportActionBar?.title = "ISKCON Donation"

        initializeViews()
        setupClickListeners()
    }

    private fun initializeViews() {
        btnDonateAnnadaan = findViewById(R.id.btnDonateAnnadaan)
        btnDonateGitadaan = findViewById(R.id.btnDonateGitadaan)
        btnDonateJanmashtami = findViewById(R.id.btnDonateJanmashtami)
        btnHistory = findViewById(R.id.btnHistory)
    }

    private fun setupClickListeners() {

        // Open History Page
        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        // Annadaan Donation Link
        btnDonateAnnadaan.setOnClickListener {
            openDonationLink("https://rzp.io/l/AnnadanAnyAmount")
        }

        // Gitadaan Donation Link
        btnDonateGitadaan.setOnClickListener {
            openDonationLink("https://rzp.io/l/CUuMw0H2Um-AnyAmount")
        }

        // Janmashtami Donation Link
        btnDonateJanmashtami.setOnClickListener {
            openDonationLink("https://pages.razorpay.com/WWo5n2F8kQuickDonate")
        }
    }

    private fun openDonationLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }
}