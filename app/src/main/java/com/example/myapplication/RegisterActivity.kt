package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.entities.User
import com.example.myapplication.databinding.ActivityRegisterBinding
import com.example.myapplication.ui.viewmodels.UserViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
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
    }

    private fun setupViews() {
        binding.apply {
            btnRegister.setOnClickListener { attemptRegistration() }
            tvLogin.setOnClickListener { navigateToLogin() }
        }
    }

    private fun setupObservers() {
        viewModel.registrationStatus.observe(this) { status ->
            when (status) {
                is UserViewModel.AuthStatus.Success -> {
                    showSuccess("Регистрация успешна!")
                    navigateToHome()
                }
                is UserViewModel.AuthStatus.Error -> {
                    showError(status.message)
                }
            }
        }
    }

    private fun attemptRegistration() {
        val name = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (!validateInput(name, email, password, confirmPassword)) return

        val user = User(name = name, email = email, password = password)
        viewModel.registerUser(user)
    }

    private fun validateInput(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            name.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                showError("Все поля должны быть заполнены")
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError("Введите корректный email")
                false
            }
            password.length < 6 -> {
                showError("Пароль должен содержать минимум 6 символов")
                false
            }
            password != confirmPassword -> {
                showError("Пароли не совпадают")
                false
            }
            else -> true
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}