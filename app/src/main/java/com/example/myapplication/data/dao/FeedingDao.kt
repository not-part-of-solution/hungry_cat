package com.example.myapplication.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.myapplication.data.entities.FeedingTime
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedingDao {
    @Insert
    suspend fun insert(feedingTime: FeedingTime)

    @Transaction
    @Query("""
        SELECT feeding_times.*, pets.name as pet_name 
        FROM feeding_times 
        INNER JOIN pets ON feeding_times.pet_id = pets.pet_id
        WHERE pets.user_id = :userId
    """)
    fun getFeedingTimesWithPetNames(userId: Int): Flow<List<FeedingWithPet>>  // Кастомный DTO
}

// DTO для связи расписания и имени питомца
data class FeedingWithPet(
    val id: Int,
    val pet_id: Int,
    val food_type: String,
    val time: String,
    val portion_size: Int,
    val pet_name: String
)