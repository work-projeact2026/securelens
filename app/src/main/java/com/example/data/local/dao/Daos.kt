package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanSessionDao {
    @Query("SELECT * FROM scan_sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<ScanSessionEntity>>

    @Query("SELECT * FROM scan_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): ScanSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ScanSessionEntity)

    @Query("DELETE FROM scan_sessions")
    suspend fun clearAll()
}

@Dao
interface DeviceObservationDao {
    @Query("SELECT * FROM device_observations ORDER BY lastSeenAt DESC")
    fun getAllObservations(): Flow<List<DeviceObservationEntity>>

    @Query("SELECT * FROM device_observations WHERE sessionId = :sessionId ORDER BY lastSeenAt DESC")
    fun getObservationsForSession(sessionId: String): Flow<List<DeviceObservationEntity>>

    @Query("SELECT * FROM device_observations WHERE id = :id LIMIT 1")
    suspend fun getObservationById(id: String): DeviceObservationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservations(observations: List<DeviceObservationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObservation(observation: DeviceObservationEntity)

    @Update
    suspend fun updateObservation(observation: DeviceObservationEntity)

    @Query("DELETE FROM device_observations")
    suspend fun clearAll()
}

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms ORDER BY createdAt ASC")
    fun getAllRooms(): Flow<List<RoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultRooms(rooms: List<RoomEntity>)

    @Query("DELETE FROM rooms WHERE id = :id")
    suspend fun deleteRoom(id: String)
}

@Dao
interface IntruderEventDao {
    @Query("SELECT * FROM intruder_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<IntruderEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: IntruderEventEntity)

    @Query("DELETE FROM intruder_events")
    suspend fun clearAll()
}

@Dao
interface VaultMediaDao {
    @Query("SELECT * FROM vault_media ORDER BY createdAt DESC")
    fun getAllMedia(): Flow<List<VaultMediaEntity>>

    @Query("SELECT * FROM vault_media WHERE mediaKind = :kind ORDER BY createdAt DESC")
    fun getMediaByKind(kind: String): Flow<List<VaultMediaEntity>>

    @Query("SELECT * FROM vault_media WHERE id = :id LIMIT 1")
    suspend fun getMediaById(id: String): VaultMediaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: VaultMediaEntity)

    @Update
    suspend fun updateMedia(media: VaultMediaEntity)

    @Delete
    suspend fun deleteMedia(media: VaultMediaEntity)

    @Query("DELETE FROM vault_media WHERE id = :id")
    suspend fun deleteMediaById(id: String)
}
