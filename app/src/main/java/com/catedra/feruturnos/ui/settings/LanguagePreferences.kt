package com.catedra.feruturnos.ui.settings

import android.content.Context

object LanguagePreferences {
    private const val PREF_NAME = "feru_settings"
    private const val KEY_LANGUAGE = "language"

    fun saveLanguage(context: Context, languageCode: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, languageCode)
            .apply()
    }

    fun getLanguage(context: Context): String {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "es") ?: "es"
    }
}