package com.iskcon.temple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class NotificationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val notifications = mutableListOf<NotificationItem>()
    private lateinit var adapter: NotificationAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyView: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupToolbar()
        setupRecyclerView()
        loadNotifications()
    }

    private fun setupToolbar() {
        findViewById<ImageView>(R.id.back_button)?.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.notifications_recycler_view)
        emptyView = findViewById(R.id.empty_view)

        adapter = NotificationAdapter(notifications)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadNotifications() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            emptyView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            return
        }

        android.util.Log.d("NOTIF", "Loading notifications for UID: ${currentUser.uid}")

        // ✅ FIXED: Removed orderBy("timestamp") — it requires composite index
        // Now we sort in memory instead
        firestore.collection("notifications")
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("NOTIF", "Error loading notifications: ${error.message}")
                    emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    return@addSnapshotListener
                }

                android.util.Log.d("NOTIF", "Documents found: ${snapshot?.documents?.size}")

                notifications.clear()

                snapshot?.documents?.forEach { doc ->
                    val notification = NotificationItem(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        message = doc.getString("message") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        read = doc.getBoolean("read") ?: false,
                        type = doc.getString("type") ?: "general"
                    )
                    notifications.add(notification)
                }

                // ✅ Sort by timestamp descending in memory (no index needed)
                notifications.sortByDescending { it.timestamp }

                adapter.notifyDataSetChanged()

                if (notifications.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
    }

    data class NotificationItem(
        val id: String,
        val title: String,
        val message: String,
        val timestamp: Long,
        val read: Boolean,
        val type: String
    )

    inner class NotificationAdapter(private val items: List<NotificationItem>) :
        RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val icon: TextView = view.findViewById(R.id.notification_icon)
            val title: TextView = view.findViewById(R.id.notification_title)
            val message: TextView = view.findViewById(R.id.notification_message)
            val time: TextView = view.findViewById(R.id.notification_time)
            val unreadDot: View = view.findViewById(R.id.unread_dot)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_notification, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]

            holder.title.text = item.title
            holder.message.text = item.message
            holder.time.text = getTimeAgo(item.timestamp)

            // ✅ Show unread dot for unread notifications
            holder.unreadDot.visibility = if (item.read) View.GONE else View.VISIBLE

            // ✅ Dim read notifications slightly
            holder.itemView.alpha = if (item.read) 0.7f else 1.0f

            holder.icon.text = when (item.type) {
                "festival" -> "🎉"
                "event" -> "📅"
                "reminder" -> "⏰"
                "seva" -> "🙏"
                else -> "🔔"
            }

            // ✅ Mark as read when user taps on notification
            holder.itemView.setOnClickListener {
                if (!item.read) {
                    markAsRead(item.id)
                }
            }
        }

        override fun getItemCount() = items.size

        private fun getTimeAgo(timestamp: Long): String {
            if (timestamp == 0L) return ""
            val now = System.currentTimeMillis()
            val diff = now - timestamp

            return when {
                diff < 60000 -> "Just now"
                diff < 3600000 -> "${diff / 60000} mins ago"
                diff < 86400000 -> "${diff / 3600000} hours ago"
                diff < 604800000 -> "${diff / 86400000} days ago"
                else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
            }
        }

        private fun markAsRead(notificationId: String) {
            firestore.collection("notifications")
                .document(notificationId)
                .update("read", true)
                .addOnSuccessListener {
                    android.util.Log.d("NOTIF", "Marked as read: $notificationId")
                }
        }
    }
}