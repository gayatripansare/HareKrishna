package com.iskcon.temple

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth

class MoreFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_more, container, false)

        auth = FirebaseAuth.getInstance()

        // Setup click listeners for all options
        setupClickListeners(view)

        // Update Sign In card based on authentication status
        updateSignInCard(view)

        return view
    }

    override fun onResume() {
        super.onResume()
        // Update the card when fragment becomes visible again
        view?.let { updateSignInCard(it) }
    }

    private fun updateSignInCard(view: View) {
        val currentUser = auth.currentUser
        val signInCard = view.findViewById<CardView>(R.id.card_sign_in)

        // You can optionally update the card appearance based on login status
        // For now, we'll handle it in the click listener
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

        // Sign In / Register - Check if user is already logged in
        view.findViewById<CardView>(R.id.card_sign_in)?.setOnClickListener {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // User is already signed in, show options
                showUserOptionsDialog(currentUser.email ?: "User")
            } else {
                // User not signed in, navigate to sign in page
                navigateToSignIn()
            }
        }

        // Scriptures - Open in browser
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
        // Optionally refresh the fragment
        view?.let { updateSignInCard(it) }
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
        // Start the Sign In Activity
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