package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.*

class UserActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var fabFeed: FloatingActionButton
    private lateinit var layoutAchievements: LinearLayout
    private lateinit var layoutActivity: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        // Инициализация элементов
        initViews()

        // Настройка Toolbar
        setupToolbar()

        // Загрузка данных
        loadData()

        // Настройка кнопки кормления
        fabFeed.setOnClickListener {
            Toast.makeText(this, "Питомец покормлен!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        fabFeed = findViewById(R.id.fabFeed)
        layoutAchievements = findViewById(R.id.layoutAchievements)
        layoutActivity = findViewById(R.id.layoutActivity)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            finish() // Закрываем активность при нажатии "Назад"
        }
    }

    private fun loadData() {
        // Заполняем достижения
        val achievements = listOf(
            Achievement("Новичок", R.drawable.bone_small),
            Achievement("Гурман", R.drawable.bone_small)
        )

        achievements.forEach { achievement ->
            val view = layoutInflater.inflate(R.layout.item_achievement, null)
            view.findViewById<ImageView>(R.id.ivAchievement).setImageResource(achievement.icon)
            view.findViewById<TextView>(R.id.tvTitle).text = achievement.title
            layoutAchievements.addView(view)
        }

        // Заполняем активность
        val activities = listOf(
            "Покормлен в 08:30",
            "Покормлен в 12:45",
            "Покормлен в 18:20"
        )

        activities.forEach { activity ->
            val textView = TextView(this).apply {
                text = activity
                setCompoundDrawablesWithIntrinsicBounds(
                    ContextCompat.getDrawable(this@UserActivity, R.drawable.pet_food_small),
                    null, null, null
                )
                compoundDrawablePadding = 16
                setPadding(0, 8, 0, 8)
            }
            layoutActivity.addView(textView)
        }
    }

    private data class Achievement(
        val title: String,
        val icon: Int
    )
}