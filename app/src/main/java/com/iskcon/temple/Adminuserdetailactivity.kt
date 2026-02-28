package com.iskcon.temple

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminUserDetailActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private val chantingLogs = mutableListOf<ChantingLog>()
    private lateinit var adapter: ChantingLogAdapter

    private lateinit var userId: String
    private lateinit var userName: String
    private lateinit var userEmail: String
    private lateinit var userPhone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_user_detail)

        firestore = FirebaseFirestore.getInstance()

        userId = intent.getStringExtra("userId") ?: ""
        userName = intent.getStringExtra("userName") ?: "Devotee"
        userEmail = intent.getStringExtra("userEmail") ?: ""
        userPhone = intent.getStringExtra("userPhone") ?: ""

        if (userId.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        findViewById<ImageView>(R.id.back_button).setOnClickListener { finish() }

        // Set user info
        findViewById<TextView>(R.id.detail_user_name).text = userName
        findViewById<TextView>(R.id.detail_user_email).text = userEmail
        val phoneView = findViewById<TextView>(R.id.detail_user_phone)
        phoneView.text = if (userPhone.isNotEmpty()) "📞 $userPhone" else ""
        phoneView.visibility = if (userPhone.isNotEmpty()) View.VISIBLE else View.GONE
        findViewById<TextView>(R.id.detail_user_avatar).text = userName.firstOrNull()?.uppercase() ?: "D"

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.chanting_recycler_view)
        val emptyView = findViewById<LinearLayout>(R.id.empty_chanting_view)

        adapter = ChantingLogAdapter(chantingLogs)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadChantingLogs(recyclerView, emptyView)

        // Assign Seva button
        findViewById<Button>(R.id.btn_assign_seva).setOnClickListener {
            val intent = Intent(this, AssignSevaActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("userName", userName)
            startActivity(intent)
        }
    }

    private fun loadChantingLogs(recyclerView: RecyclerView, emptyView: LinearLayout) {
        firestore.collection("chanting_logs")
            .document(userId)
            .collection("logs")
            .orderBy("updatedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading chanting data", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                chantingLogs.clear()
                snapshot?.documents?.forEach { doc ->
                    chantingLogs.add(
                        ChantingLog(
                            date = doc.id, // document ID is the date "dd-MM-yyyy"
                            count = doc.getLong("count") ?: 0
                        )
                    )
                }

                adapter.notifyDataSetChanged()

                if (chantingLogs.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
    }

    data class ChantingLog(val date: String, val count: Long)

    class ChantingLogAdapter(private val items: List<ChantingLog>) :
        RecyclerView.Adapter<ChantingLogAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val dateText: TextView = view.findViewById(R.id.log_date)
            val countText: TextView = view.findViewById(R.id.log_count)
            val bar: View = view.findViewById(R.id.log_bar)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_chanting_log, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val log = items[position]
            holder.dateText.text = log.date
            holder.countText.text = "${log.count} rounds 📿"

            // Simple visual bar — width based on count (max 16 rounds standard)
            val maxRounds = 16f
            val percent = (log.count.toFloat() / maxRounds).coerceAtMost(1f)
            val params = holder.bar.layoutParams
            params.width = (percent * 600).toInt().coerceAtLeast(40)
            holder.bar.layoutParams = params
        }

        override fun getItemCount() = items.size
    }
}