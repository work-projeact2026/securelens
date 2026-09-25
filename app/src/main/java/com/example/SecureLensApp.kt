package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.core.datastore.SettingsStore
import com.example.core.security.CryptoManager
import com.example.data.local.SecureLensDatabase
import com.example.data.repository.*

class SecureLensApp : Application() {

    companion object {
        const val CHANNEL_RECORDING = "securelens_recording_channel"
        const val CHANNEL_SECURITY = "securelens_security_channel"
        lateinit var instance: SecureLensApp
            private set
    }

    val database by lazy { SecureLensDatabase.getInstance(this) }
    val settingsStore by lazy { SettingsStore(this) }
    val cryptoManager by lazy { CryptoManager() }

    val scanRepository by lazy {
        ScanRepository(database.scanSessionDao(), database.deviceObservationDao())
    }
    val roomRepository by lazy {
        RoomRepository(database.roomDao())
    }
    val intruderRepository by lazy {
        IntruderRepository(database.intruderEventDao())
    }
    val vaultRepository by lazy {
        VaultRepository(database.vaultMediaDao(), cryptoManager, this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val recordingChannel = NotificationChannel(
                CHANNEL_RECORDING,
                "Secure Recording Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows ongoing video and audio recording status and controls"
            }

            val securityChannel = NotificationChannel(
                CHANNEL_SECURITY,
                "Security & Intruder Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for failed unlock attempts and security events"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(recordingChannel)
            manager?.createNotificationChannel(securityChannel)
        }
    }
}
