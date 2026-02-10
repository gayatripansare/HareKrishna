package com.iskcon.temple

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class GalleryFragment : Fragment() {

    private lateinit var rvDeityImages: RecyclerView
    private lateinit var rvEventImages: RecyclerView
    private lateinit var fabAddImage: FloatingActionButton
    private lateinit var progressBar: android.widget.ProgressBar

    private lateinit var deityAdapter: GalleryAdapter
    private lateinit var eventAdapter: GalleryAdapter

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val deityImagesList = mutableListOf<GalleryImage>()
    private val eventImagesList = mutableListOf<GalleryImage>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gallery, container, false)

        initViews(view)
        setupRecyclerViews()
        checkAdminStatus()

        // ✅ NEW: Setup zoom for static gallery images
        setupStaticImageZoom(view)

        fabAddImage.setOnClickListener {
            openUploadScreen()
        }

        // ✅ FEATURE 1: Hide donation FAB in Gallery Fragment
        hideDonationFabInGallery()

        return view
    }

    // ✅ NEW: Hide donation FAB when in gallery fragment
    private fun hideDonationFabInGallery() {
        try {
            // If activity is BaseActivity, hide the donation FAB
            if (activity is BaseActivity) {
                (activity as BaseActivity).hideDonationFab()
            }
        } catch (e: Exception) {
            Log.e("GalleryFragment", "Error hiding donation FAB: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("GalleryFragment", "onResume - Reloading gallery...")
        loadDeityImages()
        loadEventImages()

        // Re-check admin status
        checkAdminStatus()

        // ✅ Hide donation FAB again when returning to fragment
        hideDonationFabInGallery()
    }

    override fun onPause() {
        super.onPause()
        // ✅ Show donation FAB when leaving gallery fragment
        try {
            if (activity is BaseActivity) {
                (activity as BaseActivity).showDonationFab()
            }
        } catch (e: Exception) {
            Log.e("GalleryFragment", "Error showing donation FAB: ${e.message}")
        }
    }

    // ✅ NEW: Setup click listeners for all 4 static gallery images + 2 event images
    private fun setupStaticImageZoom(view: View) {
        // 4 static deity gallery images
        view.findViewById<CardView>(R.id.card_image_1)?.setOnClickListener {
            openImageZoom("deity_krishna3", "Radha Krishna")
        }

        view.findViewById<CardView>(R.id.card_image_2)?.setOnClickListener {
            openImageZoom("deity_krishna4", "Sri Krishna")
        }

        view.findViewById<CardView>(R.id.card_image_3)?.setOnClickListener {
            openImageZoom("deity_krishna5", "Sita Rama")
        }

        view.findViewById<CardView>(R.id.card_image_4)?.setOnClickListener {
            openImageZoom("deity_krishna", "Temple View")
        }

        // 2 static event images
        view.findViewById<CardView>(R.id.card_event_1)?.setOnClickListener {
            openImageZoom("janmastamievent", "Janmashtami Celebration")
        }

        view.findViewById<CardView>(R.id.card_event_2)?.setOnClickListener {
            openImageZoom("event2", "Krishna Jula")
        }
    }

    // ✅ NEW: Open zoom for static drawable images
    private fun openImageZoom(drawableName: String, title: String) {
        val resourceId = resources.getIdentifier(drawableName, "drawable", requireContext().packageName)
        val imageUri = "android.resource://${requireContext().packageName}/$resourceId"

        val intent = Intent(requireContext(), ImageZoomActivity::class.java).apply {
            putExtra("IMAGE_URL", imageUri)
            putExtra("IMAGE_TITLE", title)
        }
        startActivity(intent)
    }

    private fun initViews(view: View) {
        rvDeityImages = view.findViewById(R.id.rv_deity_images)
        rvEventImages = view.findViewById(R.id.rv_event_images)
        fabAddImage = view.findViewById(R.id.fab_add_image)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    private fun setupRecyclerViews() {
        // Setup Deity Images RecyclerView
        deityAdapter = GalleryAdapter(
            imageList = deityImagesList,
            onImageClick = { image ->
                // ✅ UPDATED: Open zoom for dynamic images
                openDynamicImageZoom(image)
            },
            onImageLongClick = { image ->
                // Handle long press - check if admin
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .get()
                        .addOnSuccessListener { document ->
                            val role = document.getString("role")
                            if (role == "admin") {
                                deleteImage(image)
                            } else {
                                Toast.makeText(requireContext(), "Only admins can delete images", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "Please login to delete images", Toast.LENGTH_SHORT).show()
                }
            }
        )
        rvDeityImages.adapter = deityAdapter
        rvDeityImages.layoutManager = GridLayoutManager(requireContext(), 2)

        // Setup Event Images RecyclerView
        eventAdapter = GalleryAdapter(
            imageList = eventImagesList,
            onImageClick = { image ->
                // ✅ UPDATED: Open zoom for dynamic images
                openDynamicImageZoom(image)
            },
            onImageLongClick = { image ->
                // Handle long press - check if admin
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .get()
                        .addOnSuccessListener { document ->
                            val role = document.getString("role")
                            if (role == "admin") {
                                deleteImage(image)
                            } else {
                                Toast.makeText(requireContext(), "Only admins can delete images", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "Please login to delete images", Toast.LENGTH_SHORT).show()
                }
            }
        )
        rvEventImages.adapter = eventAdapter
        rvEventImages.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    // ✅ NEW: Open zoom for dynamic Firestore images
    private fun openDynamicImageZoom(image: GalleryImage) {
        val intent = Intent(requireContext(), ImageZoomActivity::class.java).apply {
            putExtra("IMAGE_URL", image.imageUrl)
            putExtra("IMAGE_TITLE", image.title)
        }
        startActivity(intent)
    }

    private fun checkAdminStatus() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            fabAddImage.visibility = View.GONE
            return
        }

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    if (role == "admin") {
                        fabAddImage.visibility = View.VISIBLE
                    } else {
                        fabAddImage.visibility = View.GONE
                    }
                } else {
                    fabAddImage.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                fabAddImage.visibility = View.GONE
            }
    }

    private fun loadDeityImages() {
        progressBar.visibility = View.VISIBLE

        firestore.collection("gallery_images")
            .whereEqualTo("category", "deity")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                progressBar.visibility = View.GONE

                if (error != null) {
                    Log.e("GalleryFragment", "❌ Error loading deity images: ${error.message}")
                    if (deityImagesList.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Error loading deity images",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@addSnapshotListener
                }

                deityImagesList.clear()
                if (snapshots != null && !snapshots.isEmpty) {
                    for (document in snapshots.documents) {
                        val image = document.toObject(GalleryImage::class.java)
                        if (image != null) {
                            deityImagesList.add(image)
                        }
                    }
                }
                deityAdapter.updateImages(deityImagesList)
                Log.d("GalleryFragment", "✅ Loaded ${deityImagesList.size} deity images")
            }
    }

    private fun loadEventImages() {
        firestore.collection("gallery_images")
            .whereEqualTo("category", "events")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("GalleryFragment", "❌ Error loading event images: ${error.message}")
                    if (eventImagesList.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Error loading event images",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@addSnapshotListener
                }

                eventImagesList.clear()
                if (snapshots != null && !snapshots.isEmpty) {
                    for (document in snapshots.documents) {
                        val image = document.toObject(GalleryImage::class.java)
                        if (image != null) {
                            eventImagesList.add(image)
                        }
                    }
                }
                eventAdapter.updateImages(eventImagesList)
                Log.d("GalleryFragment", "✅ Loaded ${eventImagesList.size} event images")
            }
    }

    private fun openUploadScreen() {
        val intent = Intent(requireContext(), AdminUploadActivity::class.java)
        startActivity(intent)
    }

    // Delete image function
    private fun deleteImage(image: GalleryImage) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Image")
        builder.setMessage("Are you sure you want to delete '${image.title}'?")

        builder.setPositiveButton("Delete") { dialog, _ ->
            progressBar.visibility = View.VISIBLE

            // Delete from Firestore
            firestore.collection("gallery_images")
                .document(image.id)
                .delete()
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "✅ Image deleted!", Toast.LENGTH_SHORT).show()

                    // Real-time listener will automatically update the list
                    Log.d("GalleryFragment", "Image ${image.title} deleted successfully")
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "❌ Delete failed: ${e.message}", Toast.LENGTH_LONG).show()
                    Log.e("GalleryFragment", "Failed to delete image: ${e.message}")
                }

            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
}