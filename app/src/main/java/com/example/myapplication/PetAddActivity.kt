package com.example.myapplication

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class PetAddActivity : AppCompatActivity() {

    // Модель данных (вложенный класс)
    data class FeedingTime(val time: String, val portions: Int)

    // Адаптер (вложенный класс)
    private inner class FeedingTimesAdapter(
        private val times: MutableList<FeedingTime>,
        private val onDeleteClick: (FeedingTime) -> Unit
    ) : RecyclerView.Adapter<FeedingTimesAdapter.ViewHolder>() {

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvTime: TextView = view.findViewById(R.id.tvTime)
            val tvPortions: TextView = view.findViewById(R.id.tvPortions)
            val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
            val ivBone: ImageView = view.findViewById(R.id.ivBone)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_feeding_time, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = times[position]
            holder.tvTime.text = item.time
            holder.tvPortions.text = "${item.portions} порций"
            holder.ivBone.visibility = if (item.portions > 5) View.VISIBLE else View.GONE
            holder.btnDelete.setOnClickListener { onDeleteClick(item) }
        }

        override fun getItemCount() = times.size
    }

    // UI элементы
    private lateinit var etName: EditText
    private lateinit var etValue: EditText
    private lateinit var etRoom: EditText
    private lateinit var etLink: EditText
    private lateinit var rvFeedingTimes: RecyclerView
    private lateinit var btnAddFeedingTime: Button
    private lateinit var btnSave: Button
    private lateinit var btnBack: ImageButton

    private val feedingTimes = mutableListOf<FeedingTime>()
    private lateinit var adapter: FeedingTimesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet) // Убедитесь, что имя файла правильное!

        initViews()
        setupRecyclerView()
        setupListeners()
    }

    private fun initViews() {
        etName = findViewById(R.id.etName)
        etValue = findViewById(R.id.etValue)
        etRoom = findViewById(R.id.etRoom)
        etLink = findViewById(R.id.etLink)
        rvFeedingTimes = findViewById(R.id.rvFeedingTimes)
        btnAddFeedingTime = findViewById(R.id.btnAddFeedingTime)
        btnSave = findViewById(R.id.crtSave)
        btnBack = findViewById(R.id.ibtnBack)
    }

    private fun setupRecyclerView() {
        adapter = FeedingTimesAdapter(feedingTimes) { timeToDelete ->
            feedingTimes.remove(timeToDelete)
            adapter.notifyDataSetChanged()
        }
        rvFeedingTimes.layoutManager = LinearLayoutManager(this)
        rvFeedingTimes.adapter = adapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnAddFeedingTime.setOnClickListener { showTimeAndPortionsPicker() }

        btnSave.setOnClickListener { savePet() }
    }

    private fun showTimeAndPortionsPicker() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_time_picker, null)
        val hourPicker = dialogView.findViewById<NumberPicker>(R.id.hourPicker)
        val minutePicker = dialogView.findViewById<NumberPicker>(R.id.minutePicker)

        // Настройка часов (0-23)
        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        hourPicker.wrapSelectorWheel = true // Включаем циклическую прокрутку

        // Настройка минут (0-59)
        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        minutePicker.wrapSelectorWheel = true // Включаем циклическую прокрутку

        // Установка текущего времени
        val calendar = Calendar.getInstance()
        hourPicker.value = calendar.get(Calendar.HOUR_OF_DAY)
        minutePicker.value = calendar.get(Calendar.MINUTE)

        AlertDialog.Builder(this)
            .setTitle("Выберите время кормления")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val hour = hourPicker.value
                val minute = minutePicker.value
                val time = String.format("%02d:%02d", hour, minute)
                showPortionsDialog(time)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showPortionsDialog(time: String) {
        AlertDialog.Builder(this)
            .setTitle("Количество грамм для $time")
            .setItems(arrayOf("10", "20", "30", "40", "50", "60", "70", "80", "90", "100")) { _, which ->
                val portions = if (which == 5) 6 else which + 1
                if (feedingTimes.none { it.time == time }) {
                    feedingTimes.add(FeedingTime(time, portions))
                    adapter.notifyItemInserted(feedingTimes.size - 1)
                } else {
                    Toast.makeText(this, "Время уже добавлено", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun savePet() {
        when {
            etName.text.isEmpty() -> etName.error = "Введите имя"
            etValue.text.isEmpty() -> etValue.error = "Введите вес"
            etRoom.text.isEmpty() -> etRoom.error = "Введите комнату"
            feedingTimes.isEmpty() -> Toast.makeText(this, "Добавьте время кормления", Toast.LENGTH_SHORT).show()
            else -> {
                Toast.makeText(this, "Питомец ${etName.text} сохранён!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}