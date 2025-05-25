package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.appcompat.app.AppCompatActivity

class DeviceActivity : AppCompatActivity() {

    // Объявление UI элементов
    private lateinit var etName: EditText
    private lateinit var etRoom: EditText
    private lateinit var rgConnectionType: RadioGroup
    private lateinit var btnBack: ImageButton
    private lateinit var btnHome: ImageButton
    private lateinit var btnUser: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device) // Укажите имя вашего XML-файла

        initViews()
        setupListeners()
    }

    private fun initViews() {
        // Инициализация всех элементов
        etName = findViewById(R.id.etName)
        rgConnectionType = findViewById(R.id.rgConnectionType)
        btnBack = findViewById(R.id.myButton)
        btnHome = findViewById(R.id.btnHome)
        btnUser = findViewById(R.id.btnUser)
    }

    private fun setupListeners() {
        // Кнопка "Назад"
        btnBack.setOnClickListener {
            finish() // Закрываем текущую активность
        }

        // Нижние кнопки навигации
        btnHome.setOnClickListener {
            showToast("Переход на главный экран")
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        btnUser.setOnClickListener {
            showToast("Переход в профиль")
            startActivity(Intent(this, UserActivity::class.java))
            finish() // Оставьте закомментированным, если не нужно закрывать текущую активность
        }

        // Обработка выбора типа подключения
        rgConnectionType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbWiFi -> prepareWiFiConnection()
                R.id.rbBluetooth -> prepareBluetoothConnection()
                R.id.rbManual -> prepareApiConnection()
            }
        }
    }

    private fun prepareWiFiConnection() {
        if (!validateInputs()) return

        val name = etName.text.toString()

        showToast("Подготовка WiFi подключения для $name")
        // Здесь реализация WiFi подключения
    }

    private fun prepareBluetoothConnection() {
        if (!validateInputs()) return

        val name = etName.text.toString()

        showToast("Подготовка Bluetooth подключения для $name")
        // Здесь реализация Bluetooth подключения
    }

    private fun prepareApiConnection() {
        if (!validateInputs()) return

        val name = etName.text.toString()

        showToast("Подготовка API подключения для $name")

        // Пример API вызова в фоновом потоке
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Имитация API запроса
                kotlinx.coroutines.delay(1000)

                withContext(Dispatchers.Main) {
                    showToast("Устройство $name успешно подключено через API!")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Ошибка подключения: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (etName.text.isNullOrEmpty()) {
            etName.error = "Введите название устройства"
            isValid = false
        }

        return isValid
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Дополнительные функции для работы с API
    private suspend fun connectViaApi(name: String, room: String): Boolean {
        // Здесь реализация реального API подключения
        // Возвращает true при успешном подключении
        return try {
            // Имитация API вызова
            kotlinx.coroutines.delay(1500)
            true
        } catch (e: Exception) {
            false
        }
    }
}