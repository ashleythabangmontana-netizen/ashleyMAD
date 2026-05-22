package com.bac.unirooms.ui.student

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.bac.unirooms.databinding.ActivityFilterBinding
import com.bac.unirooms.utils.ReceiptGenerator
import com.bac.unirooms.utils.SessionManager
import java.util.Calendar

class FilterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFilterBinding
    private lateinit var session: SessionManager
    private var selectedDate: Long = Long.MAX_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val locations = resources.getStringArray(com.bac.unirooms.R.array.gaborone_locations)
        val locationAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)
        binding.spinnerLocation.adapter = locationAdapter

        binding.etMinPrice.setText(session.getFilterMin().toInt().toString())
        binding.etMaxPrice.setText(session.getFilterMax().toInt().toString())

        val savedLocation = session.getFilterLocation()
        if (savedLocation.isNotEmpty()) {
            val index = locations.indexOfFirst { it.equals(savedLocation, ignoreCase = true) }
            if (index >= 0) binding.spinnerLocation.setSelection(index)
        }

        binding.btnPickDate.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                cal.set(year, month, day)
                selectedDate = cal.timeInMillis
                binding.tvSelectedDate.text = "Available by: ${ReceiptGenerator.formatDateShort(selectedDate)}"
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.btnApplyFilter.setOnClickListener {
            applyFilter()
        }

        binding.btnClearFilter.setOnClickListener {
            val result = Intent()
            result.putExtra("minPrice", 700.0)
            result.putExtra("maxPrice", 2000.0)
            result.putExtra("location", "")
            result.putExtra("date", Long.MAX_VALUE)
            setResult(RESULT_OK, result)
            finish()
        }
    }

    private fun applyFilter() {
        val minText = binding.etMinPrice.text.toString()
        val maxText = binding.etMaxPrice.text.toString()

        val min = if (minText.isEmpty()) 700.0 else minText.toDoubleOrNull() ?: 700.0
        val max = if (maxText.isEmpty()) 2000.0 else maxText.toDoubleOrNull() ?: 2000.0

        if (min > max) {
            binding.etMinPrice.error = "Min must be less than max"
            return
        }

        val locationIndex = binding.spinnerLocation.selectedItemPosition
        val locations = resources.getStringArray(com.bac.unirooms.R.array.gaborone_locations)
        val location = if (locationIndex == 0) "" else locations[locationIndex]

        if (binding.cbSavePreferences.isChecked) {
            session.saveFilterPreferences(min.toFloat(), max.toFloat(), location)
        }

        val result = Intent()
        result.putExtra("minPrice", min)
        result.putExtra("maxPrice", max)
        result.putExtra("location", location)
        result.putExtra("date", selectedDate)
        setResult(RESULT_OK, result)
        finish()
    }
}