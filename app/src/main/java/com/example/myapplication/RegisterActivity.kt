package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.entities.User
import com.example.myapplication.ui.viewmodels.UserViewModel
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLogin: TextView

    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Инициализация ViewModel
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        initViews()
        setupListeners()
        observeRegistrationStatus()
    }

    private fun initViews() {
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLogin = findViewById(R.id.tvLogin)
    }

    private fun setupListeners() {
        btnRegister.setOnClickListener {
            registerUser()
        }

        tvLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun registerUser() {
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()

        if (!validateInput(username, email, password, confirmPassword)) {
            return
        }

        lifecycleScope.launch {
            viewModel.registerUser(
                User(
                    name = username,
                    email = email,
                    password = password
                )
            )
        }
    }

    private fun validateInput(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        return when {
            username.isEmpty() || email.isEmpty() || password.isEmpty() -> {
                showError("Все поля должны быть заполнены")
                false
            }
            !isValidEmail(email) -> {
                showError("Некорректный email")
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

    private fun observeRegistrationStatus() {
        viewModel.registrationStatus.observe(this) { status ->
            when (status) {
                is UserViewModel.RegistrationStatus.Success -> {
                    showSuccess("Регистрация успешна!")
                    navigateToHome()
                }
                is UserViewModel.RegistrationStatus.Error -> {
                    showError(status.message)
                }
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        return email.matches(emailRegex.toRegex())
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