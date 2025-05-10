package com.example.myapplication.ui.viewmodels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.entities.User

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val _registrationStatus = MutableLiveData<RegistrationStatus>()

    val registrationStatus: LiveData<RegistrationStatus> = _registrationStatus

    sealed class RegistrationStatus {
        object Success : RegistrationStatus()
        data class Error(val message: String) : RegistrationStatus()
    }

    suspend fun registerUser(user: User) {
        if (userDao.getUserByEmail(user.email)!= null) {
            _registrationStatus.postValue(RegistrationStatus.Error("Email уже используется"))
            return
        }

        try {
            userDao.insert(user)
            _registrationStatus.postValue(RegistrationStatus.Success)
        } catch (e: Exception) {
            _registrationStatus.postValue(RegistrationStatus.Error("Ошибка базы данных"))
        }
    }
}