package com.olhadem.app42.ui.theme

import android.content.Context

private const val PREFS_NAME = "app42_settings"
private const val KEY_THEME_MODE = "theme_mode"

/**
 * SharedPreferences - найпростіше сховище "ключ-значення" на диску
 * телефону. Тут ми зберігаємо лише одне значення - обрану тему - щоб
 * вибір користувача не скидався щоразу після повного закриття застосунку
 * (на відміну від звичайного remember{}, який "забуває" все після закриття).
 */
object ThemePreferences {
    fun load(context: Context): ThemeMode {
        val saved = context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_THEME_MODE, ThemeMode.LIGHT.name)
        return try {
            ThemeMode.valueOf(saved ?: ThemeMode.LIGHT.name)
        } catch (e: IllegalArgumentException) {
            ThemeMode.LIGHT
        }
    }

    fun save(context: Context, mode: ThemeMode) {
        context
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_THEME_MODE, mode.name)
            .apply()
    }
}
