package com.example.refresh.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Session manager to save and fetch user data from SharedPreferences
 */
class UserSessionManager(context: Context) {

    private val pref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = pref.edit()

    private val TAG = "UserSessionManager"

    /**
     * Save user session data
     */
    fun createLoginSession(name: String, email: String) {
        editor.putBoolean(IS_LOGGED_IN, true)
        editor.putString(KEY_NAME, name)
        editor.putString(KEY_EMAIL, email)
        editor.apply()

        Log.d(TAG, "Session created for user: $name, $email")
    }

    /**
     * Get stored session data
     */
    fun getUserDetails(): HashMap<String, String?> {
        val user = HashMap<String, String?>()
        user[KEY_NAME] = pref.getString(KEY_NAME, "Guest User")
        user[KEY_EMAIL] = pref.getString(KEY_EMAIL, "guest@example.com")

        Log.d(TAG, "Retrieved user data: ${user[KEY_NAME]}, ${user[KEY_EMAIL]}")
        return user
    }

    /**
     * Clear session details
     */
    fun logoutUser() {
        // Clear all data from SharedPreferences
        editor.clear()
        editor.apply()

        Log.d(TAG, "User logged out, session cleared")
    }

    /**
     * Check login status
     */
    fun isLoggedIn(): Boolean {
        return pref.getBoolean(IS_LOGGED_IN, false)
    }

    companion object {
        // Shared preferences file name
        private const val PREF_NAME = "UserSession"

        // All Shared Preferences Keys
        private const val IS_LOGGED_IN = "IsLoggedIn"
        const val KEY_NAME = "userName"
        const val KEY_EMAIL = "userEmail"
    }
}