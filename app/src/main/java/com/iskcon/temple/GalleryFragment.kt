package com.iskcon.temple

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GalleryFragment : Fragment() {

    private lateinit var rvDeityImages: RecyclerView
    private lateinit var rvEventImages: RecyclerView
    private lateinit var fabAddImage: FloatingActionButton
    private lateinit var progressBar: ProgressBar

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

        fabAddImage.setOnClickListener {
            openUploadScreen()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        Log.d("GalleryFragment", "onResume - Reloading gallery...")
        loadDeityImages()
        loadEventImages()
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
                Toast.makeText(requireContext(), "Clicked: ${image.title}", Toast.LENGTH_SHORT).show()
            },
            onImageLongClick = { image ->
                // ✅ Handle long press - check if admin
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
                Toast.makeText(requireContext(), "Clicked: ${image.title}", Toast.LENGTH_SHORT).show()
            },
            onImageLongClick = { image ->
                // ✅ Handle long press - check if admin
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
        rvEventImages.layoutManager = GridLayoutManager(requireContext(), 1)
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
        Log.d("GalleryFragment", "Loading deity images...")

        firestore.collection("gallery_images")
            .whereEqualTo("category", "deity")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("GalleryFragment", "✅ Loaded ${documents.size()} deity images")

                deityImagesList.clear()

                val sortedImages = documents.mapNotNull { doc ->
                    try {
                        val image = doc.toObject(GalleryImage::class.java)
                        Log.d("GalleryFragment", "Deity image: ${image.title} - ${image.imageUrl}")
                        image
                    } catch (e: Exception) {
                        Log.e("GalleryFragment", "Error parsing image: ${e.message}")
                        null
                    }
                }.sortedByDescending { it.timestamp }

                deityImagesList.addAll(sortedImages)
                deityAdapter.updateImages(deityImagesList)
                progressBar.visibility = View.GONE

                Log.d("GalleryFragment", "Deity adapter updated with ${deityImagesList.size} images")
            }
            .addOnFailureListener { exception ->
                Log.e("GalleryFragment", "❌ Error loading deity images: ${exception.message}")
                Toast.makeText(
                    requireContext(),
                    "Error loading deity images: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = View.GONE
            }
    }

    private fun loadEventImages() {
        Log.d("GalleryFragment", "Loading event images...")

        firestore.collection("gallery_images")
            .whereEqualTo("category", "events")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("GalleryFragment", "✅ Loaded ${documents.size()} event images")

                eventImagesList.clear()

                val sortedImages = documents.mapNotNull { doc ->
                    try {
                        val image = doc.toObject(GalleryImage::class.java)
                        Log.d("GalleryFragment", "Event image: ${image.title} - ${image.imageUrl}")
                        image
                    } catch (e: Exception) {
                        Log.e("GalleryFragment", "Error parsing image: ${e.message}")
                        null
                    }
                }.sortedByDescending { it.timestamp }

                eventImagesList.addAll(sortedImages)
                eventAdapter.updateImages(eventImagesList)

                Log.d("GalleryFragment", "Event adapter updated with ${eventImagesList.size} images")
            }
            .addOnFailureListener { exception ->
                Log.e("GalleryFragment", "❌ Error loading event images: ${exception.message}")
                Toast.makeText(
                    requireContext(),
                    "Error loading event images: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun openUploadScreen() {
        val intent = Intent(requireContext(), AdminUploadActivity::class.java)
        startActivity(intent)
    }

    // ✅ NEW: Delete image function
    private fun deleteImage(image: GalleryImage) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Image")
        builder.setMessage("Are you sure you want to delete '${image.title}'?")

        builder.setPositiveButton("Delete") { dialog, _ ->
            progressBar.visibility = View.VISIBLE

            // Delete from Firestore
            firestore.collection("gallery_images")
                .document(image.id)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "✅ Image deleted!", Toast.LENGTH_SHORT).show()

                    // Reload the gallery
                    loadDeityImages()
                    loadEventImages()
                }
                .addOnFailureListener { e ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "❌ Delete failed: ${e.message}", Toast.LENGTH_LONG).show()
                }

            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }
}