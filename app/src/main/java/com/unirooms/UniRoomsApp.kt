package com.bac.unirooms

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.bac.unirooms.utils.NotificationHelper
import com.bac.unirooms.utils.SessionManager
import com.google.firebase.database.FirebaseDatabase

class UniRoomsApp : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseDatabase.getInstance(
            "https://unirooms-3674a-default-rtdb.europe-west1.firebasedatabase.app"
        ).setPersistenceEnabled(true)

        val session = SessionManager(this)
        val notificationHelper = NotificationHelper(this)
        notificationHelper.createNotificationChannel()

        if (session.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}