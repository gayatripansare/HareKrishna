package com.iskcon.temple

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DonationHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        supportActionBar?.title = "Donation History"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        recyclerView = findViewById(R.id.rvDonationHistory)
        recyclerView.layoutManager = LinearLayoutManager(this)

        loadDonationHistory()
    }

    private fun loadDonationHistory() {
        lifecycleScope.launch {
            val donations = DonationDatabase
                .getDatabase(this@HistoryActivity)
                .donationDao()
                .getAllDonations()

            adapter = DonationHistoryAdapter(donations)
            recyclerView.adapter = adapter
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}