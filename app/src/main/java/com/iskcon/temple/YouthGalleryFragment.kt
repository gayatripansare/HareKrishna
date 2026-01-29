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

class YouthGalleryFragment : Fragment() {

    private lateinit var rvYouthImages: RecyclerView
    private lateinit var fabAddImage: FloatingActionButton
    private lateinit var progressBar: ProgressBar

    private lateinit var youthAdapter: GalleryAdapter

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val youthImagesList = mutableListOf<GalleryImage>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_youth_gallery, container, false)

        initViews(view)
        setupRecyclerView()
        checkAdminStatus()
        loadYouthImages()

        fabAddImage.setOnClickListener {
            openUploadScreen()
        }

        return view
    }

    private fun initViews(view: View) {
        rvYouthImages = view.findViewById(R.id.rv_youth_images)
        fabAddImage = view.findViewById(R.id.fab_add_youth_image)
        progressBar = view.findViewById(R.id.progress_bar)
    }

    private fun setupRecyclerView() {
        // FIXED: Use named parameters
        youthAdapter = GalleryAdapter(
            imageList = youthImagesList,
            onImageClick = { image ->
                Toast.makeText(requireContext(), "Clicked: ${image.title}", Toast.LENGTH_SHORT).show()
            }
        )
        rvYouthImages.adapter = youthAdapter
        rvYouthImages.layoutManager = GridLayoutManager(requireContext(), 2)
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
                    fabAddImage.visibility = if (role == "admin") View.VISIBLE else View.GONE
                } else {
                    fabAddImage.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                fabAddImage.visibility = View.GONE
            }
    }

    private fun loadYouthImages() {
        progressBar.visibility = View.VISIBLE

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

    private fun openUploadScreen() {
        val intent = Intent(requireContext(), YouthUploadActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadYouthImages()
        Log.d("YouthGallery", "onResume - Reloading youth gallery...")
    }
}