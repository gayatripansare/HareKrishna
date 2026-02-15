package com.iskcon.temple

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MusicPlayerActivity : AppCompatActivity() {

    private lateinit var imgSongArt: ImageView
    private lateinit var txtSongTitle: TextView
    private lateinit var txtSongArtist: TextView
    private lateinit var txtCurrentTime: TextView
    private lateinit var txtTotalTime: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var btnPlayPause: ImageButton
    private lateinit var btnStop: ImageButton
    private lateinit var btnDownload: Button
    private lateinit var progressDownload: ProgressBar
    private lateinit var txtDownloadStatus: TextView

    private var musicService: MusicPlayerService? = null
    private var isBound = false
    private val handler = Handler(Looper.getMainLooper())
    private var isUserSeeking = false

    private lateinit var song: Song
    private var cachedFile: File? = null

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayerService.MusicBinder
            musicService = binder.getService()
            isBound = true

            // Load and play song
            musicService?.loadSong(song, cachedFile)
            musicService?.play()

            startUpdatingProgress()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
            musicService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_player)

        initViews()
        getSongDetails()
        setupClickListeners()
        checkIfCached()
        bindMusicService()
    }

    private fun initViews() {
        imgSongArt = findViewById(R.id.img_song_art)
        txtSongTitle = findViewById(R.id.txt_song_title)
        txtSongArtist = findViewById(R.id.txt_song_artist)
        txtCurrentTime = findViewById(R.id.txt_current_time)
        txtTotalTime = findViewById(R.id.txt_total_time)
        seekBar = findViewById(R.id.seek_bar)
        btnPlayPause = findViewById(R.id.btn_play_pause)
        btnStop = findViewById(R.id.btn_stop)
        btnDownload = findViewById(R.id.btn_download)
        progressDownload = findViewById(R.id.progress_download)
        txtDownloadStatus = findViewById(R.id.txt_download_status)
    }

    private fun getSongDetails() {
        val songId = intent.getStringExtra("SONG_ID") ?: ""
        val title = intent.getStringExtra("SONG_TITLE") ?: "Unknown"
        val artist = intent.getStringExtra("SONG_ARTIST") ?: "Unknown"
        val url = intent.getStringExtra("SONG_URL") ?: ""
        val image = intent.getStringExtra("SONG_IMAGE") ?: ""
        val duration = intent.getStringExtra("SONG_DURATION") ?: "0:00"

        song = Song(songId, title, artist, url, duration, image, "", "", 0)

        txtSongTitle.text = title
        txtSongArtist.text = artist
        txtTotalTime.text = duration

        if (image.isNotEmpty()) {
            Glide.with(this)
                .load(image)
                .placeholder(R.drawable.ic_music_placeholder)
                .into(imgSongArt)
        }
    }

    private fun setupClickListeners() {
        btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        btnStop.setOnClickListener {
            stopMusic()
        }

        btnDownload.setOnClickListener {
            downloadSong()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    txtCurrentTime.text = formatTime(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                seekBar?.let { musicService?.seekTo(it.progress) }
            }
        })

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener {
            finish()
        }
    }

    private fun checkIfCached() {
        val cacheDir = File(cacheDir, "songs")
        val file = File(cacheDir, "${song.id}.mp3")

        if (file.exists()) {
            cachedFile = file
            btnDownload.visibility = View.GONE
            txtDownloadStatus.visibility = View.VISIBLE
            txtDownloadStatus.text = "✓ Downloaded"
            Log.d("MusicPlayer", "Song is cached: ${file.path}")
        } else {
            btnDownload.visibility = View.VISIBLE
            txtDownloadStatus.visibility = View.GONE
        }
    }

    private fun bindMusicService() {
        val intent = Intent(this, MusicPlayerService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun togglePlayPause() {
        musicService?.let { service ->
            if (service.isPlaying()) {
                service.pause()
                btnPlayPause.setImageResource(R.drawable.ic_play)
            } else {
                service.play()
                btnPlayPause.setImageResource(R.drawable.ic_pause)
            }
        }
    }

    private fun stopMusic() {
        musicService?.stop()
        finish()
    }

    private fun downloadSong() {
        lifecycleScope.launch {
            try {
                btnDownload.visibility = View.GONE
                progressDownload.visibility = View.VISIBLE
                txtDownloadStatus.visibility = View.VISIBLE
                txtDownloadStatus.text = "Downloading..."

                val file = withContext(Dispatchers.IO) {
                    val cacheDir = File(cacheDir, "songs")
                    if (!cacheDir.exists()) cacheDir.mkdirs()

                    val outputFile = File(cacheDir, "${song.id}.mp3")

                    // Download from Cloudinary URL
                    val connection = java.net.URL(song.cloudinaryUrl).openConnection()
                    connection.connect()

                    val input = connection.getInputStream()
                    val output = outputFile.outputStream()

                    input.copyTo(output)

                    input.close()
                    output.close()

                    outputFile
                }

                cachedFile = file
                progressDownload.visibility = View.GONE
                txtDownloadStatus.text = "✓ Downloaded"

                Toast.makeText(this@MusicPlayerActivity, "Song downloaded!", Toast.LENGTH_SHORT).show()
                Log.d("MusicPlayer", "Song cached: ${file.path}")

            } catch (e: Exception) {
                Log.e("MusicPlayer", "Download error: ${e.message}")
                progressDownload.visibility = View.GONE
                btnDownload.visibility = View.VISIBLE
                txtDownloadStatus.visibility = View.GONE
                Toast.makeText(this@MusicPlayerActivity, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startUpdatingProgress() {
        handler.post(object : Runnable {
            override fun run() {
                musicService?.let { service ->
                    if (service.isPlaying() && !isUserSeeking) {
                        val currentPos = service.getCurrentPosition()
                        val duration = service.getDuration()

                        if (duration > 0) {
                            seekBar.max = duration
                            seekBar.progress = currentPos
                            txtCurrentTime.text = formatTime(currentPos)

                            if (txtTotalTime.text == "0:00") {
                                txtTotalTime.text = formatTime(duration)
                            }
                        }
                    }

                    // Update play/pause button
                    if (service.isPlaying()) {
                        btnPlayPause.setImageResource(R.drawable.ic_pause)
                    } else {
                        btnPlayPause.setImageResource(R.drawable.ic_play)
                    }
                }

                handler.postDelayed(this, 1000)
            }
        })
    }

    private fun formatTime(millis: Int): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / 1000) / 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
    }
}