package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Entity(tableName = "dispenser_slots")
data class DispenserSlot(
    @PrimaryKey val slotNumber: Int, // 1 to 6
    val medicineName: String,
    val scheduledTime: String, // HH:MM (24-hour format)
    val isActive: Boolean = false,
    val pillCount: Int = 10,
    val dosage: String = "1 pill"
)

@Entity(tableName = "compliance_logs")
data class ComplianceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val slotNumber: Int,
    val medicineName: String,
    val status: String, // "TAKEN", "MISSED", "LATE"
    val actualTime: String // formatted string
)

@Dao
interface DispenserDao {
    @Query("SELECT * FROM dispenser_slots ORDER BY slotNumber ASC")
    fun getAllSlots(): Flow<List<DispenserSlot>>

    @Query("SELECT * FROM dispenser_slots WHERE slotNumber = :slotNumber")
    suspend fun getSlotByNumber(slotNumber: Int): DispenserSlot?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlot(slot: DispenserSlot)

    @Update
    suspend fun updateSlot(slot: DispenserSlot)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSlots(slots: List<DispenserSlot>)

    @Query("SELECT * FROM compliance_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ComplianceLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ComplianceLog)

    @Query("DELETE FROM compliance_logs")
    suspend fun clearLogs()
}

@Database(entities = [DispenserSlot::class, ComplianceLog::class], version = 1, exportSchema = false)
abstract class DispenserDatabase : RoomDatabase() {
    abstract fun dispenserDao(): DispenserDao

    companion object {
        @Volatile
        private var INSTANCE: DispenserDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): DispenserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DispenserDatabase::class.java,
                    "dispenser_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDatabase(database.dispenserDao())
                    }
                }
            }

            suspend fun populateDatabase(dao: DispenserDao) {
                // Pre-populate 6 slots
                val initialSlots = listOf(
                    DispenserSlot(1, "Metformin", "08:00", true, 12, "500mg"),
                    DispenserSlot(2, "Lisinopril", "08:00", true, 8, "10mg"),
                    DispenserSlot(3, "Atorvastatin", "20:00", true, 14, "20mg"),
                    DispenserSlot(4, "Vitamin D3", "12:00", true, 20, "1000 IU"),
                    DispenserSlot(5, "Aspirin", "12:00", false, 0, "81mg"),
                    DispenserSlot(6, "Amoxicillin", "14:00", false, 0, "500mg")
                )
                dao.insertSlots(initialSlots)

                // Pre-populate some compliance logs for the calendar / chart view
                val now = System.currentTimeMillis()
                val oneDayMs = 24 * 60 * 60 * 1000L
                val logs = listOf(
                    ComplianceLog(0, now - 2 * oneDayMs + 8 * 60 * 60 * 1000L, 1, "Metformin", "TAKEN", "08:02 AM"),
                    ComplianceLog(0, now - 2 * oneDayMs + 8 * 60 * 60 * 1000L, 2, "Lisinopril", "TAKEN", "08:03 AM"),
                    ComplianceLog(0, now - 2 * oneDayMs + 12 * 60 * 60 * 1000L, 4, "Vitamin D3", "TAKEN", "12:15 PM"),
                    ComplianceLog(0, now - 2 * oneDayMs + 20 * 60 * 60 * 1000L, 3, "Atorvastatin", "MISSED", "Missed"),
                    
                    ComplianceLog(0, now - oneDayMs + 8 * 60 * 60 * 1000L, 1, "Metformin", "TAKEN", "08:05 AM"),
                    ComplianceLog(0, now - oneDayMs + 8 * 60 * 60 * 1000L, 2, "Lisinopril", "TAKEN", "08:05 AM"),
                    ComplianceLog(0, now - oneDayMs + 12 * 60 * 60 * 1000L, 4, "Vitamin D3", "TAKEN", "12:01 PM"),
                    ComplianceLog(0, now - oneDayMs + 20 * 60 * 60 * 1000L, 3, "Atorvastatin", "TAKEN", "08:12 PM"),

                    ComplianceLog(0, now - 14 * 60 * 60 * 1000L, 1, "Metformin", "TAKEN", "08:10 AM"),
                    ComplianceLog(0, now - 14 * 60 * 60 * 1000L, 2, "Lisinopril", "TAKEN", "08:10 AM")
                )
                for (log in logs) {
                    dao.insertLog(log)
                }
            }
        }
    }
}

class DispenserRepository(private val dispenserDao: DispenserDao) {
    val allSlots: Flow<List<DispenserSlot>> = dispenserDao.getAllSlots()
    val allLogs: Flow<List<ComplianceLog>> = dispenserDao.getAllLogs()

    suspend fun getSlotByNumber(slotNumber: Int): DispenserSlot? {
        return dispenserDao.getSlotByNumber(slotNumber)
    }

    suspend fun insertSlot(slot: DispenserSlot) {
        dispenserDao.insertSlot(slot)
    }

    suspend fun updateSlot(slot: DispenserSlot) {
        dispenserDao.updateSlot(slot)
    }

    suspend fun insertLog(log: ComplianceLog) {
        dispenserDao.insertLog(log)
    }

    suspend fun clearLogs() {
        dispenserDao.clearLogs()
    }
}
