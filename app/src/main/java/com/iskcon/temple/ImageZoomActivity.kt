package com.iskcon.temple

import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class ImageZoomActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnClose: ImageView

    private var scaleFactor = 1.0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var posX = 0f
    private var posY = 0f

    private lateinit var scaleDetector: ScaleGestureDetector
    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_zoom)

        // Hide system UI for fullscreen
        hideSystemUI()

        imageView = findViewById(R.id.iv_zoom_image)
        progressBar = findViewById(R.id.progress_bar)
        btnClose = findViewById(R.id.btn_close)

        // Get image URL from intent
        val imageUrl = intent.getStringExtra("IMAGE_URL")
        val imageTitle = intent.getStringExtra("IMAGE_TITLE")

        if (imageUrl.isNullOrEmpty()) {
            finish()
            return
        }

        // Setup gesture detectors
        scaleDetector = ScaleGestureDetector(this, ScaleListener())
        gestureDetector = GestureDetector(this, GestureListener())

        // Load image
        loadImage(imageUrl)

        // Close button
        btnClose.setOnClickListener {
            finish()
        }
    }

    private fun loadImage(imageUrl: String) {
        progressBar.visibility = View.VISIBLE

        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.deity_krishna)
            .error(R.drawable.deity_krishna)
            .into(imageView)

        progressBar.visibility = View.GONE
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (scaleFactor > 1.0f) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY

                    posX += dx
                    posY += dy

                    imageView.translationX = posX
                    imageView.translationY = posY

                    lastTouchX = event.x
                    lastTouchY = event.y
                }
            }
        }

        return true
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.5f, 5.0f)

            imageView.scaleX = scaleFactor
            imageView.scaleY = scaleFactor

            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            if (scaleFactor > 1.0f) {
                // Reset zoom
                scaleFactor = 1.0f
                posX = 0f
                posY = 0f
                imageView.scaleX = 1.0f
                imageView.scaleY = 1.0f
                imageView.translationX = 0f
                imageView.translationY = 0f
            } else {
                // Zoom in
                scaleFactor = 2.5f
                imageView.scaleX = scaleFactor
                imageView.scaleY = scaleFactor
            }
            return true
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    }
}