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

    suspend fun insertPet(pet: Pet): Long {
        return petDao.insert(pet)
    }
    // Добавление питомца
    suspend fun addPet(userId: Long, name: String, weight: Float, googleDriveLink: String?): Long {
        return petDao.insert(Pet(userId = userId, name = name, weight = weight, google_drive_link = googleDriveLink))
    }

    suspend fun getFeedingTimesByPetId(petId: Long): List<FeedingTime> {
        return feedingDao.getFeedingTimesByPetId(petId)
    }

    // Получение всех питомцев пользователя
    fun getPetsByUser(userId: Long): Flow<List<Pet>> {
        return petDao.getPetsByUser(userId)
    }

    suspend fun getPetById(petId: Long): Pet? {
        return petDao.getPetById(petId)
    }

    // Добавление расписания кормления
    suspend fun addFeedingTime(petId: Long, time: String, portionSize: Int) {
        feedingDao.insert(FeedingTime(pet_id = petId, time = time, portions = portionSize))
    }

    suspend fun insertFeederTime(feedingTime: FeedingTime) {
        return feedingDao.insert(feedingTime)
    }

    // Получение расписания с именами питомцев
    suspend fun insertFeederTimes(feederTimes: List<FeedingTime>) {
        feedingDao.insertAll(feederTimes)
    }

    fun getFeedingTimesWithPets(userId: Long): Flow<List<FeedingWithPet>> {
        return feedingDao.getFeedingTimesWithPets(userId)
    }
}