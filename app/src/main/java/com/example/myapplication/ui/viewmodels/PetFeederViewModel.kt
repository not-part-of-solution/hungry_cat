package com.example.myapplication.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.entities.FeedingTime
import com.example.myapplication.data.dao.FeedingDao.FeedingWithPet
import com.example.myapplication.data.entities.Pet
import com.example.myapplication.data.repository.PetFeederRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PetFeederViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PetFeederRepository
    private val prefs = application.getSharedPreferences("pet_feeder", Application.MODE_PRIVATE)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PetFeederRepository(db.userDao(), db.petDao(), db.feedingDao())
    }

    // User operations
    fun registerUser(name: String, email: String, password: String, callback: (Long) -> Unit) {
        viewModelScope.launch {
            val userId = repository.registerUser(name, email, password)
            prefs.edit().putLong("current_user_id", userId).apply()
            callback(userId)
        }
    }

    // Pet operations
    suspend fun savePet(pet: Pet): Long = repository.addPet(pet.userId, pet.name, pet.weight, pet.google_drive_link)

    fun getPetsByUser(userId: Long): Flow<List<Pet>> = repository.getPetsByUser(userId.toInt())

    // FeederTime operations
    suspend fun insertFeederTime(feedingTime: FeedingTime) {
        return repository.insertFeederTime(feedingTime)
    }

    suspend fun saveFeederTimes(feederTimes: List<FeedingTime>) = repository.insertFeederTimes(feederTimes)

    fun getFeederSchedule(userId: Long): Flow<List<FeedingWithPet>> =
        repository.getFeedingTimesWithPets(userId.toInt())

    // Session management
    fun getCurrentUserId(): Long = prefs.getLong("current_user_id", -1)

    fun logout() = prefs.edit().remove("current_user_id").apply()
}