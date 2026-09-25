package com.example.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "securelens_settings")

class SettingsStore(private val context: Context) {

    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val KEY_VAULT_PIN = stringPreferencesKey("vault_pin")
        val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        val KEY_INTRUDER_GUARD_ENABLED = booleanPreferencesKey("intruder_guard_enabled")
        val KEY_INTRUDER_THRESHOLD = intPreferencesKey("intruder_threshold")
        val KEY_AUTO_LOCK = stringPreferencesKey("auto_lock")
        val KEY_VIDEO_QUALITY = stringPreferencesKey("video_quality")
        val KEY_VIDEO_FPS = intPreferencesKey("video_fps")
        val KEY_VIDEO_CAMERA = stringPreferencesKey("video_camera")
        val KEY_RECORD_AUDIO = booleanPreferencesKey("record_audio")
        val KEY_SAVE_TO_VAULT = booleanPreferencesKey("save_to_vault")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_SECURITY_ALERTS = booleanPreferencesKey("security_alerts")
        val KEY_SCAN_NOTIFICATIONS = booleanPreferencesKey("scan_notifications")
        val KEY_VAULT_REMINDERS = booleanPreferencesKey("vault_reminders")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_ONBOARDING_COMPLETED] ?: false
    }

    val selectedLanguage: Flow<String> = context.dataStore.data.map {
        it[KEY_SELECTED_LANGUAGE] ?: "en"
    }

    val isDarkMode: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_DARK_MODE] ?: false
    }

    val vaultPin: Flow<String?> = context.dataStore.data.map {
        it[KEY_VAULT_PIN]
    }

    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_BIOMETRIC_ENABLED] ?: true
    }

    val isIntruderGuardEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_INTRUDER_GUARD_ENABLED] ?: false
    }

    val intruderThreshold: Flow<Int> = context.dataStore.data.map {
        it[KEY_INTRUDER_THRESHOLD] ?: 3
    }

    val autoLockTimeout: Flow<String> = context.dataStore.data.map {
        it[KEY_AUTO_LOCK] ?: "Immediately"
    }

    val videoQuality: Flow<String> = context.dataStore.data.map {
        it[KEY_VIDEO_QUALITY] ?: "1080p"
    }

    val videoFps: Flow<Int> = context.dataStore.data.map {
        it[KEY_VIDEO_FPS] ?: 30
    }

    val videoCamera: Flow<String> = context.dataStore.data.map {
        it[KEY_VIDEO_CAMERA] ?: "Rear"
    }

    val recordAudioWithVideo: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_RECORD_AUDIO] ?: true
    }

    val saveToEncryptedVault: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_SAVE_TO_VAULT] ?: true
    }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_NOTIFICATIONS_ENABLED] ?: true
    }

    val securityAlerts: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_SECURITY_ALERTS] ?: true
    }

    val scanNotifications: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_SCAN_NOTIFICATIONS] ?: false
    }

    val vaultReminders: Flow<Boolean> = context.dataStore.data.map {
        it[KEY_VAULT_REMINDERS] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setSelectedLanguage(lang: String) {
        context.dataStore.edit { it[KEY_SELECTED_LANGUAGE] = lang }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    suspend fun setVaultPin(pin: String) {
        context.dataStore.edit { it[KEY_VAULT_PIN] = pin }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setIntruderGuardEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_INTRUDER_GUARD_ENABLED] = enabled }
    }

    suspend fun setIntruderThreshold(threshold: Int) {
        context.dataStore.edit { it[KEY_INTRUDER_THRESHOLD] = threshold }
    }

    suspend fun setAutoLockTimeout(timeout: String) {
        context.dataStore.edit { it[KEY_AUTO_LOCK] = timeout }
    }

    suspend fun setVideoQuality(quality: String) {
        context.dataStore.edit { it[KEY_VIDEO_QUALITY] = quality }
    }

    suspend fun setVideoFps(fps: Int) {
        context.dataStore.edit { it[KEY_VIDEO_FPS] = fps }
    }

    suspend fun setVideoCamera(cam: String) {
        context.dataStore.edit { it[KEY_VIDEO_CAMERA] = cam }
    }

    suspend fun setRecordAudioWithVideo(enabled: Boolean) {
        context.dataStore.edit { it[KEY_RECORD_AUDIO] = enabled }
    }

    suspend fun setSaveToEncryptedVault(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SAVE_TO_VAULT] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setSecurityAlerts(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SECURITY_ALERTS] = enabled }
    }

    suspend fun setScanNotifications(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SCAN_NOTIFICATIONS] = enabled }
    }

    suspend fun setVaultReminders(enabled: Boolean) {
        context.dataStore.edit { it[KEY_VAULT_REMINDERS] = enabled }
    }
}
