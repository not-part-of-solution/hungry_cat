package com.example.myapplication.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.entities.User
import com.example.myapplication.data.dao.UserDao
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.greaterThan
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao

    @Before
    fun setup() {
        // Создаем in-memory базу данных для тестов
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries() // Разрешаем выполнение на главном потоке для тестов
            .build()

        userDao = database.userDao()
    }


    @Test
    fun insertAndRetrieveUser() = runBlocking {
        // Создаем тестового пользователя
        val testUser = User(
            id = 1,
            name = "Test User",
            email = "test@example.com",
            password = "password123"
        )

        // Вставляем пользователя в базу
        val insertId = userDao.insert(testUser)

        assertThat("ID пользователя должен быть больше 0", insertId, greaterThan(0L))
        // Получаем пользователя по email
        val retrievedUser = userDao.getUserByEmail("test@example.com")

        // Проверяем, что данные совпадают
        assertThat(retrievedUser?.name, equalTo("Test User"))
        assertThat(retrievedUser?.email, equalTo("test@example.com"))
    }

    @Test
    fun getUserByEmail_shouldReturnNullForNonExistentUser() = runBlocking {
        val retrievedUser = userDao.getUserByEmail("nonexistent@example.com")
        assertThat(retrievedUser, equalTo(null))
    }

    @After
    fun cleanup() {
        // Закрываем базу после тестов
        database.close()
    }
}