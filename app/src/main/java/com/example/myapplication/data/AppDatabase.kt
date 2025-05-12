package com.example.myapplication.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myapplication.data.dao.*
import com.example.myapplication.data.entities.FeedingTime
import com.example.myapplication.data.entities.Pet
import com.example.myapplication.data.entities.User

@Database(
    entities = [User::class, Pet::class, FeedingTime::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun petDao(): PetDao
    abstract fun feedingDao(): FeedingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pet_feeder_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}