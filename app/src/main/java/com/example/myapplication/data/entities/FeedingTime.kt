package com.example.myapplication.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "feeding_times",
    foreignKeys = [
        ForeignKey(
            entity = Pet::class,
            parentColumns = ["pet_id"],
            childColumns = ["pet_id"],
            onDelete = ForeignKey.CASCADE  // Удалить расписание при удалении питомца
        )
    ]
)
data class FeedingTime(
    @PrimaryKey(autoGenerate = true)
    val feeder_time_id: Int = 0,

    val pet_id: Long,  // Ссылка на питомца

    val time: String,  // Время в формате "HH:mm"

    val portions: Int  // Количество порций
)
