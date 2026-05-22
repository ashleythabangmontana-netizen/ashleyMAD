package com.bac.unirooms.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bac.unirooms.data.model.User
import com.bac.unirooms.data.repository.FirebaseManager
import com.bac.unirooms.databinding.ActivityRegisterBinding
import com.bac.unirooms.ui.provider.ProviderDashboardActivity
import com.bac.unirooms.ui.student.StudentHomeActivity
import com.bac.unirooms.utils.SessionManager

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var session: SessionManager
    private var role: String = "STUDENT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        role = intent.getStringExtra("role") ?: "STUDENT"

        val roleLabel = if (role == "PROVIDER") "Landlord / Provider" else "Student"
        binding.tvRegisterSubtitle.text = "$roleLabel Registration"

        binding.btnRegister.setOnClickListener {
            performRegistration()
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun performRegistration() {
        binding.tvError.visibility = View.GONE
        val fullName = binding.etFullName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim().lowercase()
        val phone = binding.etPhone.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirm = binding.etConfirmPassword.text.toString()

        if (fullName.isEmpty()) { showError("Full name is required"); return }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { showError("Please enter a valid email"); return }
        if (phone.isEmpty()) { showError("Phone number is required"); return }
        if (password.length < 6) { showError("Password must be at least 6 characters"); return }
        if (password != confirm) { showError("Passwords do not match"); return }

        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Creating account..."

        val user = User(
            fullName = fullName,
            email = email,
            password = password,
            phone = phone,
            role = role
        )

        FirebaseManager.registerUser(user) { success, result ->
            runOnUiThread {
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "Create Account"
                if (success) {
                    session.saveSession(result, fullName, email, role)
                    val destination = if (role == "PROVIDER") {
                        ProviderDashboardActivity::class.java
                    } else {
                        StudentHomeActivity::class.java
                    }
                    startActivity(Intent(this, destination))
                    finishAffinity()
                } else {
                    showError(result)
                }
            }
        }
    }

    private fun showError(msg: String) {
        binding.tvError.text = msg
        binding.tvError.visibility = View.VISIBLE
    }
}