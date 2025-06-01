package com.example.myapplication.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import android.content.Context.MODE_PRIVATE
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.entities.FeedingTime
import com.example.myapplication.data.dao.FeedingDao.FeedingWithPet
import com.example.myapplication.data.entities.Pet
import com.example.myapplication.data.repository.PetFeederRepository
import kotlinx.coroutines.flow.Flow

class PetFeederViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PetFeederRepository
    private val prefs = application.getSharedPreferences("app_prefs", MODE_PRIVATE)
    private val prefsLock = Any()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PetFeederRepository(db.userDao(), db.petDao(), db.feedingDao())
    }

    // ===================== USER OPERATIONS ======================
    suspend fun registerUser(name: String, email: String, password: String): Result<Long> {
        return try {
            val userId = repository.registerUser(name, email, password)
            synchronized(prefsLock) {
                prefs.edit().putLong("current_user_id", userId).apply()  //  сохраняем
            }
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // ===================== PET OPERATIONS ======================
    suspend fun savePet(pet: Pet): Long {
        return try {
            repository.insertPet(pet)
        } catch (e: Exception) {
            e.printStackTrace()
            -1L
        }
    }

    suspend fun getFeedingTimesByPetId(petId: Long): List<FeedingTime>{
        return repository.getFeedingTimesByPetId(petId)
    }
    fun getPetsByUser(userId: Long): Flow<List<Pet>> = repository.getPetsByUser(userId)

    suspend fun getPetById(petId: Long): Pet? {
        return try {
            repository.getPetById(petId)
        } catch (e: Exception) {
            null
        }
    }

    // ===================== FEEDING SCHEDULE OPERATIONS ======================
    suspend fun saveFeederTimes(feederTimes: List<FeedingTime>): Result<Unit> = try {
        repository.insertFeederTimes(feederTimes)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getFeederSchedule(userId: Long): Flow<List<FeedingWithPet>> =
        repository.getFeedingTimesWithPets(userId)

    // ===================== SESSION MANAGEMENT ======================
    fun getCurrentUserId(): Long = synchronized(prefsLock) {
        prefs.getLong("current_user_id", -1L)
    }

    fun logout() = synchronized(prefsLock) {
        prefs.edit().remove("current_user_id").apply()
    }
}