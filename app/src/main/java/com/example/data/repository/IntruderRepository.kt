package com.example.data.repository

import com.example.data.local.dao.IntruderEventDao
import com.example.data.local.entity.IntruderEventEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class IntruderRepository(private val intruderDao: IntruderEventDao) {
    fun getAllEvents(): Flow<List<IntruderEventEntity>> = intruderDao.getAllEvents()

    suspend fun recordEvent(failedCount: Int, source: String = "Device Admin callback", threshold: Int = 3, hasPhoto: Boolean = false, photoPath: String? = null) {
        val event = IntruderEventEntity(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            failedCount = failedCount,
            source = source,
            threshold = threshold,
            hasPhoto = hasPhoto,
            photoPath = photoPath
        )
        intruderDao.insertEvent(event)
    }

    suspend fun clearAll() {
        intruderDao.clearAll()
    }
}
