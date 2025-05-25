package com.example.myapplication.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "pets",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Pet(
    @PrimaryKey(autoGenerate = true)
    val pet_id: Long = 0,
    val userId: Long,
    val name: String,
    val weight: Float,
    val google_drive_link: String? = null
)