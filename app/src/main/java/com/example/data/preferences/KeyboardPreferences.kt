package com.example.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class KeyboardTheme(val displayName: String, val bgHex: String, val keyBgHex: String, val textHex: String, val accentHex: String) {
    AMOLED("AMOLED Pure Black", "#000000", "#121212", "#FFFFFF", "#38BDF8"),
    MIDNIGHT("OLED Midnight", "#0B0F19", "#1E293B", "#F8FAFC", "#6366F1"),
    CHARCOAL("Deep Charcoal", "#18181B", "#27272A", "#FAFAFA", "#10B981"),
    LIGHT("Minimal Light", "#F4F4F5", "#FFFFFF", "#18181B", "#2563EB"),
    CYBER_NEON("Cyber Neon", "#09090B", "#18181B", "#38BDF8", "#F43F5E")
}

class KeyboardPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("aetherkey_prefs", Context.MODE_PRIVATE)

    private val _theme = MutableStateFlow(getThemePreference())
    val theme: StateFlow<KeyboardTheme> = _theme.asStateFlow()

    private val _heightFactor = MutableStateFlow(prefs.getFloat("height_factor", 1.0f))
    val heightFactor: StateFlow<Float> = _heightFactor.asStateFlow()

    private val _squircleRadiusDp = MutableStateFlow(prefs.getInt("squircle_radius", 10))
    val squircleRadiusDp: StateFlow<Int> = _squircleRadiusDp.asStateFlow()

    private val _keySpacingDp = MutableStateFlow(prefs.getInt("key_spacing", 4))
    val keySpacingDp: StateFlow<Int> = _keySpacingDp.asStateFlow()

    private val _hapticStrength = MutableStateFlow(prefs.getFloat("haptic_strength", 0.5f))
    val hapticStrength: StateFlow<Float> = _hapticStrength.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean("sound_enabled", false))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _showNumberRow = MutableStateFlow(prefs.getBoolean("show_number_row", true))
    val showNumberRow: StateFlow<Boolean> = _showNumberRow.asStateFlow()

    private val _showEmojiRow = MutableStateFlow(prefs.getBoolean("show_emoji_row", true))
    val showEmojiRow: StateFlow<Boolean> = _showEmojiRow.asStateFlow()

    private val _autocorrectEnabled = MutableStateFlow(prefs.getBoolean("autocorrect_enabled", true))
    val autocorrectEnabled: StateFlow<Boolean> = _autocorrectEnabled.asStateFlow()

    private val _autocorrectSensitivity = MutableStateFlow(prefs.getFloat("autocorrect_sensitivity", 0.65f))
    val autocorrectSensitivity: StateFlow<Float> = _autocorrectSensitivity.asStateFlow()

    private val _nextWordPrediction = MutableStateFlow(prefs.getBoolean("next_word_prediction", true))
    val nextWordPrediction: StateFlow<Boolean> = _nextWordPrediction.asStateFlow()

    private val _smartLocalModel = MutableStateFlow(prefs.getBoolean("smart_local_model", true))
    val smartLocalModel: StateFlow<Boolean> = _smartLocalModel.asStateFlow()

    private val _swipeTypingEnabled = MutableStateFlow(prefs.getBoolean("swipe_typing_enabled", true))
    val swipeTypingEnabled: StateFlow<Boolean> = _swipeTypingEnabled.asStateFlow()

    private val _personalLearningEnabled = MutableStateFlow(prefs.getBoolean("personal_learning_enabled", true))
    val personalLearningEnabled: StateFlow<Boolean> = _personalLearningEnabled.asStateFlow()

    private val _currentLanguage = MutableStateFlow(prefs.getString("current_language", "en") ?: "en")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _mixedLanguageMode = MutableStateFlow(prefs.getBoolean("mixed_language_mode", true))
    val mixedLanguageMode: StateFlow<Boolean> = _mixedLanguageMode.asStateFlow()

    private val _clipboardHistoryEnabled = MutableStateFlow(prefs.getBoolean("clipboard_history_enabled", true))
    val clipboardHistoryEnabled: StateFlow<Boolean> = _clipboardHistoryEnabled.asStateFlow()

    private val _clipboardAutoExpireMinutes = MutableStateFlow(prefs.getInt("clipboard_expire_min", 60))
    val clipboardAutoExpireMinutes: StateFlow<Int> = _clipboardAutoExpireMinutes.asStateFlow()

    private fun getThemePreference(): KeyboardTheme {
        val name = prefs.getString("theme_name", KeyboardTheme.AMOLED.name) ?: KeyboardTheme.AMOLED.name
        return try {
            KeyboardTheme.valueOf(name)
        } catch (e: Exception) {
            KeyboardTheme.AMOLED
        }
    }

    fun setTheme(theme: KeyboardTheme) {
        prefs.edit().putString("theme_name", theme.name).apply()
        _theme.value = theme
    }

    fun setHeightFactor(factor: Float) {
        prefs.edit().putFloat("height_factor", factor).apply()
        _heightFactor.value = factor
    }

    fun setSquircleRadius(radiusDp: Int) {
        prefs.edit().putInt("squircle_radius", radiusDp).apply()
        _squircleRadiusDp.value = radiusDp
    }

    fun setKeySpacing(spacingDp: Int) {
        prefs.edit().putInt("key_spacing", spacingDp).apply()
        _keySpacingDp.value = spacingDp
    }

    fun setHapticStrength(strength: Float) {
        prefs.edit().putFloat("haptic_strength", strength).apply()
        _hapticStrength.value = strength
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        _soundEnabled.value = enabled
    }

    fun setShowNumberRow(show: Boolean) {
        prefs.edit().putBoolean("show_number_row", show).apply()
        _showNumberRow.value = show
    }

    fun setShowEmojiRow(show: Boolean) {
        prefs.edit().putBoolean("show_emoji_row", show).apply()
        _showEmojiRow.value = show
    }

    fun setAutocorrectEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("autocorrect_enabled", enabled).apply()
        _autocorrectEnabled.value = enabled
    }

    fun setAutocorrectSensitivity(sensitivity: Float) {
        prefs.edit().putFloat("autocorrect_sensitivity", sensitivity).apply()
        _autocorrectSensitivity.value = sensitivity
    }

    fun setNextWordPrediction(enabled: Boolean) {
        prefs.edit().putBoolean("next_word_prediction", enabled).apply()
        _nextWordPrediction.value = enabled
    }

    fun setSmartLocalModel(enabled: Boolean) {
        prefs.edit().putBoolean("smart_local_model", enabled).apply()
        _smartLocalModel.value = enabled
    }

    fun setSwipeTypingEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("swipe_typing_enabled", enabled).apply()
        _swipeTypingEnabled.value = enabled
    }

    fun setPersonalLearningEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("personal_learning_enabled", enabled).apply()
        _personalLearningEnabled.value = enabled
    }

    fun setCurrentLanguage(lang: String) {
        prefs.edit().putString("current_language", lang).apply()
        _currentLanguage.value = lang
    }

    fun setMixedLanguageMode(enabled: Boolean) {
        prefs.edit().putBoolean("mixed_language_mode", enabled).apply()
        _mixedLanguageMode.value = enabled
    }

    fun setClipboardHistoryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("clipboard_history_enabled", enabled).apply()
        _clipboardHistoryEnabled.value = enabled
    }

    fun setClipboardAutoExpireMinutes(minutes: Int) {
        prefs.edit().putInt("clipboard_expire_min", minutes).apply()
        _clipboardAutoExpireMinutes.value = minutes
    }
}
