package com.iskcon.temple

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class AdminSongsActivity : AppCompatActivity() {

    private lateinit var edtSongTitle: EditText
    private lateinit var btnSelectFile: Button
    private lateinit var txtFileName: TextView
    private lateinit var btnUpload: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var txtStatus: TextView

    private var selectedFileUri: Uri? = null
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var cloudinaryHelper: CloudinaryHelper

    companion object {
        private const val PICK_AUDIO_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_songs)

        initViews()
        cloudinaryHelper = CloudinaryHelper(this)
        setupClickListeners()
    }

    private fun initViews() {
        edtSongTitle = findViewById(R.id.edt_song_title)
        btnSelectFile = findViewById(R.id.btn_select_file)
        txtFileName = findViewById(R.id.txt_file_name)
        btnUpload = findViewById(R.id.btn_upload)
        progressBar = findViewById(R.id.progress_bar)
        txtStatus = findViewById(R.id.txt_status)

        findViewById<ImageButton>(R.id.btn_back)?.setOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        btnSelectFile.setOnClickListener {
            selectAudioFile()
        }

        btnUpload.setOnClickListener {
            uploadSong()
        }
    }

    private fun selectAudioFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(
            Intent.createChooser(intent, "Select MP3 File"),
            PICK_AUDIO_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_AUDIO_REQUEST && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
                val fileName = getFileName(uri)
                txtFileName.text = "Selected: $fileName"
                txtFileName.visibility = View.VISIBLE
                btnUpload.isEnabled = true
                Log.d("AdminSongs", "File selected: $fileName")
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "Unknown"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    private fun uploadSong() {
        val title = edtSongTitle.text.toString().trim()
        val fileUri = selectedFileUri

        // Validation
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter song title", Toast.LENGTH_SHORT).show()
            return
        }

        if (fileUri == null) {
            Toast.makeText(this, "Please select an MP3 file", Toast.LENGTH_SHORT).show()
            return
        }

        // Start upload
        setUploadingState(true)
        txtStatus.text = "Uploading to Cloudinary..."

        lifecycleScope.launch {
            try {
                // Upload to Cloudinary
                Log.d("AdminSongs", "Starting upload for: $title")
                val cloudinaryUrl = cloudinaryHelper.uploadAudio(fileUri, "vaishnava_songs")

                Log.d("AdminSongs", "Upload successful! URL: $cloudinaryUrl")
                txtStatus.text = "Saving to Firestore..."

                // Save to Firestore
                val song = hashMapOf(
                    "title" to title,
                    "cloudinaryUrl" to cloudinaryUrl,
                    "artist" to "",
                    "duration" to "",
                    "imageUrl" to "",
                    "category" to "Bhajan",
                    "lyrics" to "",
                    "timestamp" to System.currentTimeMillis()
                )

                firestore.collection("vaishnava_songs")
                    .add(song)
                    .addOnSuccessListener { documentReference ->
                        Log.d("AdminSongs", "Song saved with ID: ${documentReference.id}")
                        setUploadingState(false)
                        txtStatus.text = "✅ Song uploaded successfully!"

                        Toast.makeText(
                            this@AdminSongsActivity,
                            "Song uploaded successfully!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Clear form
                        clearForm()
                    }
                    .addOnFailureListener { e ->
                        Log.e("AdminSongs", "Firestore error: ${e.message}")
                        setUploadingState(false)
                        txtStatus.text = "❌ Failed to save to Firestore"
                        Toast.makeText(
                            this@AdminSongsActivity,
                            "Failed to save: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

            } catch (e: Exception) {
                Log.e("AdminSongs", "Upload error: ${e.message}")
                setUploadingState(false)
                txtStatus.text = "❌ Upload failed: ${e.message}"
                Toast.makeText(
                    this@AdminSongsActivity,
                    "Upload failed: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setUploadingState(isUploading: Boolean) {
        runOnUiThread {
            progressBar.visibility = if (isUploading) View.VISIBLE else View.GONE
            btnUpload.isEnabled = !isUploading
            btnSelectFile.isEnabled = !isUploading
            edtSongTitle.isEnabled = !isUploading
        }
    }

    private fun clearForm() {
        edtSongTitle.text.clear()
        txtFileName.text = ""
        txtFileName.visibility = View.GONE
        selectedFileUri = null
        btnUpload.isEnabled = false
    }
}