package com.iskcon.temple

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView

class JapaActivity : AppCompatActivity() {

    // Counting variables
    private var currentCount = 0
    private var totalCount = 108
    private var currentRound = 1
    private var totalRounds = 16
    private var isCounting = false
    private var isFirstCount = true
    private var isPaused = false

    // Mode: 1=Audio, 2=Voice, 3=Manual
    private var currentMode = 1

    // Audio player
    private var mediaPlayer: MediaPlayer? = null
    private var currentSpeed = 1.0f
    private var currentVolume = 0.5f

    // Voice recognition
    private var speechRecognizer: SpeechRecognizer? = null
    private val RECORD_AUDIO_PERMISSION_CODE = 100

    // Handler
    private val handler = Handler(Looper.getMainLooper())
    private var countingRunnable: Runnable? = null

    // Timing for audio mode
    private val initialDelay = 20000L
    private val baseMantraDuration = 6000L
    private var currentInterval = baseMantraDuration

    private var remainingTimeWhenPaused = 0L
    private var pauseTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_japa)

        setupViews()
        checkPermissions()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Microphone permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Microphone permission needed for Voice mode", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupViews() {
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnRefresh = findViewById<ImageView>(R.id.btnRefresh)
        val btnModeAudio = findViewById<Button>(R.id.btnModeAudio)
        val btnModeVoice = findViewById<Button>(R.id.btnModeVoice)
        val btnModeManual = findViewById<Button>(R.id.btnModeManual)
        val tvMantraText = findViewById<TextView>(R.id.tvMantraText)
        val tvCurrentCount = findViewById<TextView>(R.id.tvCurrentCount)
        val tvCurrentRound = findViewById<TextView>(R.id.tvCurrentRound)
        val tvTotalRounds = findViewById<TextView>(R.id.tvTotalRounds)
        val tvPhaseStatus = findViewById<TextView>(R.id.tvPhaseStatus)
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)
        val seekBarVolume = findViewById<SeekBar>(R.id.seekBarVolume)
        val seekBarSpeed = findViewById<SeekBar>(R.id.seekBarSpeed)
        val counterCircle = findViewById<MaterialCardView>(R.id.counterCircle)
        val layoutVolume = findViewById<LinearLayout>(R.id.layoutVolume)
        val layoutSpeed = findViewById<LinearLayout>(R.id.layoutSpeed)

        tvMantraText?.text = "हरे कृष्ण हरे कृष्ण कृष्ण कृष्ण हरे हरे\nहरे राम हरे राम राम राम हरे हरे"

        tvCurrentCount?.text = currentCount.toString()
        tvCurrentRound?.text = currentRound.toString()
        tvTotalRounds?.text = totalRounds.toString()

        seekBarVolume?.progress = 50
        seekBarSpeed?.progress = 50

        // Back button
        btnBack?.setOnClickListener {
            stopEverything()
            finish()
        }

        // Refresh button
        btnRefresh?.setOnClickListener {
            stopEverything()
            currentCount = 0
            currentRound = 1
            isCounting = false
            isFirstCount = true
            isPaused = false
            remainingTimeWhenPaused = 0L
            tvCurrentCount?.text = currentCount.toString()
            tvCurrentRound?.text = currentRound.toString()
            btnStart?.text = "▶ Start"
            updateModeUI()
        }

        // Mode buttons
        btnModeAudio?.setOnClickListener {
            switchMode(1, tvPhaseStatus, layoutVolume, layoutSpeed, btnModeAudio, btnModeVoice, btnModeManual)
        }

        btnModeVoice?.setOnClickListener {
            switchMode(2, tvPhaseStatus, layoutVolume, layoutSpeed, btnModeAudio, btnModeVoice, btnModeManual)
        }

        btnModeManual?.setOnClickListener {
            switchMode(3, tvPhaseStatus, layoutVolume, layoutSpeed, btnModeAudio, btnModeVoice, btnModeManual)
        }

        // Start/Pause button
        btnStart?.setOnClickListener {
            if (!isCounting) {
                isCounting = true
                btnStart.text = "⏸ Pause"
                tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_green_dark))

                when (currentMode) {
                    1 -> { // Audio mode
                        tvPhaseStatus?.text = "Audio Playing"
                        if (isPaused) {
                            resumeAudio()
                            resumeCounting(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
                            isPaused = false
                        } else {
                            startAudio()
                            startAutoCounting(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
                        }
                    }
                    2 -> { // Voice mode
                        tvPhaseStatus?.text = "🎤 Listening..."
                        startVoiceRecognition(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
                    }
                    3 -> { // Manual mode
                        tvPhaseStatus?.text = "Tap to Count"
                    }
                }
            } else {
                isCounting = false
                isPaused = true
                btnStart.text = "▶ Resume"
                tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_orange_dark))
                tvPhaseStatus?.text = "Paused"

                pauseEverything()
            }
        }

        // Stop button
        btnStop?.setOnClickListener {
            isCounting = false
            isPaused = true
            btnStart?.text = "▶ Resume"
            tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_orange_dark))
            tvPhaseStatus?.text = "Stopped"

            pauseEverything()
        }

        // Counter circle - tap in manual mode
        counterCircle?.setOnClickListener {
            if (currentMode == 3 && isCounting) {
                incrementCount(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
            } else if (currentMode == 3 && !isCounting) {
                Toast.makeText(this, "Press Start first", Toast.LENGTH_SHORT).show()
            }
        }

        // Volume slider
        seekBarVolume?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentVolume = progress / 100f
                mediaPlayer?.setVolume(currentVolume, currentVolume)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Speed slider
        seekBarSpeed?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentSpeed = 0.5f + (progress / 100f) * 1.5f

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        val params = PlaybackParams()
                        params.speed = currentSpeed
                        mediaPlayer?.playbackParams = params
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                currentInterval = (baseMantraDuration / currentSpeed).toLong()

                if (isCounting && !isPaused && currentMode == 1) {
                    stopCounting()
                    startAutoCounting(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun switchMode(
        mode: Int,
        tvPhaseStatus: TextView?,
        layoutVolume: LinearLayout?,
        layoutSpeed: LinearLayout?,
        btnAudio: Button?,
        btnVoice: Button?,
        btnManual: Button?
    ) {
        if (isCounting) {
            Toast.makeText(this, "Stop counting before switching mode", Toast.LENGTH_SHORT).show()
            return
        }

        currentMode = mode

        when (mode) {
            1 -> { // Audio mode
                tvPhaseStatus?.text = "Audio Mode"
                tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_green_dark))
                layoutVolume?.visibility = View.VISIBLE
                layoutSpeed?.visibility = View.VISIBLE
                btnAudio?.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_purple)
                btnVoice?.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
                btnManual?.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
                Toast.makeText(this, "🔊 Audio: Auto counting with audio", Toast.LENGTH_SHORT).show()
            }
            2 -> { // Voice mode
                tvPhaseStatus?.text = "Voice Mode"
                tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_blue_dark))
                layoutVolume?.visibility = View.GONE
                layoutSpeed?.visibility = View.GONE
                btnAudio?.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
                btnVoice?.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_purple)
                btnManual?.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
                Toast.makeText(this, "🎤 Voice: Speak mantra to count", Toast.LENGTH_SHORT).show()
            }
            3 -> { // Manual mode
                tvPhaseStatus?.text = "Manual Mode"
                tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_orange_dark))
                layoutVolume?.visibility = View.GONE
                layoutSpeed?.visibility = View.GONE
                btnAudio?.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
                btnVoice?.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
                btnManual?.backgroundTintList = ContextCompat.getColorStateList(this, android.R.color.holo_purple)
                Toast.makeText(this, "👆 Manual: Tap counter to count", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateModeUI() {
        val tvPhaseStatus = findViewById<TextView>(R.id.tvPhaseStatus)
        when (currentMode) {
            1 -> tvPhaseStatus?.text = "Audio Mode"
            2 -> tvPhaseStatus?.text = "Voice Mode"
            3 -> tvPhaseStatus?.text = "Manual Mode"
        }
        tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_green_dark))
    }

    // ========== AUDIO MODE ==========

    private fun startAudio() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.hare_krishna_mantra)
            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(currentVolume, currentVolume)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val params = PlaybackParams()
                    params.speed = currentSpeed
                    mediaPlayer?.playbackParams = params
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun resumeAudio() {
        try {
            if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            startAudio()
        }
    }

    private fun startAutoCounting(
        tvCurrentCount: TextView?,
        tvCurrentRound: TextView?,
        btnStart: Button?,
        tvPhaseStatus: TextView?
    ) {
        countingRunnable = object : Runnable {
            override fun run() {
                if (isCounting) {
                    incrementCount(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
                    handler.postDelayed(this, currentInterval)
                }
            }
        }

        if (isFirstCount) {
            val adjustedInitialDelay = (initialDelay / currentSpeed).toLong()
            handler.postDelayed(countingRunnable!!, adjustedInitialDelay)
            pauseTime = System.currentTimeMillis()
            isFirstCount = false
        } else {
            handler.postDelayed(countingRunnable!!, currentInterval)
            pauseTime = System.currentTimeMillis()
        }
    }

    private fun resumeCounting(
        tvCurrentCount: TextView?,
        tvCurrentRound: TextView?,
        btnStart: Button?,
        tvPhaseStatus: TextView?
    ) {
        countingRunnable = object : Runnable {
            override fun run() {
                if (isCounting) {
                    incrementCount(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
                    handler.postDelayed(this, currentInterval)
                }
            }
        }

        if (remainingTimeWhenPaused > 0) {
            handler.postDelayed(countingRunnable!!, remainingTimeWhenPaused)
        } else {
            handler.postDelayed(countingRunnable!!, currentInterval)
        }
        pauseTime = System.currentTimeMillis()
    }

    private fun stopCounting() {
        countingRunnable?.let { handler.removeCallbacks(it) }
    }

    // ========== VOICE MODE ==========

    private fun startVoiceRecognition(
        tvCurrentCount: TextView?,
        tvCurrentRound: TextView?,
        btnStart: Button?,
        tvPhaseStatus: TextView?
    ) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Grant microphone permission", Toast.LENGTH_LONG).show()
            isCounting = false
            btnStart?.text = "▶ Start"
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                tvPhaseStatus?.text = "🎤 Listening..."
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                if (isCounting) {
                    handler.postDelayed({
                        if (isCounting) startListening()
                    }, 500)
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null && matches.isNotEmpty()) {
                    val spokenText = matches[0].lowercase()

                    if (spokenText.contains("hare") || spokenText.contains("krishna") ||
                        spokenText.contains("rama") || spokenText.contains("कृष्ण") ||
                        spokenText.contains("राम")) {

                        incrementCount(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
                        tvPhaseStatus?.text = "✅ Counted!"
                    }
                }

                if (isCounting) {
                    handler.postDelayed({
                        if (isCounting) startListening()
                    }, 500)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }

        speechRecognizer?.setRecognitionListener(recognitionListener)
        startListening()
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        speechRecognizer?.startListening(intent)
    }

    private fun stopVoiceRecognition() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    // ========== COMMON ==========

    private fun incrementCount(
        tvCurrentCount: TextView?,
        tvCurrentRound: TextView?,
        btnStart: Button?,
        tvPhaseStatus: TextView?
    ) {
        currentCount++

        if (currentCount > totalCount) {
            currentCount = 1
            currentRound++

            if (currentRound > totalRounds) {
                isCounting = false
                isFirstCount = true
                isPaused = false
                btnStart?.text = "🙏 Complete!"
                tvPhaseStatus?.text = "All Rounds Complete!"
                tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_green_dark))
                stopEverything()
                return
            }
        }

        tvCurrentCount?.text = currentCount.toString()
        tvCurrentRound?.text = currentRound.toString()
    }

    private fun pauseEverything() {
        when (currentMode) {
            1 -> {
                val currentTime = System.currentTimeMillis()
                val elapsedTime = currentTime - pauseTime
                val targetInterval = if (isFirstCount) {
                    (initialDelay / currentSpeed).toLong()
                } else {
                    currentInterval
                }
                remainingTimeWhenPaused = (targetInterval - elapsedTime).coerceAtLeast(0)

                stopCounting()
                mediaPlayer?.pause()
            }
            2 -> stopVoiceRecognition()
            3 -> {}
        }
    }

    private fun stopEverything() {
        stopCounting()
        stopVoiceRecognition()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        remainingTimeWhenPaused = 0L
    }

    override fun onDestroy() {
        super.onDestroy()
        stopEverything()
    }

    override fun onPause() {
        super.onPause()
        if (isCounting) {
            pauseEverything()
        }
    }

    override fun onResume() {
        super.onResume()
        if (isCounting) {
            val tvCurrentCount = findViewById<TextView>(R.id.tvCurrentCount)
            val tvCurrentRound = findViewById<TextView>(R.id.tvCurrentRound)
            val btnStart = findViewById<Button>(R.id.btnStart)
            val tvPhaseStatus = findViewById<TextView>(R.id.tvPhaseStatus)

            when (currentMode) {
                1 -> {
                    resumeAudio()
                    resumeCounting(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
                }
                2 -> startVoiceRecognition(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
            }
        }
    }
}