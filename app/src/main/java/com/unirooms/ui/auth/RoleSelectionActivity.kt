package com.bac.unirooms.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bac.unirooms.databinding.ActivityRoleSelectionBinding

class RoleSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoleSelectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoleSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cardStudent.setOnClickListener {
            openLogin("STUDENT")
        }

        binding.cardProvider.setOnClickListener {
            openLogin("PROVIDER")
        }
    }

    private fun openLogin(role: String) {
        val intent = Intent(this, LoginActivity::class.java)
        intent.putExtra("role", role)
        startActivity(intent)
    }
}