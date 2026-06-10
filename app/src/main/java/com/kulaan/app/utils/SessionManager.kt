package com.kulaan.app.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME   = "kulaan_session"
        private const val KEY_TOKEN   = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_NAME    = "user_name"
        private const val KEY_EMAIL   = "user_email"
        private const val KEY_ROLE    = "user_role"
        private const val KEY_HAS_STORE = "has_store"
    }

    fun saveSession(
        token: String,
        userId: Int,
        name: String,
        email: String,
        role: String,
        hasStore: Boolean = false
    ) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_NAME, name)
            .putString(KEY_EMAIL, email)
            .putString(KEY_ROLE, role)
            .putBoolean(KEY_HAS_STORE, hasStore)
            .apply()
    }

    fun getToken(): String?  = prefs.getString(KEY_TOKEN, null)
    fun getUserId(): Int     = prefs.getInt(KEY_USER_ID, -1)
    fun getName(): String?   = prefs.getString(KEY_NAME, null)
    fun getEmail(): String?  = prefs.getString(KEY_EMAIL, null)
    fun getRole(): String?   = prefs.getString(KEY_ROLE, null)
    fun hasStore(): Boolean  = prefs.getBoolean(KEY_HAS_STORE, false)
    fun isLoggedIn(): Boolean = getToken() != null
    fun isBuyer(): Boolean   = getRole() == "buyer"
    fun isSeller(): Boolean  = getRole() == "seller"

    fun setHasStore(value: Boolean) {
        prefs.edit().putBoolean(KEY_HAS_STORE, value).apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
