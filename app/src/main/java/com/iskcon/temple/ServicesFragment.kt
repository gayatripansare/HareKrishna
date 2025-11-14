package com.iskcon.temple

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class ServicesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_services, container, false)

        setupServiceCards(view)

        return view
    }

    private fun setupServiceCards(view: View) {
        // Main Service Cards
        view.findViewById<CardView>(R.id.card_annadan).setOnClickListener {
            showServiceImageDialog(
                "Annadan (Food Distribution)",
                "Free prasadam distribution to devotees and needy people. Annadana is considered Maha Daan - the highest form of charity in Vedic culture.",
                R.drawable.service_annadan2
            )
        }

        view.findViewById<CardView>(R.id.card_house_program).setOnClickListener {
            showServiceImageDialog(
                "House Program",
                "We conduct Kirtan, Bhagavad Gita lectures, and Srimad Bhagavatam discourses at your home. Invite devotees for spiritual programs and experience divine blessings.",
                R.drawable.service_house_program
            )
        }

        view.findViewById<CardView>(R.id.card_book_distribution).setOnClickListener {
            showServiceImageDialog(
                "Book Distribution",
                "Spiritual literature including Bhagavad Gita, Srimad Bhagavatam, and Krishna books available in Hindi and English. Spread the wisdom of Vedic knowledge.",
                R.drawable.service_books2
            )
        }

        view.findViewById<CardView>(R.id.card_value_education).setOnClickListener {
            showServiceImageDialog(
                "Value Education in Schools",
                "Character building programs teaching moral values, ethics, stress management, and spiritual awareness to students through Vedic wisdom.",
                R.drawable.service_education
            )
        }

        // Other Services - ImageButtons
        view.findViewById<ImageButton>(R.id.btn_donation_service).setOnClickListener {
            val intent = Intent(requireContext(), DonateActivity::class.java)
            startActivity(intent)
            Toast.makeText(context, "Donation - 80G Tax Benefits Available\nContact temple for details", Toast.LENGTH_LONG).show()
        }

        view.findViewById<ImageButton>(R.id.btn_youth_forum).setOnClickListener {
            // Open Youth Forum Activity
            val intent = Intent(requireContext(), YouthForum::class.java)
            startActivity(intent)
        }
    }

    private fun showServiceImageDialog(title: String, description: String, imageResId: Int) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_service_image)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Set content
        val imageView = dialog.findViewById<ImageView>(R.id.service_image)
        val titleView = dialog.findViewById<TextView>(R.id.service_title)
        val descView = dialog.findViewById<TextView>(R.id.service_description)
        val closeButton = dialog.findViewById<ImageView>(R.id.btn_close)

        imageView.setImageResource(imageResId)
        titleView.text = title
        descView.text = description

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}