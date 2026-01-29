package com.iskcon.temple

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class YouthGalleryActivity : BaseActivity() {

    private lateinit var rvYouthImages: RecyclerView
    private lateinit var fabAddImage: FloatingActionButton
    private lateinit var progressBar: ProgressBar

    private lateinit var youthAdapter: GalleryAdapter

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val youthImagesList = mutableListOf<GalleryImage>()
    private var isAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youth_gallery)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Youth Gallery"

        initViews()
        setupRecyclerView()
        checkAdminStatus()
        loadYouthImages()

        fabAddImage.setOnClickListener {
            openUploadScreen()
        }
    }

    private fun initViews() {
        rvYouthImages = findViewById(R.id.rv_youth_images)
        fabAddImage = findViewById(R.id.fab_add_youth_image)
        progressBar = findViewById(R.id.progress_bar)
    }

    private fun setupRecyclerView() {
        youthAdapter = GalleryAdapter(
            imageList = youthImagesList,
            onImageClick = { image ->
                handleImageClick(image)
            }
        )
        rvYouthImages.adapter = youthAdapter
        rvYouthImages.layoutManager = GridLayoutManager(this, 2)
    }

    private fun showAdminOptionsDialog(image: GalleryImage) {
        val options = arrayOf("View", "Delete")

        AlertDialog.Builder(this)
            .setTitle(image.title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> Toast.makeText(this, image.title, Toast.LENGTH_SHORT).show()
                    1 -> showDeleteConfirmation(image)
                }
            }
            .show()
    }

    private fun showDeleteConfirmation(image: GalleryImage) {
        AlertDialog.Builder(this)
            .setTitle("Delete Image")
            .setMessage("Are you sure you want to delete '${image.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                deleteImage(image)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteImage(image: GalleryImage) {
        progressBar.visibility = View.VISIBLE

        firestore.collection("youth_gallery_images")
            .document(image.id)
            .delete()
            .addOnSuccessListener {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "✅ Image deleted successfully", Toast.LENGTH_SHORT).show()
                loadYouthImages() // Reload the gallery
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                Toast.makeText(this, "❌ Failed to delete: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("YouthGallery", "Delete failed: ${e.message}")
            }
    }

    private fun loadYouthImages() {
        progressBar.visibility = View.VISIBLE

        // No authentication check needed - public read allowed
        firestore.collection("youth_gallery_images")
            .get()
            .addOnSuccessListener { documents ->
                youthImagesList.clear()
                for (document in documents) {
                    val image = document.toObject(GalleryImage::class.java)
                    youthImagesList.add(image)
                }
                youthImagesList.sortByDescending { it.timestamp }
                youthAdapter.updateImages(youthImagesList)
                progressBar.visibility = View.GONE
                Log.d("YouthGallery", "✅ Loaded ${youthImagesList.size} youth images")
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                Log.e("YouthGallery", "❌ Error loading youth images: ${exception.message}")
            }
    }

    private fun checkAdminStatus() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Not logged in - hide FAB, set isAdmin = false
            fabAddImage.visibility = View.GONE
            isAdmin = false
            return
        }

        // Logged in - check if admin
        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    isAdmin = (role == "admin")
                    fabAddImage.visibility = if (isAdmin) View.VISIBLE else View.GONE
                    Log.d("YouthGallery", "User role: $role, isAdmin: $isAdmin")
                } else {
                    fabAddImage.visibility = View.GONE
                    isAdmin = false
                }
            }
            .addOnFailureListener {
                fabAddImage.visibility = View.GONE
                isAdmin = false
            }
    }

    private fun handleImageClick(image: GalleryImage) {
        if (isAdmin) {
            // Admin: Show delete option
            showAdminOptionsDialog(image)
        } else {
            // Not admin (or not logged in): Just show toast
            Toast.makeText(this, image.title, Toast.LENGTH_SHORT).show()
        }
    }

    private fun openUploadScreen() {
        val intent = Intent(this, YouthUploadActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadYouthImages()
        Log.d("YouthGallery", "onResume - Reloading youth gallery...")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}