package com.example.myapplication.ui.viewmodels

import android.app.Application
import android.content.Context.MODE_PRIVATE
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.dao.FeedingWithPet
import com.example.myapplication.data.repository.PetFeederRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class PetFeederViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PetFeederRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PetFeederRepository(db.userDao(), db.petDao(), db.feedingDao())
    }

    fun registerUser(name: String, email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = try {
                repository.registerUser(name, email, password) > 0
            } catch (e: Exception) {
                false
            }
            onResult(result)
        }
    }

    fun addPet(
        userId: Int,
        name: String,
        weight: Float,
        googleDriveLink: String?,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val result = try {
                repository.addPet(userId, name, weight, googleDriveLink) > 0
            } catch (e: Exception) {
                false
            }
            onResult(result)
        }
    }

    fun getFeedingSchedule(userId: Int): Flow<List<FeedingWithPet>> {
        return repository.getFeedingTimesWithPets(userId)
    }
    fun getCurrentUserId(): Int {
        val prefs = getApplication<Application>().getSharedPreferences("app_prefs", MODE_PRIVATE)
        return prefs.getInt("current_user_id", -1)
    }
}