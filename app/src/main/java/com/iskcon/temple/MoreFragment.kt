package com.iskcon.temple

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MoreFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()

    // Admin section views
    private var adminSection: LinearLayout? = null
    private var btnManageFestivals: CardView? = null
    private var btnAddEvent: CardView? = null
    private var btnUploadGallery: CardView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_more, container, false)

        auth = FirebaseAuth.getInstance()

        // Initialize admin views (they might not exist in XML yet)
        try {
            adminSection = view.findViewById(R.id.admin_section)
            btnManageFestivals = view.findViewById(R.id.btn_manage_festivals)
            btnAddEvent = view.findViewById(R.id.btn_add_event)
            btnUploadGallery = view.findViewById(R.id.btn_upload_gallery)

            // Setup admin click listeners
            setupAdminListeners()
        } catch (e: Exception) {
            // Admin section doesn't exist in XML yet, that's okay
        }

        // Setup regular click listeners for all options
        setupClickListeners(view)

        // Update Sign In card based on authentication status
        updateSignInCard(view)

        // Check if user is admin and show admin section
        checkAdminStatus()

        return view
    }

    override fun onResume() {
        super.onResume()
        view?.let { updateSignInCard(it) }
        checkAdminStatus()
    }

    private fun setupAdminListeners() {
        btnManageFestivals?.setOnClickListener {
            startActivity(Intent(requireContext(), AdminFestivalActivity::class.java))
        }

        btnAddEvent?.setOnClickListener {
            startActivity(Intent(requireContext(), AdminEventActivity::class.java))
        }

        btnUploadGallery?.setOnClickListener {
            startActivity(Intent(requireContext(), AdminUploadActivity::class.java))
        }
    }

    private fun checkAdminStatus() {
        val currentUser = auth.currentUser
        if (currentUser == null || adminSection == null) {
            adminSection?.visibility = View.GONE
            return
        }

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    if (role == "admin") {
                        adminSection?.visibility = View.VISIBLE
                    } else {
                        adminSection?.visibility = View.GONE
                    }
                } else {
                    adminSection?.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                adminSection?.visibility = View.GONE
            }
    }

    private fun updateSignInCard(view: View) {
        // Existing logic remains the same
    }

    private fun setupClickListeners(view: View) {
        // About Us
        view.findViewById<CardView>(R.id.card_about_us)?.setOnClickListener {
            navigateToAboutUs()
        }

        // Contact Us
        view.findViewById<CardView>(R.id.card_contact_us)?.setOnClickListener {
            navigateToContactUs()
        }

        // Sign In / Register
        view.findViewById<CardView>(R.id.card_sign_in)?.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                showUserOptionsDialog(currentUser.email ?: "User")
            } else {
                navigateToSignIn()
            }
        }

        // Scriptures
        view.findViewById<CardView>(R.id.card_scriptures)?.setOnClickListener {
            openScripturesInBrowser()
        }

        // Share App
        view.findViewById<CardView>(R.id.card_share_app)?.setOnClickListener {
            shareApp()
        }

        // Rate App
        view.findViewById<CardView>(R.id.card_rate_app)?.setOnClickListener {
            Toast.makeText(context, "Thank you for your support! ⭐", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openScripturesInBrowser() {
        try {
            val url = "https://vedabase.io/en/library/"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open browser", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showUserOptionsDialog(email: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Account")
            .setMessage("Signed in as:\n$email")
            .setPositiveButton("Sign Out") { dialog, _ ->
                signOutUser()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun signOutUser() {
        auth.signOut()
        Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
        view?.let { updateSignInCard(it) }
        checkAdminStatus() // Hide admin section after logout
    }

    private fun navigateToAboutUs() {
        val aboutUsFragment = AboutUsFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, aboutUsFragment)
            .addToBackStack("more")
            .commit()
    }

    private fun navigateToContactUs() {
        val contactUsFragment = ContactUsFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, contactUsFragment)
            .addToBackStack("more")
            .commit()
    }

    private fun navigateToSignIn() {
        val intent = Intent(requireContext(), ISKON_Sign_in::class.java)
        startActivity(intent)
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "ISKCON Kopergaon App")
            putExtra(
                Intent.EXTRA_TEXT,
                "Check out ISKCON Kopergaon app for divine darshan and temple updates!\n\nHare Krishna! 🙏"
            )
        }
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }
}