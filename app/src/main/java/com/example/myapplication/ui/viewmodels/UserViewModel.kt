package com.example.myapplication.ui.viewmodels

import android.app.Application
import android.content.Context.MODE_PRIVATE
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.entities.User
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()

    // Статусы регистрации
    sealed class RegistrationStatus {
        object Success : RegistrationStatus()
        data class Error(val message: String) : RegistrationStatus()
    }
    val registrationStatus = MutableLiveData<RegistrationStatus>()

    // Статусы входа
    sealed class LoginStatus {
        object Success : LoginStatus()
        data class Error(val message: String) : LoginStatus()
    }
    val loginStatus = MutableLiveData<LoginStatus>()

    // Регистрация нового пользователя
    fun registerUser(user: User) {
        viewModelScope.launch {
            try {
                // Проверяем, не зарегистрирован ли уже email
                if (userDao.getUserByEmail(user.email) != null) {
                    registrationStatus.postValue(RegistrationStatus.Error("Email уже используется"))
                    return@launch
                }

                // Сохраняем в базу данных
                userDao.insert(user)

                // Автоматически входим после регистрации
                saveCurrentUserId(user.id)
                registrationStatus.postValue(RegistrationStatus.Success)

            } catch (e: Exception) {
                registrationStatus.postValue(RegistrationStatus.Error("Ошибка регистрации: ${e.message}"))
            }
        }
    }

    // Вход существующего пользователя
    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            try {
                val user = userDao.getUserByEmail(email)

                when {
                    user == null -> {
                        loginStatus.postValue(LoginStatus.Error("Пользователь не найден"))
                    }
                    user.password != password -> {
                        loginStatus.postValue(LoginStatus.Error("Неверный пароль"))
                    }
                    else -> {
                        saveCurrentUserId(user.id)
                        loginStatus.postValue(LoginStatus.Success)
                    }
                }
            } catch (e: Exception) {
                loginStatus.postValue(LoginStatus.Error("Ошибка входа: ${e.message}"))
            }
        }
    }

    // Получение текущего пользователя
    fun getCurrentUserId(): Int {
        val prefs = getApplication<Application>().getSharedPreferences("app_prefs", MODE_PRIVATE)
        return prefs.getInt("current_user_id", -1)
    }

    // Выход из системы
    fun logout() {
        val prefs = getApplication<Application>().getSharedPreferences("app_prefs", MODE_PRIVATE)
        prefs.edit().remove("current_user_id").apply()
    }

    private fun saveCurrentUserId(userId: Int) {
        getApplication<Application>()
            .getSharedPreferences("app_prefs", MODE_PRIVATE)
            .edit()
            .putInt("current_user_id", userId)
            .apply()
    }
}