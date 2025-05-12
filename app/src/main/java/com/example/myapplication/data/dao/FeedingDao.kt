package com.example.myapplication.data.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.myapplication.data.entities.FeedingTime
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedingDao {
    @Insert
    suspend fun insert(feederTime: FeedingTime)

    @Insert
    suspend fun insertAll(feederTimes: List<FeedingTime>)

    @Query("SELECT * FROM feeding_times WHERE pet_id = :petId")
    fun getFeederTimesForPet(petId: Int): Flow<List<FeedingTime>>

    @Transaction
    @Query("""
        SELECT 
            ft.feeder_time_id AS id,
            ft.pet_id,
            ft.time,
            ft.portions AS portion_size,
            p.name AS pet_name
        FROM feeding_times ft
        JOIN pets p ON ft.pet_id = p.pet_id
        WHERE p.userId = :userId
    """)
    fun getFeedingTimesWithPets(userId: Int): Flow<List<FeedingWithPet>>

    data class FeedingWithPet(
        val id: Int,
        @ColumnInfo(name = "pet_id") val petId: Int,
        val time: String,
        @ColumnInfo(name = "portion_size") val portions: Int,
        @ColumnInfo(name = "pet_name") val petName: String
    )
}