package com.iskcon.temple

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File

class MusicPlayerService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val binder = MusicBinder()
    private var currentSong: Song? = null
    private var isPrepared = false

    companion object {
        const val CHANNEL_ID = "VaishnavaSongsChannel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d("MusicPlayerService", "Service created")
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> play()
            ACTION_PAUSE -> pause()
            ACTION_STOP -> stop()
        }
        return START_STICKY
    }

    fun loadSong(song: Song, cachedFile: File? = null) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer()
            currentSong = song
            isPrepared = false

            // ✅ IMPORTANT: Set wake lock to prevent cutting off
            mediaPlayer?.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)

            if (cachedFile != null && cachedFile.exists()) {
                // Play from cached file
                Log.d("MusicPlayerService", "Playing from cache: ${cachedFile.path}")
                mediaPlayer?.setDataSource(cachedFile.path)
            } else {
                // Stream from Cloudinary
                Log.d("MusicPlayerService", "Streaming from: ${song.cloudinaryUrl}")
                mediaPlayer?.setDataSource(song.cloudinaryUrl)

                // ✅ Enable better buffering for streaming
                mediaPlayer?.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC)
            }

            mediaPlayer?.setOnPreparedListener {
                isPrepared = true
                val duration = mediaPlayer?.duration ?: 0
                Log.d("MusicPlayerService", "MediaPlayer prepared - Duration: ${duration}ms")
            }

            mediaPlayer?.setOnErrorListener { mp, what, extra ->
                Log.e("MusicPlayerService", "Error: what=$what, extra=$extra")
                when (what) {
                    MediaPlayer.MEDIA_ERROR_SERVER_DIED -> {
                        Log.e("MusicPlayerService", "Media server died")
                    }
                    MediaPlayer.MEDIA_ERROR_UNKNOWN -> {
                        Log.e("MusicPlayerService", "Unknown media error")
                    }
                }
                false
            }

            mediaPlayer?.setOnCompletionListener {
                Log.d("MusicPlayerService", "Song completed normally")
                // Don't stop service, just pause
                pause()
            }

            // ✅ Add buffering update listener
            mediaPlayer?.setOnBufferingUpdateListener { mp, percent ->
                Log.d("MusicPlayerService", "Buffering: $percent%")
            }

            mediaPlayer?.prepareAsync()

        } catch (e: Exception) {
            Log.e("MusicPlayerService", "Error loading song: ${e.message}")
            e.printStackTrace()
        }
    }

    fun play() {
        if (isPrepared && mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            showNotification()
            Log.d("MusicPlayerService", "Playing")
        }
    }

    fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            Log.d("MusicPlayerService", "Paused")
        }
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPrepared = false
        stopForeground(true)
        stopSelf()
        Log.d("MusicPlayerService", "Stopped")
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun getDuration(): Int = if (isPrepared) mediaPlayer?.duration ?: 0 else 0

    fun seekTo(position: Int) {
        if (isPrepared) {
            mediaPlayer?.seekTo(position)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Vaishnava Songs Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Music player controls"
                setShowBadge(false)
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun showNotification() {
        val song = currentSong ?: return

        val playIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = if (isPlaying()) ACTION_PAUSE else ACTION_PLAY
        }
        val playPendingIntent = PendingIntent.getService(
            this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, MusicPlayerService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(song.title)
            .setContentText(song.artist)
            .setSmallIcon(R.drawable.ic_music_note)
            .addAction(
                if (isPlaying()) R.drawable.ic_pause else R.drawable.ic_play,
                if (isPlaying()) "Pause" else "Play",
                playPendingIntent
            )
            .addAction(R.drawable.ic_stop, "Stop", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        Log.d("MusicPlayerService", "Service destroyed")
    }
}