package com.example.myapplication

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.entities.FeedingTime
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class UserActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var fabFeed: FloatingActionButton
    private lateinit var layoutAchievements: LinearLayout
    private lateinit var layoutActivity: LinearLayout

    private lateinit var db: AppDatabase
    private var petId: Long = 1 // <-- замените на реальный ID из Intent или SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        initViews()
        setupToolbar()

        // Получаем из SharedPreferences
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val petName = prefs.getString("PET_NAME", "Безымянный")
        petId = prefs.getLong("PET_ID", 1L)

        findViewById<TextView>(R.id.tvPetName).text = petName

        db = AppDatabase.getDatabase(this)
        loadData()

        fabFeed.setOnClickListener {
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            val feeding = FeedingTime(
                pet_id = petId,
                time = currentTime,
                portions = 1
            )

            lifecycleScope.launch {
                db.feedingDao().insert(feeding)
            }

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
            finish()
        }
    }

    private fun loadData() {
        // Заполняем достижения (пока статично)
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

        // Загружаем активности кормления из БД
        lifecycleScope.launch {
            db.feedingDao().getFeederTimesForPet(petId.toInt()).collectLatest { feedings ->
                layoutActivity.removeAllViews()

                feedings.forEach { feeding ->
                    val textView = TextView(this@UserActivity).apply {
                        text = "Покормлен в ${feeding.time} (${feeding.portions} порций)"
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
        }
    }

    private data class Achievement(
        val title: String,
        val icon: Int
    )
}
