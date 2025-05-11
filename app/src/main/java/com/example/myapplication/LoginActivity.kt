package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.ActivityLoginBinding
import com.example.myapplication.ui.viewmodels.UserViewModel
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: UserViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация ViewModel
        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        setupListeners()
        observeLoginStatus()
        checkAutoLogin()
    }

    private fun setupListeners() {
        binding.loginButton.setOnClickListener {
            attemptLogin()
        }
    }

    private fun attemptLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (!validateInput(email, password)) return

        lifecycleScope.launch {
            viewModel.loginUser(email, password)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() || password.isEmpty() -> {
                showError("Заполните все поля")
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
            else -> true
        }
    }

    private fun observeLoginStatus() {
        viewModel.loginStatus.observe(this) { status ->
            when (status) {
                is UserViewModel.LoginStatus.Success -> {
                    showSuccess("Вход выполнен")
                    navigateToHome()
                }
                is UserViewModel.LoginStatus.Error -> {
                    showError(status.message)
                }
            }
        }
    }

    private fun checkAutoLogin() {
        if (viewModel.getCurrentUserId() != -1) {
            navigateToHome()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
        finish()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}