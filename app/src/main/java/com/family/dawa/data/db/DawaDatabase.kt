package com.family.dawa.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        MedicationEntity::class,
        MedicationPhotoEntity::class,
        ScheduleEntity::class,
        DoseEventEntity::class,
        ContactEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DawaDatabase : RoomDatabase() {
    abstract fun medicationDao(): MedicationDao
    abstract fun medicationPhotoDao(): MedicationPhotoDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun doseEventDao(): DoseEventDao
    abstract fun contactDao(): ContactDao

    companion object {
        private const val DB_NAME = "dawa_database.db"

        @Volatile
        private var INSTANCE: DawaDatabase? = null

        fun getInstance(context: Context): DawaDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    DawaDatabase::class.java,
                    DB_NAME
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
