package com.example.myapplication.data.repository

import com.example.myapplication.data.dao.UserDao
import com.example.myapplication.data.dao.PetDao
import com.example.myapplication.data.dao.FeedingDao
import com.example.myapplication.data.dao.FeedingDao.FeedingWithPet
import com.example.myapplication.data.entities.User
import com.example.myapplication.data.entities.Pet
import com.example.myapplication.data.entities.FeedingTime
import kotlinx.coroutines.flow.Flow

class PetFeederRepository(
    private val userDao: UserDao,
    private val petDao: PetDao,
    private val feedingDao: FeedingDao
) {
    // Регистрация пользователя
    suspend fun registerUser(name: String, email: String, password: String): Long {
        return userDao.insert(User(name = name, email = email, password = password))
    }

    // Добавление питомца
    suspend fun addPet(userId: Int, name: String, weight: Float, googleDriveLink: String?): Long {
        return petDao.insert(Pet(userId = userId, name = name, weight = weight, google_drive_link = googleDriveLink))
    }

    // Получение всех питомцев пользователя
    fun getPetsByUser(userId: Int): Flow<List<Pet>> {
        return petDao.getPetsByUser(userId)
    }

    // Добавление расписания кормления
    suspend fun addFeedingTime(petId: Int, foodType: String, time: String, portionSize: Int) {
        feedingDao.insert(FeedingTime(pet_id = petId, time = time, portions = portionSize))
    }

    suspend fun insertFeederTime(feedingTime: FeedingTime) {
        return feedingDao.insert(feedingTime)
    }

    // Получение расписания с именами питомцев
    suspend fun insertFeederTimes(feederTimes: List<FeedingTime>) {
        feedingDao.insertAll(feederTimes)
    }

    fun getFeedingTimesWithPets(userId: Int): Flow<List<FeedingWithPet>> {
        return feedingDao.getFeedingTimesWithPets(userId)
    }
}