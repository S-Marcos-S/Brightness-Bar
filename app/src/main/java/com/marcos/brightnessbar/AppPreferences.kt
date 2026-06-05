package com.marcos.brightnessbar

import android.content.Context

class AppPreferences(context: Context) {
    private val sharedPreferences = context.getSharedPreferences("brightness_bar_prefs", Context.MODE_PRIVATE)

    var isEnabled: Boolean
        get() = sharedPreferences.getBoolean("is_enabled", true)
        set(value) = sharedPreferences.edit().putBoolean("is_enabled", value).apply()

    fun registerListener(listener: (String) -> Unit): android.content.SharedPreferences.OnSharedPreferenceChangeListener {
        val spListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key != null) listener(key)
        }
        sharedPreferences.registerOnSharedPreferenceChangeListener(spListener)
        return spListener
    }

    fun unregisterListener(listener: android.content.SharedPreferences.OnSharedPreferenceChangeListener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
