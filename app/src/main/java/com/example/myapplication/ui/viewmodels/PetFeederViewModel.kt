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

/**
 * ViewModel для управления данными приложения "Умная кормушка для котов".
 * Содержит логику работы с пользователями, питомцами и расписанием кормления.
 */
class PetFeederViewModel(application: Application) : AndroidViewModel(application) {
    // Репозиторий для работы с базой данных
    private val repository: PetFeederRepository

    // SharedPreferences для хранения данных сессии
    private val prefs = application.getSharedPreferences("pet_feeder", MODE_PRIVATE)

    // Объект для синхронизации доступа к SharedPreferences
    private val prefsLock = Any()

    init {
        // Инициализация базы данных и репозитория
        val db = AppDatabase.getDatabase(application)
        repository = PetFeederRepository(db.userDao(), db.petDao(), db.feedingDao())
    }

    // ===================== ОПЕРАЦИИ С ПОЛЬЗОВАТЕЛЕМ ======================

    /**
     * Регистрация нового пользователя
     * @param name - имя пользователя
     * @param email - email (используется как логин)
     * @param password - пароль
     * @return Result с ID пользователя или ошибкой
     */
    suspend fun registerUser(name: String, email: String, password: String): Result<Long> {
        return try {
            val userId = repository.registerUser(name, email, password)
            // Сохраняем ID текущего пользователя
            synchronized(prefsLock) {
                prefs.edit().putLong("current_user_id", userId).apply()
            }
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ===================== ОПЕРАЦИИ С ПИТОМЦАМИ ======================

    /**
     * Сохранение данных питомца
     * @param pet - объект с данными питомца
     * @return ID сохраненного питомца или -1 при ошибке
     */
    suspend fun savePet(pet: Pet): Long {
        return try {
            repository.addPet(pet.userId, pet.name, pet.weight, pet.google_drive_link)
        } catch (e: Exception) {
            -1L
        }
    }

    /**
     * Получение списка питомцев пользователя
     * @param userId - ID пользователя
     * @return Flow со списком питомцев
     */
    fun getPetsByUser(userId: Long): Flow<List<Pet>> = repository.getPetsByUser(userId)

    // ===================== ОПЕРАЦИИ С РАСПИСАНИЕМ КОРМЛЕНИЯ ======================

    /**
     * Сохранение расписания кормления
     * @param feederTimes - список объектов FeedingTime
     * @return Result с Unit при успехе или ошибкой
     */
    suspend fun saveFeederTimes(feederTimes: List<FeedingTime>): Result<Unit> = try {
        repository.insertFeederTimes(feederTimes)
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Получение расписания кормления с информацией о питомцах
     * @param userId - ID пользователя
     * @return Flow со списком FeedingWithPet
     */
    fun getFeederSchedule(userId: Long): Flow<List<FeedingWithPet>> =
        repository.getFeedingTimesWithPets(userId)

    // ===================== УПРАВЛЕНИЕ СЕССИЕЙ ======================

    /**
     * Получение ID текущего пользователя
     * @return ID пользователя или -1 если пользователь не авторизован
     */
    fun getCurrentUserId(): Long = synchronized(prefsLock) {
        prefs.getLong("current_user_id", -1L)
    }

    /**
     * Выход из системы (очистка данных сессии)
     */
    fun logout() = synchronized(prefsLock) {
        prefs.edit().remove("current_user_id").apply()
    }
}