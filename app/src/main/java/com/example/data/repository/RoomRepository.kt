package com.example.data.repository

import com.example.data.local.dao.RoomDao
import com.example.data.local.entity.RoomEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class RoomRepository(private val roomDao: RoomDao) {
    fun getAllRooms(): Flow<List<RoomEntity>> = roomDao.getAllRooms()

    suspend fun addCustomRoom(name: String): RoomEntity {
        val room = RoomEntity(
            id = "custom_" + UUID.randomUUID().toString().take(8),
            displayName = name,
            type = "CUSTOM",
            iconKey = "ic_custom_room",
            isCustom = true
        )
        roomDao.insertRoom(room)
        return room
    }

    suspend fun deleteRoom(id: String) {
        roomDao.deleteRoom(id)
    }
}
