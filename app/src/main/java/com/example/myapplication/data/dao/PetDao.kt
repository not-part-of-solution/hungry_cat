package com.example.myapplication.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.data.entities.Pet
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Insert
    suspend fun insert(pet: Pet): Long

    @Query("SELECT * FROM pets WHERE userId = :userId")
    fun getPetsByUser(userId: Long): Flow<List<Pet>>  // Flow для LiveData

    @Query("DELETE FROM pets WHERE pet_id = :petId")
    suspend fun deletePet(petId: Long)  // Ничего не возвращает (void)
}