package com.manacdc.nanityping1

import android.content.Context
import android.content.SharedPreferences
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Persisted App Settings for voice, speed, pitch, and storage preferences.
 */
object AppSettings {
    private const val PREFS_NAME = "nani_typing_prefs"

    private const val KEY_SPEED = "speech_speed"
    private const val KEY_PITCH = "speech_pitch"
    private const val KEY_LOCALE = "speech_locale"

    // Default values tuned for young children (LKG stage)
    const val DEFAULT_SPEED = 0.70f
    const val DEFAULT_PITCH = 1.12f
    const val DEFAULT_LOCALE = "en_IN"

    val SPEED_OPTIONS = listOf(
        0.55f to "Very Slow (0.55x)",
        0.70f to "Child Pace (0.70x)",
        1.00f to "Normal (1.00x)"
    )

    val PITCH_OPTIONS = listOf(
        1.00f to "Normal Pitch (1.0x)",
        1.12f to "Child Friendly (1.12x)",
        1.25f to "Higher Pitch (1.25x)"
    )

    val LOCALE_OPTIONS = listOf(
        "en_IN" to "Indian English (en-IN)",
        "en_US" to "US English (en-US)",
        "en_GB" to "UK English (en-GB)"
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getSpeed(context: Context): Float {
        return getPrefs(context).getFloat(KEY_SPEED, DEFAULT_SPEED)
    }

    fun setSpeed(context: Context, speed: Float) {
        getPrefs(context).edit().putFloat(KEY_SPEED, speed).apply()
    }

    fun getPitch(context: Context): Float {
        return getPrefs(context).getFloat(KEY_PITCH, DEFAULT_PITCH)
    }

    fun setPitch(context: Context, pitch: Float) {
        getPrefs(context).edit().putFloat(KEY_PITCH, pitch).apply()
    }

    fun getLocaleCode(context: Context): String {
        return getPrefs(context).getString(KEY_LOCALE, DEFAULT_LOCALE) ?: DEFAULT_LOCALE
    }

    fun setLocaleCode(context: Context, code: String) {
        getPrefs(context).edit().putString(KEY_LOCALE, code).apply()
    }

    fun getLocale(context: Context): Locale {
        return when (getLocaleCode(context)) {
            "en_US" -> Locale.US
            "en_GB" -> Locale.UK
            else -> Locale("en", "IN")
        }
    }

    fun applyToTts(context: Context, tts: TextToSpeech?) {
        if (tts == null) return
        try {
            tts.setSpeechRate(getSpeed(context))
            tts.setPitch(getPitch(context))
            val targetLocale = getLocale(context)
            val result = tts.setLanguage(targetLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts.setLanguage(Locale.US)
            }
        } catch (e: Exception) {
            // Defensive degradation
        }
    }
}
