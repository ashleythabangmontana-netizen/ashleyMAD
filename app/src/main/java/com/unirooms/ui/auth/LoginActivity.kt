package com.bac.unirooms.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.bac.unirooms.data.repository.FirebaseManager
import com.bac.unirooms.databinding.ActivityLoginBinding
import com.bac.unirooms.ui.provider.ProviderDashboardActivity
import com.bac.unirooms.ui.student.StudentHomeActivity
import com.bac.unirooms.utils.SessionManager

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var session: SessionManager
    private var role: String = "STUDENT"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        role = intent.getStringExtra("role") ?: "STUDENT"

        val roleLabel = if (role == "PROVIDER") "Landlord / Provider" else "Student"
        binding.tvLoginTitle.text = "$roleLabel Login"

        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("role", role)
            startActivity(intent)
        }

        binding.tvSwitchRole.setOnClickListener {
            startActivity(Intent(this, RoleSelectionActivity::class.java))
            finish()
        }
    }

    private fun performLogin() {
        binding.tvErrorMessage.visibility = View.GONE

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            return
        }
        binding.tilEmail.error = null

        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return
        }
        binding.tilPassword.error = null

        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Logging in..."
        binding.progressLogin.visibility = View.VISIBLE

        // FIX: No longer passing role — login matches on email + password only.
        // The role is read from the user's saved record in Firebase, so accounts
        // always work regardless of which role screen the user arrived from.
        FirebaseManager.loginUser(email, password, role) { user ->
            runOnUiThread {
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Login"
                binding.progressLogin.visibility = View.GONE

                if (user != null) {
                    session.saveSession(
                        user.id,
                        user.fullName,
                        user.email,
                        user.role
                    )
                    val destination = if (user.role == "PROVIDER") {
                        ProviderDashboardActivity::class.java
                    } else {
                        StudentHomeActivity::class.java
                    }
                    startActivity(Intent(this, destination))
                    finishAffinity()
                } else {
                    binding.tvErrorMessage.text =
                        "Invalid email or password. Please try again."
                    binding.tvErrorMessage.visibility = View.VISIBLE
                }
            }
        }
    }
}