package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.ui.viewmodels.UserViewModel

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🧹 Очищаем предыдущую сессию при попадании на экран логина
        val prefs = getSharedPreferences("session", MODE_PRIVATE)
        prefs.edit().clear().apply()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userDao = AppDatabase.getDatabase(this).userDao()
        viewModel = ViewModelProvider(this,
            object : ViewModelProvider.Factory {
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                    return UserViewModel(application, userDao) as T
                }
            }
        )[UserViewModel::class.java]

        setupViews()
        setupObservers()
        checkAutoLogin()
    }


    private fun setupViews() {
        binding.apply {
            loginButton.setOnClickListener { attemptLogin() }
        }
    }

    private fun setupObservers() {
        viewModel.loginStatus.observe(this) { status ->
            when (status) {
                is UserViewModel.AuthStatus.Success -> {
                    showSuccess("Вход выполнен успешно")
                    navigateToHome()
                }
                is UserViewModel.AuthStatus.Error -> {
                    showError(status.message)
                }
            }
        }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (!validateInput(email, password)) return

        viewModel.loginUser(email, password)
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() || password.isEmpty() -> {
                showError("Все поля должны быть заполнены")
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError("Введите корректный email")
                false
            }
            password.length < 8 -> {
                showError("Пароль должен содержать минимум 8 символов")
                false
            }
            else -> true
        }
    }

    private fun checkAutoLogin() {
        if (viewModel.isLoggedIn()) {
            navigateToHome()
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}