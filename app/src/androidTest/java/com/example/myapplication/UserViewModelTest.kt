package com.example.myapplication

import android.app.Application
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.entities.User
import com.example.myapplication.ui.viewmodels.UserViewModel
import junit.framework.TestCase.*
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@RunWith(AndroidJUnit4::class)
class UserViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: AppDatabase
    private lateinit var viewModel: UserViewModel
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        // Очищаем SharedPreferences перед каждым тестом
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            .edit().clear().commit()

        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        viewModel = UserViewModel(
            context.applicationContext as Application,
            database.userDao()
        )
    }

    @After
    fun cleanup() {
        database.close()
    }

    @Test
    fun registerNewUser_shouldSaveSession() = runBlocking {
        val latch = CountDownLatch(1)
        var result: UserViewModel.AuthStatus? = null

        val testUser = User(
            name = "Test User",
            email = "test@test.com",
            password = "password123"
        )

        val observer = Observer<UserViewModel.AuthStatus> {
            result = it
            latch.countDown()
        }

        viewModel.registrationStatus.observeForever(observer)

        viewModel.registerUser(testUser)

        if (!latch.await(2, TimeUnit.SECONDS)) {
            fail("LiveData value was never set.")
        }

        viewModel.registrationStatus.removeObserver(observer)

        val status = result
        assertTrue(status is UserViewModel.AuthStatus.Success)

        val userId = (status as UserViewModel.AuthStatus.Success).userId
        assertTrue(userId > 0)
        assertTrue(viewModel.isLoggedIn())
        assertEquals(userId, viewModel.getCurrentUserId())
    }

    @Test
    fun loginWithValidCredentials_shouldSucceed() = runBlocking {
        val latch = CountDownLatch(1)
        var result: UserViewModel.AuthStatus? = null

        val testUser = User(
            name = "Test User",
            email = "test@test.com",
            password = "password123"
        )
        database.userDao().insert(testUser)

        val observer = Observer<UserViewModel.AuthStatus> {
            result = it
            latch.countDown()
        }

        viewModel.loginStatus.observeForever(observer)

        viewModel.loginUser("test@test.com", "password123")

        if (!latch.await(2, TimeUnit.SECONDS)) {
            fail("LiveData value was never set.")
        }

        viewModel.loginStatus.removeObserver(observer)

        val status = result
        assertTrue(status is UserViewModel.AuthStatus.Success)
        assertTrue(viewModel.isLoggedIn())
    }

    @Test
    fun loginWithInvalidCredentials_shouldFail() = runBlocking {
        val latch = CountDownLatch(1)
        var result: UserViewModel.AuthStatus? = null

        val testUser = User(
            name = "Test User",
            email = "test@test.com",
            password = "correct_password"
        )
        database.userDao().insert(testUser)

        val observer = Observer<UserViewModel.AuthStatus> {
            result = it
            latch.countDown()
        }

        viewModel.loginStatus.observeForever(observer)

        viewModel.loginUser("test@test.com", "wrong_password")

        if (!latch.await(2, TimeUnit.SECONDS)) {
            fail("LiveData value was never set.")
        }

        viewModel.loginStatus.removeObserver(observer)

        val status = result
        assertTrue(status is UserViewModel.AuthStatus.Error)
        assertFalse(viewModel.isLoggedIn())
    }

    @Test
    fun logout_shouldClearSession() = runBlocking {
        val latch = CountDownLatch(1)
        var result: UserViewModel.AuthStatus? = null

        val testUser = User(name = "Logout User", email = "logout@test.com", password = "pass123")

        val observer = Observer<UserViewModel.AuthStatus> {
            result = it
            latch.countDown()
        }

        viewModel.registrationStatus.observeForever(observer)

        viewModel.registerUser(testUser)

        if (!latch.await(2, TimeUnit.SECONDS)) {
            fail("LiveData value was never set.")
        }

        viewModel.registrationStatus.removeObserver(observer)

        assertTrue(result is UserViewModel.AuthStatus.Success)

        viewModel.logout()

        assertFalse(viewModel.isLoggedIn())
        assertEquals(-1L, viewModel.getCurrentUserId())
    }
}
