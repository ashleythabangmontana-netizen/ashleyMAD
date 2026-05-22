package com.bac.unirooms.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    companion object {
        private const val PREF_NAME = "UniRoomsSession"
        private const val KEY_USER_ID = "userId"
        private const val KEY_USER_NAME = "userName"
        private const val KEY_USER_EMAIL = "userEmail"
        private const val KEY_USER_ROLE = "userRole"
        private const val KEY_IS_LOGGED_IN = "isLoggedIn"
        private const val KEY_DARK_MODE = "darkMode"
        private const val KEY_PREFERRED_CAMPUS = "preferredCampus"
        private const val KEY_NOTIFY_ENABLED = "notifyEnabled"
        private const val KEY_FILTER_MIN = "filterMin"
        private const val KEY_FILTER_MAX = "filterMax"
        private const val KEY_FILTER_LOCATION = "filterLocation"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveSession(userId: String, name: String, email: String, role: String) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_ROLE, role)
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .apply()
    }

    fun getUserId(): String = prefs.getString(KEY_USER_ID, "") ?: ""
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "") ?: ""
    fun getUserRole(): String = prefs.getString(KEY_USER_ROLE, "") ?: ""
    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun isDarkMode(): Boolean = prefs.getBoolean(KEY_DARK_MODE, false)
    fun setDarkMode(enabled: Boolean) = prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()

    fun getPreferredCampus(): String = prefs.getString(KEY_PREFERRED_CAMPUS, "Botswana Accountancy College (BAC)") ?: "Botswana Accountancy College (BAC)"
    fun setPreferredCampus(campus: String) = prefs.edit().putString(KEY_PREFERRED_CAMPUS, campus).apply()

    fun isNotificationsEnabled(): Boolean = prefs.getBoolean(KEY_NOTIFY_ENABLED, true)
    fun setNotificationsEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_NOTIFY_ENABLED, enabled).apply()

    fun saveFilterPreferences(min: Float, max: Float, location: String) {
        prefs.edit()
            .putFloat(KEY_FILTER_MIN, min)
            .putFloat(KEY_FILTER_MAX, max)
            .putString(KEY_FILTER_LOCATION, location)
            .apply()
    }

    fun getFilterMin(): Float = prefs.getFloat(KEY_FILTER_MIN, 700f)
    fun getFilterMax(): Float = prefs.getFloat(KEY_FILTER_MAX, 2000f)
    fun getFilterLocation(): String = prefs.getString(KEY_FILTER_LOCATION, "") ?: ""

    fun clearSession() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_ROLE)
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .apply()
    }
}