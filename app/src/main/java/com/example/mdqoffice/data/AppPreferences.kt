package com.example.mdqoffice.data

import android.content.Context
import android.content.SharedPreferences

enum class ScreenMode(val displayName: String, val description: String) {
    FULLSCREEN_IMMERSIVE("Full Screen Immersive", "Status bar dan navigation bar disembunyikan untuk ruang layar maksimal."),
    FULLSCREEN_GESTURE("Full Screen dengan Gesture Navigation", "Sembunyikan status bar sambil mempertahankan navigasi gestur."),
    MODE_NORMAL("Mode Normal", "Tampilkan status bar dan navigation bar Android seperti biasa.")
}

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("mdq_office_prefs", Context.MODE_PRIVATE)

    var screenMode: ScreenMode
        get() {
            val name = prefs.getString("screen_mode", ScreenMode.FULLSCREEN_IMMERSIVE.name)
            return try {
                ScreenMode.valueOf(name ?: ScreenMode.FULLSCREEN_IMMERSIVE.name)
            } catch (e: Exception) {
                ScreenMode.FULLSCREEN_IMMERSIVE
            }
        }
        set(value) {
            prefs.edit().putString("screen_mode", value.name).apply()
        }

    fun getCustomFonts(): List<Pair<String, String>> {
        val raw = prefs.getString("custom_fonts_list", "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split(";").mapNotNull {
            val parts = it.split("|")
            if (parts.size == 2) parts[0] to parts[1] else null
        }
    }

    fun addCustomFont(name: String, path: String) {
        val existing = getCustomFonts().toMutableList()
        existing.removeAll { it.first == name }
        existing.add(name to path)
        val serialized = existing.joinToString(";") { "${it.first}|${it.second}" }
        prefs.edit().putString("custom_fonts_list", serialized).apply()
    }
}
