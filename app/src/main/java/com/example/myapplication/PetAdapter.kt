package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.entities.Pet

class PetAdapter : RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    // Список питомцев
    private val pets = mutableListOf<Pet>()

    // Метод для обновления списка
    fun submitList(newList: List<Pet>) {
        pets.clear()
        pets.addAll(newList)
        notifyDataSetChanged()
    }

    // ViewHolder для одного элемента
    inner class PetViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvPetName)
        val tvWeight: TextView = itemView.findViewById(R.id.tvPetWeight)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pet_item, parent, false)
        return PetViewHolder(view)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val pet = pets[position]
        holder.tvName.text = "Имя: ${pet.name}"
        holder.tvWeight.text = "Вес: ${pet.weight} кг"
    }

    override fun getItemCount(): Int = pets.size
}
