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

class PetActivity: AppCompatActivity() {
    private lateinit var ibtnBack: ImageButton
    private lateinit var btnDinam: ImageButton
    private lateinit var plusButton: ImageButton
    private lateinit var btnHome: ImageButton
    private lateinit var btnUser: ImageButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_start)

        // Находим кнопку по ID
        val buttonBack = findViewById<ImageButton>(R.id.ibtnBack)

        val buttonDinam = findViewById<ImageButton>(R.id.btnDinam)

        val buttonAddPet = findViewById<ImageButton>(R.id.plusButton)

        val iButtonHome = findViewById<ImageButton>(R.id.btnHome)

        val iButtonUser = findViewById<ImageButton>(R.id.btnUser)
        buttonBack.setOnClickListener {
            finish() // Закрывает PetActivity и возвращает к предыдущей Activity
        }
        buttonDinam.setOnClickListener {
            val intent = Intent(this, PetActivity::class.java)
            startActivity(intent)
        }
        buttonAddPet.setOnClickListener {
            val intent = Intent(this, PetAddActivity::class.java)
            startActivity(intent)
        }
        iButtonHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        iButtonUser.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        }
    }

}