package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.SecureLensApp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

data class RecordingSessionState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val elapsedSeconds: Long = 0L,
    val mode: String = "VIDEO", // "VIDEO" or "AUDIO"
    val amplitude: Int = 0,
    val lastSavedFileId: String? = null
)

class RecordingService : Service() {

    inner class LocalBinder : Binder() {
        fun getService(): RecordingService = this@RecordingService
    }

    private val binder = LocalBinder()
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null
    private var mediaRecorder: MediaRecorder? = null
    private var currentOutputFile: File? = null
    private var currentMode = "VIDEO"

    companion object {
        const val ACTION_START = "com.example.service.ACTION_START"
        const val ACTION_PAUSE = "com.example.service.ACTION_PAUSE"
        const val ACTION_RESUME = "com.example.service.ACTION_RESUME"
        const val ACTION_STOP = "com.example.service.ACTION_STOP"
        const val EXTRA_MODE = "EXTRA_MODE"

        private val _sessionState = MutableStateFlow(RecordingSessionState())
        val sessionState: StateFlow<RecordingSessionState> = _sessionState.asStateFlow()

        fun startService(context: Context, mode: String) {
            val intent = Intent(context, RecordingService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_MODE, mode)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, RecordingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun pauseService(context: Context) {
            val intent = Intent(context, RecordingService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startService(intent)
        }

        fun resumeService(context: Context) {
            val intent = Intent(context, RecordingService::class.java).apply {
                action = ACTION_RESUME
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val mode = intent.getStringExtra(EXTRA_MODE) ?: "VIDEO"
                startRecordingSession(mode)
            }
            ACTION_PAUSE -> pauseRecording()
            ACTION_RESUME -> resumeRecording()
            ACTION_STOP -> stopRecordingSession()
        }
        return START_NOT_STICKY
    }

    private fun startRecordingSession(mode: String) {
        currentMode = mode
        startForeground(1001, buildNotification(0L, mode, false))

        try {
            val stagingDir = File(filesDir, "private_staging").apply { if (!exists()) mkdirs() }
            val extension = if (mode == "AUDIO") "m4a" else "mp4"
            val file = File(stagingDir, "rec_${System.currentTimeMillis()}.$extension")
            currentOutputFile = file

            if (mode == "AUDIO") {
                @Suppress("DEPRECATION")
                val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(this)
                } else {
                    MediaRecorder()
                }
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                recorder.setOutputFile(file.absolutePath)
                recorder.prepare()
                recorder.start()
                mediaRecorder = recorder
            }
        } catch (e: Exception) {
            // Log/handle fallback
        }

        _sessionState.value = RecordingSessionState(
            isRecording = true,
            isPaused = false,
            elapsedSeconds = 0L,
            mode = mode
        )

        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            var seconds = _sessionState.value.elapsedSeconds
            while (isActive) {
                delay(1000)
                if (!_sessionState.value.isPaused) {
                    seconds++
                    val amp = try {
                        mediaRecorder?.maxAmplitude ?: 0
                    } catch (e: Exception) { 0 }
                    _sessionState.value = _sessionState.value.copy(
                        elapsedSeconds = seconds,
                        amplitude = amp
                    )
                    updateNotification(seconds, currentMode, _sessionState.value.isPaused)
                }
            }
        }
    }

    private fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.pause()
            } catch (e: Exception) {}
        }
        _sessionState.value = _sessionState.value.copy(isPaused = true)
        updateNotification(_sessionState.value.elapsedSeconds, currentMode, true)
    }

    private fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            try {
                mediaRecorder?.resume()
            } catch (e: Exception) {}
        }
        _sessionState.value = _sessionState.value.copy(isPaused = false)
        updateNotification(_sessionState.value.elapsedSeconds, currentMode, false)
    }

    private fun stopRecordingSession() {
        timerJob?.cancel()
        val duration = _sessionState.value.elapsedSeconds
        try {
            mediaRecorder?.stop()
            mediaRecorder?.release()
        } catch (e: Exception) {}
        mediaRecorder = null

        // Encrypt and save to vault in background
        serviceScope.launch(Dispatchers.IO) {
            val file = currentOutputFile
            var savedId: String? = null
            if (currentMode == "AUDIO" && file != null && file.exists() && file.length() > 0) {
                val bytes = file.readBytes()
                val title = "Audio recording"
                val entity = SecureLensApp.instance.vaultRepository.saveEncryptedMedia(
                    title = title,
                    sourceBytes = bytes,
                    mediaKind = "AUDIO",
                    durationMs = duration * 1000L
                )
                file.delete()
                savedId = entity.id
            } else if (file != null && file.exists()) {
                file.delete()
            }

            withContext(Dispatchers.Main) {
                _sessionState.value = RecordingSessionState(
                    isRecording = false,
                    isPaused = false,
                    elapsedSeconds = 0L,
                    mode = currentMode,
                    lastSavedFileId = savedId
                )
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
    }

    private fun buildNotification(seconds: Long, mode: String, isPaused: Boolean): Notification {
        val minutes = seconds / 60
        val secs = seconds % 60
        val timeString = String.format("%02d:%02d", minutes, secs)

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, RecordingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (mode == "AUDIO") "Audio Recording Active" else "Video Recording Active"
        val statusText = if (isPaused) "Paused · $timeString" else "Recording · $timeString"

        return NotificationCompat.Builder(this, SecureLensApp.CHANNEL_RECORDING)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(statusText)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Stop & Save", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun updateNotification(seconds: Long, mode: String, isPaused: Boolean) {
        val notification = buildNotification(seconds, mode, isPaused)
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
        manager?.notify(1001, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        try {
            mediaRecorder?.release()
        } catch (e: Exception) {}
        mediaRecorder = null
    }
}
