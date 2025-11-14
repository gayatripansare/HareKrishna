package com.iskcon.temple

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.view.View
import android.graphics.Typeface
import android.widget.ImageView
import android.widget.ScrollView

class MainActivity2: AppCompatActivity() {

    // Declare variables for UI elements
    private lateinit var mainLayout: ScrollView
    private lateinit var templeListLayout: LinearLayout
    private lateinit var templeDetailLayout: ScrollView
    private lateinit var locationInput: EditText
    private lateinit var findButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        try {
            // Connect variables to UI elements
            mainLayout = findViewById(R.id.mainLayout)
            templeListLayout = findViewById(R.id.templeListLayout)
            templeDetailLayout = findViewById(R.id.templeDetailLayout)
            locationInput = findViewById(R.id.locationInput)
            findButton = findViewById(R.id.findButton)

            // Show main page first
            mainLayout.visibility = View.VISIBLE
            templeListLayout.visibility = View.GONE
            templeDetailLayout.visibility = View.GONE

            // When Find Temples button is clicked
            findButton.setOnClickListener {
                val location = locationInput.text.toString().trim()

                // Check if location is empty
                if (location.isEmpty()) {
                    // Show error message
                    Toast.makeText(
                        this,
                        "Please enter a location to find temples",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Optional: Highlight the input field
                    locationInput.requestFocus()
                } else {
                    // Location is entered, proceed to show temple list
                    showTempleList(location)
                }
            }

            // Back button from temple list
            val backButton = findViewById<TextView>(R.id.backButton)
            backButton.setOnClickListener {
                goBackToMain()
            }

            // Back button from temple detail
            val backButtonDetail = findViewById<TextView>(R.id.backButtonDetail)
            backButtonDetail.setOnClickListener {
                goBackToList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Function to show temple list
    private fun showTempleList(location: String) {
        mainLayout.visibility = View.GONE
        templeListLayout.visibility = View.VISIBLE
        templeDetailLayout.visibility = View.GONE

        // Update location text
        val locationText: TextView = findViewById(R.id.locationText)
        locationText.text = "Temples near $location"

        // Get temple container
        val templeContainer: LinearLayout = findViewById(R.id.templeContainer)
        templeContainer.removeAllViews()

        // Add temples based on location
        val temples = getTemplesForLocation(location)

        if (temples.isEmpty()) {
            // Show message if no temples found
            val noTempleText = TextView(this).apply {
                text = "Sorry, we only have temple information for Kopargaon and Shirdi locations.\n\nPlease search for:\n• Kopargaon\n• Shirdi"
                textSize = 16f
                setTextColor(0xFF666666.toInt())
                gravity = android.view.Gravity.CENTER
                val dp = resources.displayMetrics.density.toInt()
                setPadding(20 * dp, 40 * dp, 20 * dp, 20 * dp)
            }
            templeContainer.addView(noTempleText)
        } else {
            temples.forEach { temple ->
                addTempleCard(templeContainer, temple)
            }
        }
    }

    // Get temples based on location
    private fun getTemplesForLocation(location: String): List<Temple> {
        val searchLocation = location.trim().lowercase()

        return when {
            searchLocation.contains("kopargaon") -> {
                listOf(
                    Temple("🛕 ISKCON Kopargaon", "Station Road, Kopargaon", "In City", "4.7",
                        "19.8826,74.4761", "+91 2423 222000", "5:30 AM - 9:00 PM",
                        "Local ISKCON center in Kopargaon. Regular kirtans, bhajans and prasadam distribution. Peaceful atmosphere for devotion.",
                        R.drawable.radha_2), // Change to your actual drawable name
                    Temple("🛕 Radha Krishna Mandir", "Main Market, Kopargaon", "500m", "4.6",
                        "19.8835,74.4755", "+91 2423 220500", "6:00 AM - 8:30 PM",
                        "Beautiful local temple with daily aarti and festivals. Active community participation and spiritual programs.",
                        R.drawable.radhe_1), // Change to your actual drawable name
                    Temple("🛕 Hare Krishna Temple", "College Road, Kopargaon", "1.2 km", "4.5",
                        "19.8840,74.4770", "+91 2423 221100", "5:00 AM - 9:00 PM",
                        "Serene temple near college area. Perfect for morning and evening prayers with beautiful deity darshan.",
                        R.drawable.radha_3) // Change to your actual drawable name
                )
            }
            searchLocation.contains("shirdi") -> {
                listOf(
                    Temple("🛕 ISKCON Shirdi", "Near Sai Baba Temple, Shirdi", "In City", "4.7",
                        "19.7645,74.4779", "+91 2423 585000", "5:00 AM - 10:00 PM",
                        "Located near the famous Sai Baba temple. Beautiful deities and peaceful atmosphere for devotion.",
                        R.drawable.img) // Change to your actual drawable name
                )
            }
            else -> {
                // Return empty list for other locations
                emptyList()
            }
        }
    }

    // Add temple card to layout
    private fun addTempleCard(container: LinearLayout, temple: Temple) {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFF5F5F5.toInt())
            val dp = resources.displayMetrics.density.toInt()
            setPadding(15 * dp, 15 * dp, 15 * dp, 15 * dp)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 15 * dp)
            }
            isClickable = true
            isFocusable = true
            setOnClickListener {
                showTempleDetail(temple)
            }
        }

        val nameText = TextView(this).apply {
            text = temple.name
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setTextColor(0xFF000000.toInt())
        }

        val addressText = TextView(this).apply {
            text = temple.address
            textSize = 14f
            setTextColor(0xFF666666.toInt())
            val dp = resources.displayMetrics.density.toInt()
            setPadding(0, 5 * dp, 0, 0)
        }

        val detailsText = TextView(this).apply {
            text = "📍 ${temple.distance} away • ⭐ ${temple.rating}"
            textSize = 14f
            setTextColor(0xFFFF9800.toInt())
            val dp = resources.displayMetrics.density.toInt()
            setPadding(0, 5 * dp, 0, 0)
        }

        card.addView(nameText)
        card.addView(addressText)
        card.addView(detailsText)
        container.addView(card)
    }

    // Show temple detail page
    private fun showTempleDetail(temple: Temple) {
        mainLayout.visibility = View.GONE
        templeListLayout.visibility = View.GONE
        templeDetailLayout.visibility = View.VISIBLE

        // Update temple details
        findViewById<TextView>(R.id.detailTempleName).text = temple.name
        findViewById<TextView>(R.id.detailAddress).text = temple.address
        findViewById<TextView>(R.id.detailRating).text = "⭐ ${temple.rating}"
        findViewById<TextView>(R.id.detailPhone).text = temple.phone
        findViewById<TextView>(R.id.detailTiming).text = temple.timing
        findViewById<TextView>(R.id.detailDescription).text = temple.description

        // Display temple image
        val templeImage = findViewById<ImageView>(R.id.detailTempleImage)
        templeImage.setImageResource(temple.imageResource)
        templeImage.visibility = View.VISIBLE

        // Open map button - Opens Google Maps
        findViewById<Button>(R.id.openMapButton).setOnClickListener {
            try {
                val uri = Uri.parse("geo:${temple.mapLocation}?q=${temple.mapLocation}(${temple.name})")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
            } catch (e: Exception) {
                val uri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${temple.mapLocation}")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)
            }
        }

        // Get Directions button
        findViewById<Button>(R.id.directionsButton).setOnClickListener {
            val uri = Uri.parse("google.navigation:q=${temple.mapLocation}&mode=d")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.google.android.apps.maps")
            try {
                startActivity(intent)
            } catch (e: Exception) {
                val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${temple.mapLocation}")
                startActivity(Intent(Intent.ACTION_VIEW, webUri))
            }
        }

        // Call button
        findViewById<Button>(R.id.callButton).setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${temple.phone}"))
            startActivity(intent)
        }
    }

    // Go back to main page
    private fun goBackToMain() {
        mainLayout.visibility = View.VISIBLE
        templeListLayout.visibility = View.GONE
        templeDetailLayout.visibility = View.GONE
    }

    // Go back to list page
    private fun goBackToList() {
        mainLayout.visibility = View.GONE
        templeListLayout.visibility = View.VISIBLE
        templeDetailLayout.visibility = View.GONE
    }
}

// Data class for Temple
data class Temple(
    val name: String,
    val address: String,
    val distance: String,
    val rating: String,
    val mapLocation: String,
    val phone: String,
    val timing: String,
    val description: String,
    val imageResource: Int
)