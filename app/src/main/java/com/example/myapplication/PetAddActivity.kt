package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.data.entities.FeedingTime
import com.example.myapplication.data.entities.Pet
import com.example.myapplication.ui.viewmodels.PetFeederViewModel
import kotlinx.coroutines.launch
import java.util.*

class PetAddActivity : AppCompatActivity() {

    data class UIFeedingTime(val time: String, val portions: Int)

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
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feeding_time, parent, false)
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

    private lateinit var etName: EditText
    private lateinit var etValue: EditText
    private lateinit var etLink: EditText
    private lateinit var rvFeedingTimes: RecyclerView
    private lateinit var btnAddFeedingTime: Button
    private lateinit var btnSave: Button
    private lateinit var btnBack: ImageButton
    private lateinit var btnHome: ImageButton
    private lateinit var btnUser: ImageButton

    private val feedingTimes = mutableListOf<UIFeedingTime>()
    private lateinit var adapter: FeedingTimesAdapter
    private lateinit var viewModel: PetFeederViewModel

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
        btnHome = findViewById(R.id.btnHome)
        btnUser = findViewById(R.id.btnUser)
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
        btnHome.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        btnUser.setOnClickListener {
            startActivity(Intent(this, UserActivity::class.java))
        }
    }

    private fun showTimeAndPortionsPicker() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_time_picker, null)
        val hourPicker = dialogView.findViewById<NumberPicker>(R.id.hourPicker)
        val minutePicker = dialogView.findViewById<NumberPicker>(R.id.minutePicker)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)

        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        hourPicker.wrapSelectorWheel = true
        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        minutePicker.wrapSelectorWheel = true

        val calendar = Calendar.getInstance()
        hourPicker.value = calendar.get(Calendar.HOUR_OF_DAY)
        minutePicker.value = calendar.get(Calendar.MINUTE)

        val dialog = AlertDialog.Builder(this, R.style.RoundedAlertDialog)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnOk.setOnClickListener {
            val hour = hourPicker.value
            val minute = minutePicker.value
            val time = String.format("%02d:%02d", hour, minute)
            showPortionsDialog(time)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showPortionsDialog(time: String) {
        AlertDialog.Builder(this)
            .setTitle("Количество порций для $time")
            .setItems((1..12).map { it.toString() }.toTypedArray()) { _, which ->
                val portions = which + 1
                if (feedingTimes.none { it.time == time }) {
                    feedingTimes.add(UIFeedingTime(time, portions))
                    adapter.notifyItemInserted(feedingTimes.size - 1)
                } else {
                    Toast.makeText(this, "Время уже добавлено", Toast.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun convertToFeederTimes(petId: Long): List<FeedingTime> {
        return feedingTimes.map {
            FeedingTime(pet_id = petId, time = it.time, portions = it.portions)
        }
    }

    private fun savePet() {
        val name = etName.text.toString()
        val weight = etValue.text.toString().toFloatOrNull()
        val link = etLink.text.toString().takeIf { it.isNotBlank() }

        when {
            name.isEmpty() -> {
                etName.error = "Введите имя питомца"
            }
            weight == null || weight <= 0 -> {
                etValue.error = "Введите корректный вес"
            }
            feedingTimes.isEmpty() -> {
                Toast.makeText(this, "Добавьте время кормления", Toast.LENGTH_SHORT).show()
            }
            else -> {
                lifecycleScope.launch {
                    try {
                        val userId = viewModel.getCurrentUserId()
                        if (userId <= 0) {
                            Toast.makeText(this@PetAddActivity, "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
                            return@launch
                        }

                        val pet = Pet(userId = userId, name = name, weight = weight, google_drive_link = link)
                        val petId = viewModel.savePet(pet)

                        if (petId > 0) {
                            viewModel.saveFeederTimes(convertToFeederTimes(petId))

                            // сохраняем имя и ID в SharedPreferences
                            getSharedPreferences("app_prefs", MODE_PRIVATE)
                                .edit()
                                .putString("PET_NAME", name)
                                .putLong("PET_ID", petId)
                                .apply()

                            showSuccessDialog(name, weight, feedingTimes.size)
                        } else {
                            runOnUiThread {
                                Toast.makeText(this@PetAddActivity, "Ошибка сохранения питомца", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@PetAddActivity, "Ошибка сохранения: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun showSuccessDialog(name: String, weight: Float, count: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_window, null)
        val tvName = dialogView.findViewById<TextView>(R.id.tvnamePet)
        val tvWeight = dialogView.findViewById<TextView>(R.id.tvValue)
        val tvFeeding = dialogView.findViewById<TextView>(R.id.tvtimeForFood)
        val btnOk = dialogView.findViewById<Button>(R.id.btnDialogOk)

        tvName.text = "Имя: $name"
        tvWeight.text = "Вес: $weight кг"
        tvFeeding.text = "Времён кормления: $count"

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnOk.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, PetActivity::class.java))
            finish()
        }

        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }
}
