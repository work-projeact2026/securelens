package com.example.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.SecureLensApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SecurityAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        CoroutineScope(Dispatchers.IO).launch {
            SecureLensApp.instance.settingsStore.setIntruderGuardEnabled(true)
        }
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        CoroutineScope(Dispatchers.IO).launch {
            SecureLensApp.instance.settingsStore.setIntruderGuardEnabled(false)
        }
    }

    override fun onPasswordFailed(context: Context, intent: Intent) {
        super.onPasswordFailed(context, intent)
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        val failedCount = try {
            dpm?.getCurrentFailedPasswordAttempts() ?: 1
        } catch (e: Exception) {
            1
        }

        CoroutineScope(Dispatchers.IO).launch {
            val app = SecureLensApp.instance
            val threshold = app.settingsStore.intruderThreshold.first()
            val alertsEnabled = app.settingsStore.securityAlerts.first()

            app.intruderRepository.recordEvent(
                failedCount = failedCount,
                source = "Device Admin callback",
                threshold = threshold
            )

            if (alertsEnabled && failedCount >= threshold) {
                val notifyIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    notifyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, SecureLensApp.CHANNEL_SECURITY)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("Intruder Security Alert")
                    .setContentText("Failed unlock attempt ($failedCount) detected on your device.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .build()

                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                manager?.notify(2001, notification)
            }
        }
    }

    override fun onPasswordSucceeded(context: Context, intent: Intent) {
        super.onPasswordSucceeded(context, intent)
    }
}
