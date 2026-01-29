package com.iskcon.temple

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CloudinaryHelper(private val context: Context) {

    init {
        // Initialize Cloudinary MediaManager if not already initialized
        try {
            MediaManager.get()
            Log.d("CloudinaryHelper", "MediaManager already initialized")
        } catch (e: IllegalStateException) {
            // For UNSIGNED uploads, we only need cloud_name
            val config = mapOf(
                "cloud_name" to CloudinaryConfig.CLOUD_NAME,
                "secure" to true
            )
            MediaManager.init(context, config)
            Log.d("CloudinaryHelper", "MediaManager initialized with cloud_name: ${CloudinaryConfig.CLOUD_NAME}")
        }
    }

    suspend fun uploadImage(
        imageUri: Uri,
        folder: String = CloudinaryConfig.UPLOAD_FOLDER
    ): String = suspendCancellableCoroutine { continuation ->

        Log.d("CloudinaryHelper", "Starting upload for URI: $imageUri")
        Log.d("CloudinaryHelper", "Using preset: ${CloudinaryConfig.UPLOAD_PRESET}")
        Log.d("CloudinaryHelper", "Target folder: $folder")

        val requestId = MediaManager.get()
            .upload(imageUri)
            .unsigned(CloudinaryConfig.UPLOAD_PRESET) // ✅ Using correct preset now
            .option("folder", folder)
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    Log.d("CloudinaryHelper", "✅ Upload started: $requestId")
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    val progress = (bytes.toDouble() / totalBytes.toDouble() * 100).toInt()
                    Log.d("CloudinaryHelper", "📤 Upload progress: $progress% ($bytes / $totalBytes bytes)")
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    Log.d("CloudinaryHelper", "✅ Upload successful!")
                    Log.d("CloudinaryHelper", "Result data: $resultData")

                    val imageUrl = resultData["secure_url"] as? String
                    if (imageUrl != null) {
                        Log.d("CloudinaryHelper", "🖼️ Image URL: $imageUrl")
                        continuation.resume(imageUrl)
                    } else {
                        Log.e("CloudinaryHelper", "❌ No secure_url in response")
                        continuation.resumeWithException(
                            Exception("Failed to get image URL from response")
                        )
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    Log.e("CloudinaryHelper", "❌ Upload error: ${error.description}")
                    Log.e("CloudinaryHelper", "Error code: ${error.code}")
                    continuation.resumeWithException(
                        Exception("Upload failed: ${error.description}")
                    )
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    Log.w("CloudinaryHelper", "⚠️ Upload rescheduled: ${error.description}")
                }
            })
            .dispatch()

        continuation.invokeOnCancellation {
            Log.d("CloudinaryHelper", "❌ Upload cancelled")
            MediaManager.get().cancelRequest(requestId)
        }
    }
}
