package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.entities.FeedingTime

class FeedingTimeAdapter : RecyclerView.Adapter<FeedingTimeAdapter.ViewHolder>() {

    // Список данных (всё расписание)
    private val items = mutableListOf<FeedingTime>()

    // Метод для обновления данных
    fun submitList(newList: List<FeedingTime>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    // Класс ViewHolder — описывает одну строку в списке
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tvTime)
        val tvPortions: TextView = view.findViewById(R.id.tvPortions)
    }

    // Создание ViewHolder (инфлейтим layout)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.feeding_item, parent, false)
        return ViewHolder(view)
    }

    // Сколько элементов в списке
    override fun getItemCount() = items.size

    // Привязка данных к элементу списка
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTime.text = "Время: ${item.time}"
        holder.tvPortions.text = "Порций: ${item.portions}"
    }
}
