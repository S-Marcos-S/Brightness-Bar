package com.marcos.brightnessbar

import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService

class BrightnessQsTileService : TileService() {

    private lateinit var prefs: AppPreferences

    override fun onCreate() {
        super.onCreate()
        prefs = AppPreferences(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()
        prefs.isEnabled = !prefs.isEnabled
        updateTile()
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val isEnabled = prefs.isEnabled
        
        tile.state = if (isEnabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.app_name)
        tile.icon = Icon.createWithResource(this, R.drawable.ic_launcher_foreground) 
        tile.updateTile()
    }
}
