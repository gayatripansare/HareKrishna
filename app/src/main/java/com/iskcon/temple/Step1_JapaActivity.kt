package com.iskcon.temple

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class JapaActivity : AppCompatActivity() {

    private var currentCount = 0
    private var totalCount = 108
    private var currentRound = 1
    private var totalRounds = 16
    private var isCounting = false
    private var isFirstCount = true
    private var isPaused = false

    private var mediaPlayer: MediaPlayer? = null
    private var currentSpeed = 1.0f
    private var currentVolume = 0.5f

    private val handler = Handler(Looper.getMainLooper())
    private var countingRunnable: Runnable? = null

    // Timing: First count at 20 seconds (0:20), then every 6 seconds
    private val initialDelay = 20000L
    private val baseMantraDuration = 6000L
    private var currentInterval = baseMantraDuration

    private var remainingTimeWhenPaused = 0L
    private var pauseTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_japa)

        setupViews()
    }

    private fun setupViews() {
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnRefresh = findViewById<ImageView>(R.id.btnRefresh)
        val btnStats = findViewById<ImageView>(R.id.btnStats)
        val tvMantraText = findViewById<TextView>(R.id.tvMantraText)
        val tvCurrentCount = findViewById<TextView>(R.id.tvCurrentCount)
        val tvCurrentRound = findViewById<TextView>(R.id.tvCurrentRound)
        val tvTotalRounds = findViewById<TextView>(R.id.tvTotalRounds)
        val tvPhaseStatus = findViewById<TextView>(R.id.tvPhaseStatus)
        val btnStart = findViewById<Button>(R.id.btnStart)
        val btnStop = findViewById<Button>(R.id.btnStop)
        val seekBarVolume = findViewById<SeekBar>(R.id.seekBarVolume)
        val seekBarSpeed = findViewById<SeekBar>(R.id.seekBarSpeed)

        // Set mantra text and keep it static (no word-by-word)
        tvMantraText?.text = "हरे कृष्ण हरे कृष्ण कृष्ण कृष्ण हरे हरे\nहरे राम हरे राम राम राम हरे हरे"

        tvCurrentCount?.text = currentCount.toString()
        tvCurrentRound?.text = currentRound.toString()
        tvTotalRounds?.text = totalRounds.toString()

        seekBarVolume?.progress = 50
        seekBarSpeed?.progress = 50

        btnBack?.setOnClickListener {
            stopEverything()
            finish()
        }

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
            tvPhaseStatus?.text = "Japa Counting"
            tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_green_dark))
        }

        btnStats?.setOnClickListener {
            // TODO: Statistics
        }

        btnStart?.setOnClickListener {
            if (!isCounting) {
                // START or RESUME
                isCounting = true
                btnStart.text = "⏸ Pause"
                tvPhaseStatus?.text = "Japa Counting"
                tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_green_dark))

                if (isPaused) {
                    // Resume from pause
                    resumeAudio()
                    resumeCounting(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
                    isPaused = false
                } else {
                    // Start fresh
                    startAudio()
                    startAutoCounting(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
                }
            } else {
                // PAUSE
                isCounting = false
                isPaused = true
                btnStart.text = "▶ Resume"
                tvPhaseStatus?.text = "Round Paused"
                tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_orange_dark))

                pauseEverything()
            }
        }

        btnStop?.setOnClickListener {
            isCounting = false
            isPaused = true
            btnStart?.text = "▶ Resume"
            tvPhaseStatus?.text = "Round Paused"
            tvPhaseStatus?.setBackgroundColor(getColor(android.R.color.holo_orange_dark))

            pauseEverything()
        }

        // Volume slider - controls audio volume only
        seekBarVolume?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentVolume = progress / 100f
                mediaPlayer?.setVolume(currentVolume, currentVolume)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Speed slider - controls audio speed AND counting speed
        seekBarSpeed?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Calculate speed: 0% = 0.5x, 50% = 1.0x, 100% = 2.0x
                currentSpeed = 0.5f + (progress / 100f) * 1.5f

                // Update audio playback speed
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    try {
                        val params = PlaybackParams()
                        params.speed = currentSpeed
                        mediaPlayer?.playbackParams = params
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                // Update counting interval to match speed
                currentInterval = (baseMantraDuration / currentSpeed).toLong()

                // Restart counting with new speed if currently counting
                if (isCounting && !isPaused) {
                    stopCounting()
                    startAutoCounting(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun startAudio() {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.hare_krishna_mantra)
            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(currentVolume, currentVolume)

            // Set playback speed
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

        // First count at 20 seconds, then every 6 seconds (adjusted by speed)
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

        // Resume with remaining time
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

    private fun incrementCount(
        tvCurrentCount: TextView?,
        tvCurrentRound: TextView?,
        btnStart: Button?,
        tvPhaseStatus: TextView?
    ) {
        currentCount++

        if (currentCount > totalCount) {
            // Round complete
            currentCount = 1
            currentRound++

            if (currentRound > totalRounds) {
                // All 16 rounds complete
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
        // Calculate remaining time
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

    private fun stopEverything() {
        stopCounting()
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

            resumeAudio()
            resumeCounting(tvCurrentCount, tvCurrentRound, btnStart, tvPhaseStatus)
        }
    }
}