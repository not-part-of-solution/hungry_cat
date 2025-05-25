package com.example.myapplication.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.entities.Pet
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    // Вставка нового питомца
    @Insert
    suspend fun insert(pet: Pet): Long

    // Получение всех питомцев пользователя
    @Query("SELECT * FROM pets WHERE userId = :userId ORDER BY name ASC")
    fun getPetsByUser(userId: Long): Flow<List<Pet>>

    // Получение питомца по ID
    @Query("SELECT * FROM pets WHERE pet_id = :petId LIMIT 1")
    suspend fun getPetById(petId: Long): Pet?

    // Обновление данных питомца
    @Update
    suspend fun updatePet(pet: Pet): Int

    // Удаление питомца по ID (возвращает количество удаленных строк)
    @Query("DELETE FROM pets WHERE pet_id = :petId")
    suspend fun deletePet(petId: Long): Int
}