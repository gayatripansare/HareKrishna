package com.iskcon.temple

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class DarshanFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_darshan, container, false)

        // Setup View Gallery button click listener
        setupGalleryButton(view)

        // ✅ NEW: Setup zoom for all 3 deity images
        setupDeityImageZoom(view)

        return view
    }

    private fun setupGalleryButton(view: View) {
        val btnViewGallery = view.findViewById<CardView>(R.id.btn_view_gallery)

        btnViewGallery.setOnClickListener {
            // Navigate to Gallery Fragment
            navigateToGallery()
        }
    }

    // ✅ NEW: Setup click listeners for zoom on all 3 deity images
    private fun setupDeityImageZoom(view: View) {
        val deity1 = view.findViewById<ImageView>(R.id.iv_deity_1)
        val deity2 = view.findViewById<ImageView>(R.id.iv_deity_2)
        val deity3 = view.findViewById<ImageView>(R.id.iv_deity_3)

        deity1.setOnClickListener {
            openImageZoom("deity_krishna1", "Sri Sri Radha Krishna")
        }

        deity2.setOnClickListener {
            openImageZoom("deity_krishna2", "Sri Krishna")
        }

        deity3.setOnClickListener {
            openImageZoom("deity_radha_krishna", "Lord Balaram")
        }
    }

    // ✅ NEW: Open zoom activity for static drawable images
    private fun openImageZoom(drawableName: String, title: String) {
        // For drawable resources, we'll convert to URI
        val resourceId = resources.getIdentifier(drawableName, "drawable", requireContext().packageName)
        val imageUri = "android.resource://${requireContext().packageName}/$resourceId"

        val intent = Intent(requireContext(), ImageZoomActivity::class.java).apply {
            putExtra("IMAGE_URL", imageUri)
            putExtra("IMAGE_TITLE", title)
        }
        startActivity(intent)
    }

    private fun navigateToGallery() {
        // Create Gallery Fragment instance
        val galleryFragment = GalleryFragment()

        // Navigate to Gallery
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, galleryFragment)
            .addToBackStack("darshan") // This allows back navigation
            .commit()

        // Show a toast for confirmation
        Toast.makeText(
            requireContext(),
            "Opening Gallery 📸",
            Toast.LENGTH_SHORT
        ).show()
    }
}