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

// ✅ SOLUTION: MainActivity2 does NOT extend BaseActivity
// This means NO donation FAB will appear on home screen
class MainActivity2: AppCompatActivity() {

    // Declare variables for UI elements
    private lateinit var mainLayout: ScrollView
    private lateinit var templeListLayout: LinearLayout
    private lateinit var templeDetailLayout: ScrollView
    private lateinit var kopargaonButton: Button
    private lateinit var shirdiButton: Button
    private lateinit var nashikButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        try {
            // Connect variables to UI elements
            mainLayout = findViewById(R.id.mainLayout)
            templeListLayout = findViewById(R.id.templeListLayout)
            templeDetailLayout = findViewById(R.id.templeDetailLayout)
            kopargaonButton = findViewById(R.id.kopargaonButton)
            shirdiButton = findViewById(R.id.shirdiButton)
            nashikButton = findViewById(R.id.nashikButton)

            // Show main page first
            mainLayout.visibility = View.VISIBLE
            templeListLayout.visibility = View.GONE
            templeDetailLayout.visibility = View.GONE

            // Direct location buttons
            kopargaonButton.setOnClickListener {
                showTempleList("Kopargaon")
            }

            shirdiButton.setOnClickListener {
                showTempleList("Shirdi")
            }

            nashikButton.setOnClickListener {
                showTempleList("Nashik")
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
                text = "Sorry, we only have temple information for Kopargaon, Shirdi and Nashik locations.\n\nPlease search for:\n• Kopargaon\n• Shirdi\n• Nashik"
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
                    Temple("Iskcon Kopargaon", "Station Rd, Annapurna Nagar, Kopargaon, Maharashtra 423603", "In City", "4.7",
                        "19.8993645,74.4901565", "+91 7204025975", "5:30 AM - 9:00 PM",
                        "Local ISKCON center in Kopargaon. Regular kirtans, bhajans and prasadam distribution. Peaceful atmosphere for devotion.",
                        R.drawable.radha_2),

                    )
            }
            searchLocation.contains("shirdi") -> {
                listOf(
                    Temple("Shri Sai Baba Samadhi Mandir", "Mauli Nagar, Shirdi, Maharashtra", "In City", "4.9",
                        "19.766180,74.477039", "+91 2423 258777", "4:00 AM - 11:00 PM",
                        "World famous Sai Baba temple. Millions of devotees visit yearly. Divine atmosphere and spiritual experience.",
                        R.drawable.sai_b
                    )
                )
            }
            searchLocation.contains("nashik") -> {
                listOf(
                    Temple("ISKCON Sri Sri Radha Madan Gopal Mandir", "Poornima Stop, Vrindavan Colony, Hare Krishna Road Gen. Vaidya nagar, Dwarka, Nashik, Maharashtra 422011", "In City", "4.8",
                        "19.9871188,73.7969705", "+91 93090 16553", "5:00 AM - 9:00 PM",
                        "Beautiful ISKCON temple in Nashik. Grand architecture, daily darshan, aarti and prasadam. Spiritual oasis in the holy city.",
                        R.drawable.radha_5),
                    Temple("ISKCON Goshala", "Bhagyodaya Colony, Nashik, Maharashtra 422003", "3 km", "4.7",
                        "20.023715,73.779408", "+91 253 2570483", "5:30 AM - 8:30 PM",
                        "Sacred cow shelter and temple. Serve and protect cows while experiencing spiritual atmosphere and devotion.",
                        R.drawable.goshala1)
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

        // Open map button - Opens Google Maps with proper URL
        findViewById<Button>(R.id.openMapButton).setOnClickListener {
            try {
                // Try opening in Google Maps app first
                val gmmIntentUri = Uri.parse("geo:0,0?q=${temple.mapLocation}(${Uri.encode(temple.name)})")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            } catch (e: Exception) {
                // Fallback to web browser
                val coords = temple.mapLocation.split(",")
                val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=${coords[0]},${coords[1]}")
                val intent = Intent(Intent.ACTION_VIEW, webUri)
                startActivity(intent)
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