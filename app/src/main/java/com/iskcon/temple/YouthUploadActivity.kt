package com.iskcon.temple

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class YouthUploadActivity : AppCompatActivity() {

    private lateinit var ivImagePreview: ImageView
    private lateinit var tvPreviewPlaceholder: TextView
    private lateinit var btnChooseImage: CardView
    private lateinit var etImageTitle: TextInputEditText
    private lateinit var btnUpload: CardView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView

    private var selectedImageUri: Uri? = null
    private lateinit var cloudinaryHelper: CloudinaryHelper
    private val firestore = FirebaseFirestore.getInstance()

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            if (selectedImageUri != null) {
                ivImagePreview.setImageURI(selectedImageUri)
                tvPreviewPlaceholder.visibility = View.GONE
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youth_upload)

        cloudinaryHelper = CloudinaryHelper(this)
        initViews()
        setupClickListeners()
    }

    private fun initViews() {
        ivImagePreview = findViewById(R.id.iv_image_preview)
        tvPreviewPlaceholder = findViewById(R.id.tv_preview_placeholder)
        btnChooseImage = findViewById(R.id.btn_choose_image)
        etImageTitle = findViewById(R.id.et_image_title)
        btnUpload = findViewById(R.id.btn_upload)
        progressBar = findViewById(R.id.progress_bar)
        tvProgress = findViewById(R.id.tv_progress)
    }

    private fun setupClickListeners() {
        btnChooseImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            imagePickerLauncher.launch(intent)
        }

        btnUpload.setOnClickListener {
            validateAndUpload()
        }
    }

    private fun validateAndUpload() {
        val title = etImageTitle.text.toString().trim()

        when {
            selectedImageUri == null -> {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            }
            title.isEmpty() -> {
                etImageTitle.error = "Please enter title"
                etImageTitle.requestFocus()
            }
            else -> {
                uploadImage(title)
            }
        }
    }

    private fun uploadImage(title: String) {
        showProgress(true)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                tvProgress.text = "Uploading to cloud..."
                val imageUrl = withContext(Dispatchers.IO) {
                    cloudinaryHelper.uploadImage(selectedImageUri!!)
                }

                tvProgress.text = "Saving to database..."
                saveToFirestore(imageUrl, title)

            } catch (e: Exception) {
                showProgress(false)
                Toast.makeText(
                    this@YouthUploadActivity,
                    "Upload failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun saveToFirestore(imageUrl: String, title: String) {
        val imageData = hashMapOf(
            "imageUrl" to imageUrl,
            "title" to title,
            "category" to "youth_events",
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("youth_gallery_images")
            .add(imageData)
            .addOnSuccessListener { documentReference ->
                documentReference.update("id", documentReference.id)
                    .addOnSuccessListener {
                        showProgress(false)
                        Toast.makeText(this, "✅ Youth image uploaded successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
            }
            .addOnFailureListener { e ->
                showProgress(false)
                Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showProgress(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        tvProgress.visibility = if (show) View.VISIBLE else View.GONE
        btnUpload.isEnabled = !show
        btnChooseImage.isEnabled = !show
    }
}