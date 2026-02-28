package com.iskcon.temple

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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

class DarshanFragment : Fragment() {

    private lateinit var fabAddDarshanImage: FloatingActionButton
    private lateinit var rvDarshanImages: RecyclerView
    private lateinit var tvDarshanImagesTitle: TextView
    private lateinit var darshanAdapter: GalleryAdapter

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val darshanImagesList = mutableListOf<GalleryImage>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_darshan, container, false)

        initViews(view)
        setupGalleryButton(view)
        setupDeityImageZoom(view)
        setupDarshanRecyclerView()
        checkAdminStatus()
        loadDarshanImages()

        fabAddDarshanImage.setOnClickListener {
            openUploadScreen()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadDarshanImages()
        checkAdminStatus()
    }

    private fun initViews(view: View) {
        fabAddDarshanImage = view.findViewById(R.id.fab_add_darshan_image)
        rvDarshanImages = view.findViewById(R.id.rv_darshan_images)
        tvDarshanImagesTitle = view.findViewById(R.id.tv_darshan_images_title)
    }

    private fun setupDarshanRecyclerView() {
        darshanAdapter = GalleryAdapter(
            imageList = darshanImagesList,
            onImageClick = { image ->
                // Open zoom on tap
                val intent = Intent(requireContext(), ImageZoomActivity::class.java).apply {
                    putExtra("IMAGE_URL", image.imageUrl)
                    putExtra("IMAGE_TITLE", image.title)
                }
                startActivity(intent)
            },
            onImageLongClick = { image ->
                // ✅ Admin can long-press to delete
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .get()
                        .addOnSuccessListener { document ->
                            val role = document.getString("role")
                            if (role == "admin") {
                                showDeleteDialog(image)
                            } else {
                                Toast.makeText(requireContext(), "Only admins can delete images", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "Please login to delete images", Toast.LENGTH_SHORT).show()
                }
            }
        )
        rvDarshanImages.adapter = darshanAdapter
        rvDarshanImages.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    // ✅ Check admin role - show FAB only for admins
    private fun checkAdminStatus() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            fabAddDarshanImage.visibility = View.GONE
            return
        }

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    if (role == "admin") {
                        fabAddDarshanImage.visibility = View.VISIBLE
                        Log.d("DarshanFragment", "✅ Admin detected - FAB shown")
                    } else {
                        fabAddDarshanImage.visibility = View.GONE
                        Log.d("DarshanFragment", "User is not admin - FAB hidden")
                    }
                } else {
                    fabAddDarshanImage.visibility = View.GONE
                }
            }
            .addOnFailureListener { e ->
                fabAddDarshanImage.visibility = View.GONE
                Log.e("DarshanFragment", "Failed to check admin: ${e.message}")
            }
    }

    // ✅ Load darshan images from Firestore (category = "darshan")
    private fun loadDarshanImages() {
        firestore.collection("gallery_images")
            .whereEqualTo("category", "darshan")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("DarshanFragment", "❌ Error loading darshan images: ${error.message}")
                    return@addSnapshotListener
                }

                darshanImagesList.clear()
                if (snapshots != null && !snapshots.isEmpty) {
                    for (document in snapshots.documents) {
                        val image = document.toObject(GalleryImage::class.java)
                        if (image != null) {
                            darshanImagesList.add(image)
                        }
                    }
                    // Show section title and RecyclerView only when there are images
                    tvDarshanImagesTitle.visibility = View.VISIBLE
                    rvDarshanImages.visibility = View.VISIBLE
                } else {
                    // Hide section if no admin-uploaded darshan images yet
                    tvDarshanImagesTitle.visibility = View.GONE
                    rvDarshanImages.visibility = View.GONE
                }
                darshanAdapter.updateImages(darshanImagesList)
                Log.d("DarshanFragment", "✅ Loaded ${darshanImagesList.size} darshan images")
            }
    }

    // ✅ Open AdminUploadActivity with "darshan" category pre-selected
    private fun openUploadScreen() {
        val intent = Intent(requireContext(), AdminUploadActivity::class.java).apply {
            putExtra("CATEGORY", "darshan")  // Pre-select darshan category
        }
        startActivity(intent)
    }

    // ✅ Show delete confirmation dialog
    private fun showDeleteDialog(image: GalleryImage) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Image")
            .setMessage("Are you sure you want to delete '${image.title}'?")
            .setPositiveButton("Delete") { dialog, _ ->
                firestore.collection("gallery_images")
                    .document(image.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "✅ Image deleted!", Toast.LENGTH_SHORT).show()
                        Log.d("DarshanFragment", "Deleted image: ${image.title}")
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "❌ Delete failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun setupGalleryButton(view: View) {
        val btnViewGallery = view.findViewById<CardView>(R.id.btn_view_gallery)
        btnViewGallery.setOnClickListener {
            navigateToGallery()
        }
    }

    private fun setupDeityImageZoom(view: View) {
        val deity1 = view.findViewById<ImageView>(R.id.iv_deity_1)
        val deity2 = view.findViewById<ImageView>(R.id.iv_deity_2)
        val deity3 = view.findViewById<ImageView>(R.id.iv_deity_3)

        deity1.setOnClickListener { openImageZoom("deity_krishna1", "Sri Sri Radha Krishna") }
        deity2.setOnClickListener { openImageZoom("deity_krishna2", "Sri Krishna") }
        deity3.setOnClickListener { openImageZoom("deity_radha_krishna", "Lord Balaram") }
    }

    private fun openImageZoom(drawableName: String, title: String) {
        val resourceId = resources.getIdentifier(drawableName, "drawable", requireContext().packageName)
        val imageUri = "android.resource://${requireContext().packageName}/$resourceId"
        val intent = Intent(requireContext(), ImageZoomActivity::class.java).apply {
            putExtra("IMAGE_URL", imageUri)
            putExtra("IMAGE_TITLE", title)
        }
        startActivity(intent)
    }

    private fun navigateToGallery() {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, GalleryFragment())
            .addToBackStack("darshan")
            .commit()
        Toast.makeText(requireContext(), "Opening Gallery 📸", Toast.LENGTH_SHORT).show()
    }
}