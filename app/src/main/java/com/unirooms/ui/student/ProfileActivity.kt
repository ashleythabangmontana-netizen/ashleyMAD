package com.bac.unirooms.ui.student

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bac.unirooms.databinding.ActivityProfileBinding
import com.bac.unirooms.ui.auth.RoleSelectionActivity
import com.bac.unirooms.utils.SessionManager

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var session: SessionManager

    // BAC is first
    private val campuses = listOf(
        "Botswana Accountancy College (BAC)",
        "University of Botswana (UB)",
        "Botho University",
        "BAISAGO University",
        "Limkokwing University",
        "Botswana Open University (BOU)"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        binding.tvProfileName.text = session.getUserName()
        binding.tvProfileRole.text = if (session.getUserRole() == "PROVIDER") "Landlord / Provider" else "Student"
        binding.tvProfileEmail.text = session.getUserEmail()

        binding.switchDarkMode.isChecked = session.isDarkMode()
        binding.switchNotifications.isChecked = session.isNotificationsEnabled()

        val campusAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            campuses
        )
        binding.spinnerCampus.adapter = campusAdapter

        val preferred = session.getPreferredCampus()
        val prefIndex = campuses.indexOfFirst { it == preferred }
        binding.spinnerCampus.setSelection(if (prefIndex >= 0) prefIndex else 0)

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            session.setDarkMode(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            session.setNotificationsEnabled(isChecked)
            val msg = if (isChecked) "Notifications enabled" else "Notifications disabled"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.spinnerCampus.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    session.setPreferredCampus(campuses[position])
                }

                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }

        binding.btnLogout.setOnClickListener {
            session.clearSession()
            startActivity(Intent(this, RoleSelectionActivity::class.java))
            finishAffinity()
        }
    }
}