package com.example.myapplication.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "pets",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Pet(
    @PrimaryKey(autoGenerate = true) val pet_id: Int = 0,
    @ColumnInfo(name = "user_id") val userId: Int,  // Внешний ключ
    val name: String,
    val weight: Float,  // Вес в кг
    val google_drive_link: String?  // Ссылка на Google Drive (может быть null)
)