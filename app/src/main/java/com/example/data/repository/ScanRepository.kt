package com.example.data.repository

import com.example.data.local.dao.DeviceObservationDao
import com.example.data.local.dao.ScanSessionDao
import com.example.data.local.entity.DeviceObservationEntity
import com.example.data.local.entity.ScanSessionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ScanRepository(
    private val sessionDao: ScanSessionDao,
    private val observationDao: DeviceObservationDao
) {
    fun getAllSessions(): Flow<List<ScanSessionEntity>> = sessionDao.getAllSessions()

    fun getAllObservations(): Flow<List<DeviceObservationEntity>> = observationDao.getAllObservations()

    fun getObservationsForSession(sessionId: String): Flow<List<DeviceObservationEntity>> =
        observationDao.getObservationsForSession(sessionId)

    suspend fun getObservationById(id: String): DeviceObservationEntity? =
        observationDao.getObservationById(id)

    suspend fun createSession(
        method: String,
        roomId: String? = null,
        roomName: String? = null
    ): String {
        val id = UUID.randomUUID().toString()
        val session = ScanSessionEntity(
            id = id,
            method = method,
            roomId = roomId,
            roomName = roomName,
            startedAt = System.currentTimeMillis()
        )
        sessionDao.insertSession(session)
        return id
    }

    suspend fun finishSession(
        sessionId: String,
        devicesCount: Int,
        suspiciousCount: Int,
        safeCount: Int
    ) {
        val session = sessionDao.getSessionById(sessionId)
        if (session != null) {
            sessionDao.insertSession(
                session.copy(
                    endedAt = System.currentTimeMillis(),
                    devicesCount = devicesCount,
                    suspiciousCount = suspiciousCount,
                    safeCount = safeCount,
                    status = "COMPLETED"
                )
            )
        }
    }

    suspend fun saveObservations(observations: List<DeviceObservationEntity>) {
        observationDao.insertObservations(observations)
    }

    suspend fun markDeviceSafe(deviceId: String, isSafe: Boolean) {
        val device = observationDao.getObservationById(deviceId)
        if (device != null) {
            observationDao.updateObservation(
                device.copy(
                    isUserMarkedSafe = isSafe,
                    classification = if (isSafe) "SAFE" else "NEEDS_REVIEW"
                )
            )
        }
    }

    suspend fun clearHistory() {
        sessionDao.clearAll()
        observationDao.clearAll()
    }
}
