package com.bac.unirooms.ui.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.bac.unirooms.databinding.ActivitySplashBinding
import com.bac.unirooms.ui.provider.ProviderDashboardActivity
import com.bac.unirooms.ui.student.StudentHomeActivity
import com.bac.unirooms.utils.SessionManager

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if (session.isLoggedIn()) {
                val destination = when (session.getUserRole()) {
                    "PROVIDER" -> ProviderDashboardActivity::class.java
                    else -> StudentHomeActivity::class.java
                }
                startActivity(Intent(this, destination))
            } else {
                startActivity(Intent(this, RoleSelectionActivity::class.java))
            }
            finish()
        }, 2000)
    }
}