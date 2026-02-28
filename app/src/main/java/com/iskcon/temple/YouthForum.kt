package com.iskcon.temple

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class YouthForum : BaseActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var ivQuickImage1: ImageView
    private lateinit var ivQuickImage2: ImageView
    private lateinit var cardQuickImage1: CardView
    private lateinit var cardQuickImage2: CardView

    private var quickImage1: GalleryImage? = null
    private var quickImage2: GalleryImage? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_youth_forum)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Youth Forum"

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initQuickGalleryViews()
        loadRecentYouthImages()
        setupClickListeners()
    }

    private fun initQuickGalleryViews() {
        ivQuickImage1 = findViewById(R.id.iv_quick_youth_1)
        ivQuickImage2 = findViewById(R.id.iv_quick_youth_2)
        cardQuickImage1 = findViewById(R.id.card_quick_youth_1)
        cardQuickImage2 = findViewById(R.id.card_quick_youth_2)

        cardQuickImage1.setOnClickListener {
            quickImage1?.let { image ->
                openImageZoom(image.imageUrl, image.title)
            }
        }

        cardQuickImage2.setOnClickListener {
            quickImage2?.let { image ->
                openImageZoom(image.imageUrl, image.title)
            }
        }
    }

    private fun openImageZoom(imageUrl: String, title: String) {
        val intent = Intent(this, ImageZoomActivity::class.java).apply {
            putExtra("IMAGE_URL", imageUrl)
            putExtra("IMAGE_TITLE", title)
        }
        startActivity(intent)
    }

    private fun loadRecentYouthImages() {
        firestore.collection("gallery_images")
            .whereEqualTo("category", "youth")
            .limit(2)
            .get()
            .addOnSuccessListener { documents ->
                val images = documents.mapNotNull { it.toObject(GalleryImage::class.java) }
                    .sortedByDescending { it.timestamp }

                Log.d("YouthForum", "Fetched ${images.size} images from Firestore")

                if (images.isNotEmpty()) {
                    quickImage1 = images[0]
                    Glide.with(this)
                        .load(images[0].imageUrl)
                        .placeholder(R.drawable.deity_krishna)
                        .error(R.drawable.deity_krishna)
                        .centerCrop()
                        .into(ivQuickImage1)
                    cardQuickImage1.visibility = View.VISIBLE
                }

                if (images.size > 1) {
                    quickImage2 = images[1]
                    Glide.with(this)
                        .load(images[1].imageUrl)
                        .placeholder(R.drawable.deity_krishna)
                        .error(R.drawable.deity_krishna)
                        .centerCrop()
                        .into(ivQuickImage2)
                    cardQuickImage2.visibility = View.VISIBLE
                }

                Log.d("YouthForum", "Loaded ${images.size} quick access images")
            }
            .addOnFailureListener { exception ->
                Log.e("YouthForum", "Error loading quick images: ${exception.message}")
            }
    }

    private fun setupClickListeners() {
        findViewById<Button>(R.id.registerButton)?.setOnClickListener {
            openRegistrationOptions()
        }

        findViewById<Button>(R.id.btn_see_more_youth_images)?.setOnClickListener {
            openYouthGallery()
        }

        findViewById<ImageButton>(R.id.imageButton)?.setOnClickListener {
            Toast.makeText(this, "Communication Skills Development", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.imageButton2)?.setOnClickListener {
            Toast.makeText(this, "Concentration & Focus Training", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.imageButton3)?.setOnClickListener {
            Toast.makeText(this, "Spiritual Prasadam", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.imageButton4)?.setOnClickListener {
            Toast.makeText(this, "Chanting & Meditation", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openYouthGallery() {
        val intent = Intent(this, YouthGalleryActivity::class.java)
        startActivity(intent)
    }

    private fun openRegistrationOptions() {
        val options = arrayOf("Call Temple", "Register via Google Form", "Cancel")

        android.app.AlertDialog.Builder(this)
            .setTitle("Register for Youth Forum")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openPhoneDialer()
                    1 -> openGoogleForm()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun openPhoneDialer() {
        try {
            val phoneNumber = "tel:919876543210"
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse(phoneNumber)
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open dialer", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGoogleForm() {
        try {
            val formUrl = "https://forms.gle/D6rRucLjoueXYbxw6"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open Google Form", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        loadRecentYouthImages()
    }

    @SuppressLint("GestureBackNavigation")
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}