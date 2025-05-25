package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.viewmodels.PetFeederViewModel
import kotlinx.coroutines.launch

class PetActivity : AppCompatActivity() {

    // UI элементы
    private lateinit var ibtnBack: ImageButton
    private lateinit var btnDinam: ImageButton
    private lateinit var plusButton: ImageButton
    private lateinit var btnHome: ImageButton
    private lateinit var btnUser: ImageButton

    // ViewModel и адаптер
    private lateinit var viewModel: PetFeederViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PetAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_start)

        // ViewModel
        viewModel = ViewModelProvider(this)[PetFeederViewModel::class.java]

        // Инициализация адаптера и списка питомцев
        adapter = PetAdapter() // 👈 адаптер без статистических кнопок

        recyclerView = findViewById(R.id.recyclerPets)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Загружаем питомцев текущего пользователя
        val userId = viewModel.getCurrentUserId()
        if (userId > 0) {
            lifecycleScope.launch {
                viewModel.getPetsByUser(userId).collect { pets ->
                    adapter.submitList(pets)
                }
            }
        } else {
            Toast.makeText(this, "Пользователь не авторизован", Toast.LENGTH_SHORT).show()
        }

        // Настраиваем кнопки
        setupNavigationButtons()
    }

    // Кнопки: назад, динамика, добавить, домой, профиль
    private fun setupNavigationButtons() {
        ibtnBack = findViewById(R.id.ibtnBack)
        btnDinam = findViewById(R.id.btnDinam)
        plusButton = findViewById(R.id.plusButton)
        btnHome = findViewById(R.id.btnHome)
        btnUser = findViewById(R.id.btnUser)

        ibtnBack.setOnClickListener {
            finish()
        }

        btnDinam.setOnClickListener {
            // Открыть общую статистику всех питомцев
            val intent = Intent(this, FeedingScheduleActivity::class.java)
            intent.putExtra("petId", -1L)
            startActivity(intent)
        }

        plusButton.setOnClickListener {
            startActivity(Intent(this, PetAddActivity::class.java))
        }

        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        btnUser.setOnClickListener {
            startActivity(Intent(this, UserActivity::class.java))
        }
    }
}
