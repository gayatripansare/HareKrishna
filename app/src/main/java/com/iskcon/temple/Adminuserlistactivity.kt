package com.iskcon.temple

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AdminUserListActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private val userList = mutableListOf<UserModel>()
    private lateinit var adapter: UserListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_user_list)

        firestore = FirebaseFirestore.getInstance()

        findViewById<ImageView>(R.id.back_button).setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.users_recycler_view)
        val emptyView = findViewById<LinearLayout>(R.id.empty_view)

        adapter = UserListAdapter(userList) { user ->
            val intent = Intent(this, AdminUserDetailActivity::class.java)
            intent.putExtra("userId", user.uid)
            intent.putExtra("userName", user.name)
            intent.putExtra("userEmail", user.email)
            intent.putExtra("userPhone", user.phone)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadUsers(recyclerView, emptyView)
    }

    private fun loadUsers(recyclerView: RecyclerView, emptyView: LinearLayout) {
        firestore.collection("users")
            .whereEqualTo("role", "regular")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading users", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                userList.clear()
                snapshot?.documents?.forEach { doc ->
                    val user = UserModel(
                        uid = doc.id,  // ← ALWAYS use document ID
                        name = doc.getString("name") ?: "Unknown",
                        email = doc.getString("email") ?: "",
                        phone = doc.getString("phone") ?: ""
                    )
                    userList.add(user)
                }

                adapter.notifyDataSetChanged()

                if (userList.isEmpty()) {
                    emptyView.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    emptyView.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                }
            }
    }

    data class UserModel(
        val uid: String,
        val name: String,
        val email: String,
        val phone: String
    )

    class UserListAdapter(
        private val items: List<UserModel>,
        private val onClick: (UserModel) -> Unit
    ) : RecyclerView.Adapter<UserListAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val card: CardView = view.findViewById(R.id.user_card)
            val nameText: TextView = view.findViewById(R.id.user_name)
            val emailText: TextView = view.findViewById(R.id.user_email)
            val phoneText: TextView = view.findViewById(R.id.user_phone)
            val avatarText: TextView = view.findViewById(R.id.user_avatar)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_user, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = items[position]
            holder.nameText.text = user.name
            holder.emailText.text = user.email
            holder.phoneText.text = if (user.phone.isNotEmpty()) "📞 ${user.phone}" else ""
            holder.avatarText.text = user.name.firstOrNull()?.uppercase() ?: "D"
            holder.card.setOnClickListener { onClick(user) }
        }

        override fun getItemCount() = items.size
    }
}