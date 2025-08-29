package com.example.dogs.data

import android.content.Context

// com.example.dogs.data.TokenProvider
class TokenProvider(ctx: Context) {
    private val prefs = ctx.getSharedPreferences("auth", Context.MODE_PRIVATE)

    private companion object {
        const val KEY_TOKEN = "token"
        const val KEY_EXPIRES_AT = "expires_at_epoch"
        const val DAY_MS = 24 * 60 * 60 * 1000L
    }

    fun saveToken(token: String, expiresAtMillis: Long? = null) {
        val exp = expiresAtMillis ?: (System.currentTimeMillis() + DAY_MS)
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_EXPIRES_AT, exp)
            .apply()
    }

    fun getRawToken(): String? = prefs.getString(KEY_TOKEN, null)
    fun getExpiresAt(): Long = prefs.getLong(KEY_EXPIRES_AT, 0L)
    fun isSessionValid(now: Long = System.currentTimeMillis()): Boolean =
        (getRawToken()?.isNotBlank() == true) && getExpiresAt() > now

    fun clear() = prefs.edit().clear().apply()
}