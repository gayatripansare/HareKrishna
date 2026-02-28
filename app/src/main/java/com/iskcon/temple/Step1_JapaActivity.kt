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

    private var currentCount = 0
    private var totalCount = 108
    private var currentRound = 1
    private var totalRounds = 16
    private var isCounting = false
    private var isFirstCount = true
    private var isPaused = false
    private var currentMode = 1

    private var mediaPlayer: MediaPlayer? = null
    private var currentSpeed = 1.0f
    private var currentVolume = 0.5f

    private var speechRecognizer: SpeechRecognizer? = null
    private val RECORD_AUDIO_PERMISSION_CODE = 100

    private var mantraDetectedInSession = false
    private var speechStartTime = 0L
    private var hasCountedThisSession = false
    private var mantraWordCount = 0

    // ✅ Reduced timing for better detection
    private val MIN_SPEAKING_TIME_MS = 2000L
    private val MIN_MANTRA_WORDS = 2

    private val handler = Handler(Looper.getMainLooper())
    private var countingRunnable: Runnable? = null

    private val initialDelay = 20000L
    private val baseMantraDuration = 6000L
    private var currentInterval = baseMantraDuration
    private var remainingTimeWhenPaused = 0L
    private var pauseTime = 0L

    // ✅ More keyword variants for better recognition
    private val mantraKeywords = listOf(
        "hare", "हरे", "krishna", "कृष्ण", "krishn", "krish",
        "rama", "राम", "ram", "hari", "हरि",
        "harey", "hrey", "krsna", "raam", "hara",
        "हरे कृष्ण", "हरे राम", "क्रिष्ण", "हरि"
    )

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
            val msg = if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                "Microphone permission granted 🙏"
            else
                "Microphone permission needed for Voice mode"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupViews() {
        val btnBack        = findViewById<ImageView>(R.id.btnBack)
        val btnRefresh     = findViewById<ImageView>(R.id.btnRefresh)
        val btnModeAudio   = findViewById<Button>(R.id.btnModeAudio)
        val btnModeVoice   = findViewById<Button>(R.id.btnModeVoice)
        val btnModeManual  = findViewById<Button>(R.id.btnModeManual)
        val tvMantraText   = findViewById<TextView>(R.id.tvMantraText)
        val tvCurrentCount = findViewById<TextView>(R.id.tvCurrentCount)
        val tvCurrentRound = findViewById<TextView>(R.id.tvCurrentRound)
        val tvTotalRounds  = findViewById<TextView>(R.id.tvTotalRounds)
        val tvPhaseStatus  = findViewById<TextView>(R.id.tvPhaseStatus)
        val btnStart       = findViewById<Button>(R.id.btnStart)
        val btnStop        = findViewById<Button>(R.id.btnStop)
        val seekBarVolume  = findViewById<SeekBar>(R.id.seekBarVolume)
        val seekBarSpeed   = findViewById<SeekBar>(R.id.seekBarSpeed)
        val counterCircle  = findViewById<MaterialCardView>(R.id.counterCircle)
        val layoutVolume   = findViewById<LinearLayout>(R.id.layoutVolume)
        val layoutSpeed    = findViewById<LinearLayout>(R.id.layoutSpeed)

        tvMantraText?.text = "हरे कृष्ण हरे कृष्ण कृष्ण कृष्ण हरे हरे\nहरे राम हरे राम राम राम हरे हरे"
        tvCurrentCount?.text = currentCount.toString()
        tvCurrentRound?.text = currentRound.toString()
        tvTotalRounds?.text  = totalRounds.toString()
        seekBarVolume?.progress = 50
        seekBarSpeed?.progress  = 50

        btnBack?.setOnClickListener { stopEverything(); finish() }

        btnRefresh?.setOnClickListener {
            stopEverything()
            currentCount = 0
            currentRound = 1
            isCounting = false
            isFirstCount = true
            isPaused = false
            remainingTimeWhenPaused = 0L
            resetVoiceSession()
            tvCurrentCount?.text = "0"
            tvCurrentRound?.text = "1"
            btnStart?.text = "▶ Start"
            updateModeUI()
        }

        btnModeAudio?.setOnClickListener {
            switchMode(1, tvPhaseStatus, layoutVolume, layoutSpeed, btnModeAudio, btnModeVoice, btnModeManual)
        }
        btnModeVoice?.setOnClickListener {
            switchMode(2, tvPhaseStatus, layoutVolume, layoutSpeed, btnModeAudio, btnModeVoice, btnModeManual)
        }
        btnModeManual?.setOnClickListener {
            switchMode(3, tvPhaseStatus, layoutVolume, layoutSpeed, btnModeAudio, btnModeVoice, btnModeManual)
        }

        btnStart?.setOnClickListener {
            if (!isCounting) {
                isCounting = true
                btnStart.text = "⏸ Pause"
                tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_green_dark))
                when (currentMode) {
                    1 -> {
                        tvPhaseStatus?.text = "🔊 Audio Playing"
                        if (isPaused) {
                            resumeAudio()
                            resumeCounting(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
                            isPaused = false
                        } else {
                            startAudio()
                            startAutoCounting(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
                        }
                    }
                    2 -> {
                        resetVoiceSession()
                        tvPhaseStatus?.text = "🎤 Speak full mantra..."
                        tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_blue_dark))
                        startVoiceRecognition(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
                    }
                    3 -> tvPhaseStatus?.text = "👆 Tap to Count"
                }
            } else {
                isCounting = false
                isPaused = true
                btnStart.text = "▶ Resume"
                tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_orange_dark))
                tvPhaseStatus?.text = "⏸ Paused"
                pauseEverything()
            }
        }

        btnStop?.setOnClickListener {
            isCounting = false
            isPaused = true
            btnStart?.text = "▶ Resume"
            tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_orange_dark))
            tvPhaseStatus?.text = "⏹ Stopped"
            pauseEverything()
        }

        counterCircle?.setOnClickListener {
            if (currentMode == 3 && isCounting) {
                incrementCount(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
            } else if (currentMode == 3) {
                Toast.makeText(this, "Press Start first", Toast.LENGTH_SHORT).show()
            }
        }

        seekBarVolume?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentVolume = progress / 100f
                mediaPlayer?.setVolume(currentVolume, currentVolume)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarSpeed?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentSpeed = 0.5f + (progress / 100f) * 1.5f
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        val p = PlaybackParams()
                        p.speed = currentSpeed
                        mediaPlayer?.playbackParams = p
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
        val purple = ContextCompat.getColorStateList(this, android.R.color.holo_purple)
        val gray   = ContextCompat.getColorStateList(this, android.R.color.darker_gray)
        when (mode) {
            1 -> {
                tvPhaseStatus?.text = "🔊 Audio Mode"
                tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_green_dark))
                layoutVolume?.visibility = View.VISIBLE
                layoutSpeed?.visibility  = View.VISIBLE
                btnAudio?.backgroundTintList  = purple
                btnVoice?.backgroundTintList  = gray
                btnManual?.backgroundTintList = gray
                Toast.makeText(this, "🔊 Audio: Auto counting with audio", Toast.LENGTH_SHORT).show()
            }
            2 -> {
                tvPhaseStatus?.text = "🎤 Voice Mode"
                tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_blue_dark))
                layoutVolume?.visibility = View.GONE
                layoutSpeed?.visibility  = View.GONE
                btnAudio?.backgroundTintList  = gray
                btnVoice?.backgroundTintList  = purple
                btnManual?.backgroundTintList = gray
                Toast.makeText(this, "🎤 Speak full mantra → count +1", Toast.LENGTH_SHORT).show()
            }
            3 -> {
                tvPhaseStatus?.text = "👆 Manual Mode"
                tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_orange_dark))
                layoutVolume?.visibility = View.GONE
                layoutSpeed?.visibility  = View.GONE
                btnAudio?.backgroundTintList  = gray
                btnVoice?.backgroundTintList  = gray
                btnManual?.backgroundTintList = purple
                Toast.makeText(this, "👆 Manual: Tap counter to count", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateModeUI() {
        val tvPhaseStatus = findViewById<TextView>(R.id.tvPhaseStatus)
        tvPhaseStatus?.text = when (currentMode) {
            1 -> "🔊 Audio Mode"
            2 -> "🎤 Voice Mode"
            else -> "👆 Manual Mode"
        }
        tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_green_dark))
    }

    // ==================== AUDIO MODE ====================

    private fun startAudio() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.hare_krishna_mantra)
            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(currentVolume, currentVolume)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    val p = PlaybackParams()
                    p.speed = currentSpeed
                    mediaPlayer?.playbackParams = p
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
            if (mediaPlayer != null && !mediaPlayer!!.isPlaying) mediaPlayer?.start()
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
        val delay = if (isFirstCount) (initialDelay / currentSpeed).toLong() else currentInterval
        handler.postDelayed(countingRunnable!!, delay)
        isFirstCount = false
        pauseTime = System.currentTimeMillis()
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
        handler.postDelayed(
            countingRunnable!!,
            if (remainingTimeWhenPaused > 0) remainingTimeWhenPaused else currentInterval
        )
        pauseTime = System.currentTimeMillis()
    }

    private fun stopCounting() {
        countingRunnable?.let { handler.removeCallbacks(it) }
    }

    // ==================== VOICE MODE ====================

    private fun resetVoiceSession() {
        mantraDetectedInSession = false
        hasCountedThisSession   = false
        speechStartTime         = 0L
        mantraWordCount         = 0
    }

    private fun countMantraWordsInText(text: String): Int {
        val lower = text.lowercase()
        return mantraKeywords.sumOf { keyword ->
            var count = 0
            var index = lower.indexOf(keyword.lowercase())
            while (index >= 0) {
                count++
                index = lower.indexOf(keyword.lowercase(), index + 1)
            }
            count
        }
    }

    private fun startVoiceRecognition(
        tvCurrentCount: TextView?,
        tvCurrentRound: TextView?,
        btnStart: Button?,
        tvPhaseStatus: TextView?
    ) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Grant microphone permission first", Toast.LENGTH_LONG).show()
            isCounting = false
            btnStart?.text = "▶ Start"
            return
        }

        speechRecognizer?.destroy()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {
                resetVoiceSession()
                tvPhaseStatus?.text = "🎤 Ready... speak mantra now"
            }

            override fun onBeginningOfSpeech() {
                speechStartTime = System.currentTimeMillis()
                tvPhaseStatus?.text = "🙏 Listening... chant full mantra"
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val text = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: return

                val wordsHeard = countMantraWordsInText(text)
                if (wordsHeard > 0) {
                    mantraDetectedInSession = true
                    mantraWordCount = wordsHeard
                    tvPhaseStatus?.text = "🙏 Hearing... ($wordsHeard mantra words)"
                }
            }

            override fun onResults(results: Bundle?) {
                val finalText = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: ""

                val finalWordCount  = countMantraWordsInText(finalText)
                val totalWordsHeard = maxOf(mantraWordCount, finalWordCount)

                val speakingDuration = if (speechStartTime > 0)
                    System.currentTimeMillis() - speechStartTime else 0L

                val enoughWords = totalWordsHeard >= MIN_MANTRA_WORDS
                val enoughTime  = speakingDuration >= MIN_SPEAKING_TIME_MS
                val mantraHeard = mantraDetectedInSession || finalWordCount > 0

                if ((mantraHeard || finalWordCount > 0)
                    && (enoughWords || enoughTime)
                    && isCounting
                    && !hasCountedThisSession
                ) {
                    // ✅ Valid mantra → count +1
                    hasCountedThisSession = true
                    incrementCount(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
                    tvPhaseStatus?.text = "✅ +1 counted! Speak next mantra..."
                } else if (!mantraHeard && isCounting) {
                    tvPhaseStatus?.text = "❌ Not heard. Speak: हरे कृष्ण हरे कृष्ण..."
                } else if (mantraHeard && !enoughWords && !enoughTime && isCounting) {
                    tvPhaseStatus?.text = "🔄 Too short! Speak full mantra"
                }

                resetVoiceSession()

                // ✅ Restart listening after short delay
                if (isCounting) {
                    handler.postDelayed({ if (isCounting) startListening() }, 400)
                }
            }

            override fun onError(error: Int) {
                resetVoiceSession()
                if (isCounting) {
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH       -> "🎤 Not heard. Speak mantra..."
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "🎤 Timeout. Speak mantra..."
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "⏳ Busy. Please wait..."
                        SpeechRecognizer.ERROR_AUDIO          -> "🎤 Audio error. Retrying..."
                        else                                   -> "🎤 Speak mantra..."
                    }
                    val tv = findViewById<TextView>(R.id.tvPhaseStatus)
                    tv?.text = msg
                    // ✅ Retry listening
                    handler.postDelayed({ if (isCounting) startListening() }, 600)
                }
            }

            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                tvPhaseStatus?.text = "⏳ Processing mantra..."
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        startListening()
    }

    private fun startListening() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000L)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            if (isCounting) {
                handler.postDelayed({ if (isCounting) startListening() }, 1000)
            }
        }
    }

    private fun stopVoiceRecognition() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==================== COMMON ====================

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
                isCounting   = false
                isFirstCount = true
                isPaused     = false
                btnStart?.text = "🙏 Complete!"
                tvPhaseStatus?.text = "🎉 All Rounds Complete! Hare Krishna!"
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
                val elapsed = System.currentTimeMillis() - pauseTime
                val target  = if (isFirstCount) (initialDelay / currentSpeed).toLong() else currentInterval
                remainingTimeWhenPaused = (target - elapsed).coerceAtLeast(0)
                stopCounting()
                mediaPlayer?.pause()
            }
            2 -> stopVoiceRecognition()
            3 -> { /* nothing */ }
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
        if (isCounting) pauseEverything()
    }

    override fun onResume() {
        super.onResume()
        if (isCounting) {
            val tvCurrentCount = findViewById<TextView>(R.id.tvCurrentCount)
            val tvCurrentRound = findViewById<TextView>(R.id.tvCurrentRound)
            val btnStart       = findViewById<Button>(R.id.btnStart)
            val tvPhaseStatus  = findViewById<TextView>(R.id.tvPhaseStatus)
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