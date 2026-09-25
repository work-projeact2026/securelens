package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ScanSessionEntity::class,
        DeviceObservationEntity::class,
        RoomEntity::class,
        IntruderEventEntity::class,
        VaultMediaEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SecureLensDatabase : RoomDatabase() {
    abstract fun scanSessionDao(): ScanSessionDao
    abstract fun deviceObservationDao(): DeviceObservationDao
    abstract fun roomDao(): RoomDao
    abstract fun intruderEventDao(): IntruderEventDao
    abstract fun vaultMediaDao(): VaultMediaDao

    companion object {
        @Volatile
        private var INSTANCE: SecureLensDatabase? = null

        fun getInstance(context: Context): SecureLensDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SecureLensDatabase::class.java,
                    "securelens.db"
                ).addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default rooms matching reference UI D06
                        CoroutineScope(Dispatchers.IO).launch {
                            val defaultRooms = listOf(
                                RoomEntity("room_living", "Living Room", "LIVING_ROOM", "ic_living_room"),
                                RoomEntity("room_bedroom", "Bedroom", "BEDROOM", "ic_bedroom"),
                                RoomEntity("room_bathroom", "Bathroom", "BATHROOM", "ic_bathroom"),
                                RoomEntity("room_hotel", "Hotel Room", "HOTEL_ROOM", "ic_hotel_room"),
                                RoomEntity("room_office", "Office", "OFFICE", "ic_office"),
                                RoomEntity("room_other", "Other Area", "OTHER", "ic_other_room")
                            )
                            getInstance(context).roomDao().insertDefaultRooms(defaultRooms)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
