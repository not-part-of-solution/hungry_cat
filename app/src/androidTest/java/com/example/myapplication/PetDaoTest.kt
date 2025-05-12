package com.example.myapplication.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.entities.*
import com.example.myapplication.data.dao.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.*
import org.hamcrest.Matchers.greaterThan
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PetDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var petDao: PetDao
    private lateinit var feedingDao: FeedingDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()

        userDao = db.userDao()
        petDao = db.petDao()
        feedingDao = db.feedingDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun userInsertAndRetrieve() = runBlocking {
        val id = userDao.insert(User(name = "Alex", email = "alex@test.com", password = "123"))
        assertTrue(id > 0)

        val user = userDao.getUserByEmail("alex@test.com")
        assertEquals("Alex", user?.name)
    }

    @Test
    fun petInsertAndGetByUser() = runBlocking {
        val userId = userDao.insert(User(name = "Owner", email = "owner@test.com", password = "123"))

        val petId = petDao.insert(Pet(userId = userId.toInt(), name = "Buddy", weight = 5f))
        assertTrue(petId > 0)

        val pets = petDao.getPetsByUser(userId.toInt()).first()
        assertEquals(1, pets.size)
        assertEquals("Buddy", pets[0].name)
    }

    @Test
    fun feedingTimeOperations() = runBlocking {
        val userId = userDao.insert(User(name = "O", email = "o@t.com", password = "1"))
        val petId = petDao.insert(Pet(userId = userId.toInt(), name = "P", weight = 1f))

        // Insert
        feedingDao.insert(FeedingTime(pet_id = petId.toInt(), time = "12:00", portions = 2))

        // Verify
        val times = feedingDao.getFeederTimesForPet(petId.toInt()).first()
        assertEquals(1, times.size)
        assertEquals("12:00", times[0].time)
    }

    @Test
    fun deletePetCascades() = runBlocking {
        // 1. Добавляем тестовые данные
        val userId = userDao.insert(User(name = "O", email = "o@t.com", password = "1"))
        val petId = petDao.insert(Pet(userId = userId.toInt(), name = "P", weight = 1f))
        feedingDao.insert(FeedingTime(pet_id = petId.toInt(), time = "12:00", portions = 2))

        // 2. Убедимся, что кормление добавлено
        val initialTimes = feedingDao.getFeederTimesForPet(petId.toInt()).first()
        assertFalse(initialTimes.isEmpty()) // Проверяем, что кормление есть

        // 3. Удаляем питомца (должно каскадно удалить кормление)
        petDao.deletePet(petId.toInt())

        // 4. Проверяем, что кормления больше нет
        val timesAfterDeletion = feedingDao.getFeederTimesForPet(petId.toInt()).first()
        assertTrue(timesAfterDeletion.isEmpty()) // Список должен быть пуст
    }
}