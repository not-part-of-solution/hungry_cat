package com.example.myapplication.ui.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.dao.UserDao
import com.example.myapplication.data.entities.User
import kotlinx.coroutines.launch

class UserViewModel(
    application: Application,
    private val userDao: UserDao
) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    sealed class AuthStatus {
        data class Success(val userId: Long) : AuthStatus()
        data class Error(val message: String) : AuthStatus()
    }

    val registrationStatus = MutableLiveData<AuthStatus>()
    val loginStatus = MutableLiveData<AuthStatus>()

    private val SESSION_DURATION = 60 * 60 * 1000L // 1 час в миллисекундах

    fun registerUser(user: User) {
        viewModelScope.launch {
            try {
                val id = userDao.insert(user)
                saveSession(id)
                registrationStatus.postValue(AuthStatus.Success(id))
            } catch (e: Exception) {
                registrationStatus.postValue(AuthStatus.Error(e.message ?: "Неизвестная ошибка"))
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = userDao.getUserByEmail(email) ?: run {
                    loginStatus.postValue(AuthStatus.Error("Пользователь не найден"))
                    return@launch
                }

                if (user.password != password) {
                    loginStatus.postValue(AuthStatus.Error("Неверный пароль"))
                    return@launch
                }

                saveSession(user.id)
                loginStatus.postValue(AuthStatus.Success(user.id))
            } catch (e: Exception) {
                loginStatus.postValue(AuthStatus.Error("Ошибка входа: ${e.message}"))
            }
        }
    }

    fun getCurrentUserId(): Long = sharedPrefs.getLong("current_user_id", -1L)

    fun isLoggedIn(): Boolean = getCurrentUserId() != -1L && !isSessionExpired()

    fun logout() {
        sharedPrefs.edit()
            .remove("current_user_id")
            .remove("session_start_time")
            .apply()
    }

    private fun saveSession(userId: Long) {
        val now = System.currentTimeMillis()
        sharedPrefs.edit()
            .putLong("current_user_id", userId)
            .putLong("session_start_time", now)
            .apply()
    }

    fun isSessionExpired(): Boolean {
        val start = sharedPrefs.getLong("session_start_time", -1)
        if (start == -1L) return true

        val now = System.currentTimeMillis()
        return now - start > SESSION_DURATION
    }
}
