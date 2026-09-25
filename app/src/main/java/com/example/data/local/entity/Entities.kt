package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_sessions")
data class ScanSessionEntity(
    @PrimaryKey val id: String,
    val method: String, // WIFI, BLUETOOTH, FULL, MAGNETIC, OPTICAL, THERMAL
    val roomId: String? = null,
    val roomName: String? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val status: String = "COMPLETED",
    val devicesCount: Int = 0,
    val suspiciousCount: Int = 0,
    val safeCount: Int = 0
)

@Entity(tableName = "device_observations")
data class DeviceObservationEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val source: String, // WIFI, BLUETOOTH
    val displayName: String,
    val observedAddress: String? = null, // IP or MAC or BSSID
    val deviceType: String = "Unknown",
    val signalDbm: Int? = null,
    val firstSeenAt: Long = System.currentTimeMillis(),
    val lastSeenAt: Long = System.currentTimeMillis(),
    val classification: String = "NEEDS_REVIEW", // NEEDS_REVIEW (Suspicious), SAFE, UNKNOWN
    val evidenceCodes: String = "",
    val vendor: String = "Unverified",
    val isUserMarkedSafe: Boolean = false
)

@Entity(tableName = "rooms")
data class RoomEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val type: String, // LIVING_ROOM, BEDROOM, BATHROOM, HOTEL_ROOM, OFFICE, OTHER
    val iconKey: String = "ic_room_default",
    val createdAt: Long = System.currentTimeMillis(),
    val isCustom: Boolean = false
)

@Entity(tableName = "intruder_events")
data class IntruderEventEntity(
    @PrimaryKey val id: String,
    val timestamp: Long = System.currentTimeMillis(),
    val failedCount: Int = 1,
    val source: String = "Device Admin callback",
    val threshold: Int = 3,
    val note: String = "Sample credential attempt",
    val hasPhoto: Boolean = false,
    val photoPath: String? = null
)

@Entity(tableName = "vault_media")
data class VaultMediaEntity(
    @PrimaryKey val id: String,
    val title: String,
    val filePath: String,
    val mediaKind: String, // PHOTO, VIDEO, AUDIO, INTRUDER_EVENT
    val durationMs: Long = 0L,
    val fileSize: Long = 0L,
    val createdAt: Long = System.currentTimeMillis(),
    val isEncrypted: Boolean = true
)
