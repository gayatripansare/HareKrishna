package com.iskcon.temple

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment

class ContactUsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contact_us, container, false)

        setupContactOptions(view)

        return view
    }

    private fun setupContactOptions(view: View) {
        // Phone Call
        view.findViewById<CardView>(R.id.card_phone).setOnClickListener {
            val phoneNumber = "tel:+919876543210" // Replace with actual number
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse(phoneNumber))
            startActivity(intent)
        }

        // Email
        view.findViewById<CardView>(R.id.card_email).setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:iskconkopergaon@gmail.com") // Replace with actual email
                putExtra(Intent.EXTRA_SUBJECT, "Inquiry from ISKCON App")
            }
            startActivity(Intent.createChooser(emailIntent, "Send Email"))
        }

        // WhatsApp
        view.findViewById<CardView>(R.id.card_whatsapp).setOnClickListener {
            try {
                val whatsappNumber = "+919876543210" // Replace with actual number
                val url = "https://wa.me/$whatsappNumber"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            }
        }

        // Visit Temple Button - Navigate to new screen
        view.findViewById<Button>(R.id.btn_visit_temple).setOnClickListener {
            val intent = Intent(requireActivity(), MainActivity2::class.java)
            startActivity(intent)
        }
    }
}