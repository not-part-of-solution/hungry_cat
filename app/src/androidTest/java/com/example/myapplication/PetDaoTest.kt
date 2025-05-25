package com.example.myapplication.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.dao.*
import com.example.myapplication.data.entities.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.Assert.*
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
        ).allowMainThreadQueries().build()

        userDao = db.userDao()
        petDao = db.petDao()
        feedingDao = db.feedingDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun should_insert_and_retrieve_user_by_email() = runBlocking {
        // arrange
        val user = User(name = "Alex", email = "alex@test.com", password = "123")

        // act
        val id = userDao.insert(user)
        val retrieved = userDao.getUserByEmail("alex@test.com")

        // assert
        assertTrue("ID пользователя должен быть положительным", id > 0)
        assertNotNull("Пользователь должен быть найден", retrieved)
        assertEquals(user.name, retrieved?.name)
        assertEquals(user.email, retrieved?.email)
    }

    @Test
    fun should_insert_pet_and_retrieve_by_user_id() = runBlocking {
        // arrange
        val userId = userDao.insert(User(name = "Owner", email = "owner@test.com", password = "123"))
        val testLink = "https://drive.google.com/file/d/12345/view"
        val pet = Pet(userId = userId, name = "Buddy", weight = 5f, google_drive_link = testLink)

        // act
        val petId = petDao.insert(pet)
        val pets = petDao.getPetsByUser(userId).first()

        // assert
        assertTrue("ID питомца должен быть положительным", petId > 0)
        assertEquals("Должен быть один питомец", 1, pets.size)

        val retrieved = pets.first()
        assertEquals("Buddy", retrieved.name)
        assertEquals(testLink, retrieved.google_drive_link)
        assertEquals(5f, retrieved.weight)
        assertEquals(userId, retrieved.userId)
    }

    @Test
    fun should_insert_and_retrieve_feeding_times_for_pet() = runBlocking {
        // arrange
        val userId = userDao.insert(User(name = "O", email = "o@t.com", password = "1"))
        val petId = petDao.insert(Pet(userId = userId, name = "P", weight = 1f))

        // act
        feedingDao.insert(FeedingTime(pet_id = petId, time = "12:00", portions = 2))
        val times = feedingDao.getFeederTimesForPet(petId.toInt()).first()

        // assert
        assertEquals("Должно быть одно время кормления", 1, times.size)
        val time = times.first()
        assertEquals("12:00", time.time)
        assertEquals(2, time.portions)
    }



    @Test
    fun deletePetRemovesAssociatedFeedingTimes() = runBlocking {
        // Given
        val userId = userDao.insert(User(name = "Temp Owner", email = "temp@test.com", password = "123"))
        val petId = petDao.insert(Pet(userId = userId, name = "Temp Pet", weight = 3.0f))
        feedingDao.insert(FeedingTime(pet_id = petId, time = "12:00", portions = 1))

        // Verify initial state
        assertNotNull(petDao.getPetById(petId))
        assertFalse(feedingDao.getFeederTimesForPet(petId.toInt()).first().isEmpty())

        // When
        val deletedRows = petDao.deletePet(petId)

        // Then
        assertEquals(1, deletedRows)
        assertNull(petDao.getPetById(petId))
        assertTrue(feedingDao.getFeederTimesForPet(petId.toInt()).first().isEmpty())
    }
}
