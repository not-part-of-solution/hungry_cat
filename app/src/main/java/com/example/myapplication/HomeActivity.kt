package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity


class HomeActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Находим кнопку по ID
        val buttonDevice = findViewById<Button>(R.id.btnDevice)

        val buttonPet = findViewById<Button>(R.id.btnPet)


        val btnHome = findViewById<ImageButton>(R.id.btnHome)

        val btnUser = findViewById<ImageButton>(R.id.btnUser)
        buttonDevice.setOnClickListener {
            val intent = Intent(this, DeviceActivity::class.java)
            startActivity(intent)
        }
        buttonPet.setOnClickListener {
            val intent = Intent(this, PetActivity::class.java)
            startActivity(intent)
        }
        btnHome.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        btnUser.setOnClickListener {
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
        }
    }
}