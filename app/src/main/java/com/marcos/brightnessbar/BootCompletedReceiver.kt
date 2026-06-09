package com.marcos.brightnessbar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val prefs = AppPreferences(context)
            if (prefs.autoStart) {
                // Waking up the process. The system will auto-bind to the enabled accessibility service.
            }
        }
    }
}
