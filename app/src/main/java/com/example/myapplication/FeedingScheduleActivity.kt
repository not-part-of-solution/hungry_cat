package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.ui.viewmodels.PetFeederViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FeedingScheduleActivity : AppCompatActivity() {

    private lateinit var viewModel: PetFeederViewModel
    private lateinit var adapter: FeedingTimeAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var ibtnBack: ImageButton
    private lateinit var btnHome: ImageButton
    private lateinit var btnUser: ImageButton


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feeding_schedule)

        ibtnBack = findViewById(R.id.ibtnBack)
        btnHome = findViewById(R.id.btnHome)
        btnUser = findViewById(R.id.btnUser)
        // Получаем ViewModel
        viewModel = ViewModelProvider(this)[PetFeederViewModel::class.java]

        // Настраиваем RecyclerView и адаптер
        adapter = FeedingTimeAdapter()
        recyclerView = findViewById(R.id.recyclerFeeding)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        ibtnBack.setOnClickListener {
            finish()
        }

        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        btnUser.setOnClickListener {
            startActivity(Intent(this, UserActivity::class.java))
        }

        // Загружаем расписания кормлений всех питомцев текущего пользователя
        lifecycleScope.launch {
            val userId = viewModel.getCurrentUserId()
            if (userId > 0) {
                val pets = viewModel.getPetsByUser(userId).first()
                val allFeedings = pets.flatMap { pet ->
                    viewModel.getFeedingTimesByPetId(pet.pet_id)
                }

                // Отображаем расписание
                adapter.submitList(allFeedings)
            } else {
                // Ошибка: пользователь не авторизован
                Toast.makeText(
                    this@FeedingScheduleActivity,
                    "Ошибка: пользователь не авторизован",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }
}
