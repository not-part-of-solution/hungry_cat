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

    fun registerUser(user: User) {
        viewModelScope.launch {
            try {
                val id = userDao.insert(user)
                sharedPrefs.edit().putLong("current_user_id", id).apply()
                registrationStatus.postValue(AuthStatus.Success(id))  // ✅ передаём id
            } catch (e: Exception) {
                registrationStatus.postValue(AuthStatus.Error(e.message ?: "Неизвестная ошибка"))
            }
        }
    }


    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = userDao.getUserByEmail(email) ?: run {
                    loginStatus.postValue(AuthStatus.Error("User not found"))
                    return@launch
                }

                if (user.password != password) {
                    loginStatus.postValue(AuthStatus.Error("Invalid password"))
                    return@launch
                }

                saveSession(user.id)
                loginStatus.postValue(AuthStatus.Success(user.id))
            } catch (e: Exception) {
                loginStatus.postValue(AuthStatus.Error("Login failed: ${e.message}"))
            }
        }
    }

    fun getCurrentUserId(): Long = sharedPrefs.getLong("current_user_id", -1L)

    fun isLoggedIn(): Boolean = getCurrentUserId() != -1L

    fun logout() {
        sharedPrefs.edit().remove("current_user_id").apply()
    }

    private fun saveSession(userId: Long) {
        sharedPrefs.edit().putLong("current_user_id", userId).apply()
    }
}