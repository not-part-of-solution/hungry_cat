package com.example.myapplication.data.entities

import androidx.room.ColumnInfo
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
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "pet_id") val petId: Int,  // Внешний ключ
    val food_type: String,  // Тип корма ("Сухой", "Влажный")
    val time: String,  // Время в формате "HH:mm"
    val portion_size: Int  // Размер порции в граммах
)