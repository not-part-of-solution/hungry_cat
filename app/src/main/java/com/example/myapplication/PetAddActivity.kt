package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.entities.Pet
import com.example.myapplication.data.entities.FeedingTime
import com.example.myapplication.ui.viewmodels.PetFeederViewModel
import kotlinx.coroutines.launch
import java.util.*

class PetAddActivity : AppCompatActivity() {

    // Модель данных (вложенный класс) - теперь без pet_id
    data class UIFeedingTime(val time: String, val portions: Int)

    // Адаптер (вложенный класс)
    private inner class FeedingTimesAdapter(
        private val times: MutableList<UIFeedingTime>,
        private val onDeleteClick: (UIFeedingTime) -> Unit
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
            holder.ivBone.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener { onDeleteClick(item) }
        }

        override fun getItemCount() = times.size
    }

    // UI элементы
    private lateinit var etName: EditText
    private lateinit var etValue: EditText
    private lateinit var etLink: EditText
    private lateinit var rvFeedingTimes: RecyclerView
    private lateinit var btnAddFeedingTime: Button
    private lateinit var btnSave: Button
    private lateinit var btnBack: ImageButton

    private val feedingTimes = mutableListOf<UIFeedingTime>()
    private lateinit var adapter: FeedingTimesAdapter
    private lateinit var viewModel: PetFeederViewModel

    private fun showPortionsDialog(time: String) {
        AlertDialog.Builder(this)
            .setTitle("Количество порций для $time")
            .setItems(
                arrayOf(
                    "1",
                    "2",
                    "3",
                    "4",
                    "5",
                    "6",
                    "7",
                    "8",
                    "9",
                    "10",
                    "11",
                    "12"
                )
            ) { _, which ->
                val portions = if (which == 5) 6 else which + 1
                if (feedingTimes.none { it.time == time }) {
                    feedingTimes.add(UIFeedingTime(time, portions))
                    adapter.notifyItemInserted(feedingTimes.size - 1)
                } else {
                    Toast.makeText(this, "Время уже добавлено", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet)
        viewModel = ViewModelProvider(this)[PetFeederViewModel::class.java]
        initViews()
        setupRecyclerView()
        setupListeners()
    }

    private fun initViews() {
        etName = findViewById(R.id.etName)
        etValue = findViewById(R.id.etValue)
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

        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        hourPicker.wrapSelectorWheel = true

        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        minutePicker.wrapSelectorWheel = true

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


    private fun convertToFeederTimes(petId: Long): List<FeedingTime> {
        return feedingTimes.map { uiFeedingTime ->
            FeedingTime(
                pet_id = petId,
                time = uiFeedingTime.time,
                portions = uiFeedingTime.portions  // Совпадающие имена полей
            )
        }
    }


    private fun savePet() {
        val name = etName.text.toString()
        val weight = etValue.text.toString().toFloatOrNull()
        val link = etLink.text.toString().takeIf { it.isNotBlank() }

        when {
            name.isEmpty() -> {
                etName.error = "Введите имя питомца"
                return
            }
            weight == null || weight <= 0 -> {
                etValue.error = "Введите корректный вес"
                return
            }
            feedingTimes.isEmpty() -> {
                Toast.makeText(this, "Добавьте время кормления", Toast.LENGTH_SHORT).show()
                return
            }
            else -> {
                lifecycleScope.launch {
                    try {
                        val userId = viewModel.getCurrentUserId()

                        if (userId <= 0) {
                            Toast.makeText(
                                this@PetAddActivity,
                                "Ошибка: пользователь не авторизован",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@launch
                        }

                        val pet = Pet(
                            userId = userId,
                            name = name,
                            weight = weight,
                            google_drive_link = link
                        )

                        val petId = viewModel.savePet(pet)

                        if (petId > 0) {
                            val feederTimes = convertToFeederTimes(petId)
                            viewModel.saveFeederTimes(feederTimes)

                            runOnUiThread {
                                val dialogView = LayoutInflater.from(this@PetAddActivity).inflate(R.layout.dialog_window, null)

                                val tvnamePet = dialogView.findViewById<TextView>(R.id.tvnamePet)
                                val tvValue = dialogView.findViewById<TextView>(R.id.tvValue)
                                val tvFeeding = dialogView.findViewById<TextView>(R.id.tvtimeForFood)
                                val btnOk = dialogView.findViewById<Button>(R.id.btnDialogOk)

                                tvnamePet.text = "Имя: $name"
                                tvValue.text = "Вес: $weight кг"
                                tvFeeding.text = "Времён кормления: ${feedingTimes.size}"

                                val dialog = AlertDialog.Builder(this@PetAddActivity)
                                    .setView(dialogView)
                                    .setCancelable(false)
                                    .create()

                                btnOk.setOnClickListener {
                                    dialog.dismiss()
                                    finish()
                                }

                                dialog.show()
                            }
                        } else {
                            runOnUiThread {
                                Toast.makeText(
                                    this@PetAddActivity,
                                    "Ошибка сохранения питомца",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(
                                this@PetAddActivity,
                                "Ошибка сохранения: ${e.localizedMessage}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }
}