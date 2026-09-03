package com.example.keyboard.ui

import androidx.compose.ui.graphics.Color
import com.example.data.preferences.KeyboardTheme

data class KeyboardColorScheme(
    val background: Color,
    val keyBackground: Color,
    val keyBackgroundPressed: Color,
    val keyText: Color,
    val actionKeyBackground: Color,
    val accent: Color,
    val suggestionBarBg: Color,
    val border: Color
)

fun getKeyboardColorScheme(theme: KeyboardTheme): KeyboardColorScheme {
    return when (theme) {
        KeyboardTheme.AMOLED -> KeyboardColorScheme(
            background = Color(0xFF000000),
            keyBackground = Color(0xFF141417),
            keyBackgroundPressed = Color(0xFF27272A),
            keyText = Color(0xFFFAFAFA),
            actionKeyBackground = Color(0xFF1F1F23),
            accent = Color(0xFF3B82F6),
            suggestionBarBg = Color(0xFF000000),
            border = Color(0xFF27272A)
        )
        KeyboardTheme.MIDNIGHT -> KeyboardColorScheme(
            background = Color(0xFF0B0F19),
            keyBackground = Color(0xFF1E293B),
            keyBackgroundPressed = Color(0xFF334155),
            keyText = Color(0xFFF8FAFC),
            actionKeyBackground = Color(0xFF1E293B),
            accent = Color(0xFF6366F1),
            suggestionBarBg = Color(0xFF0F172A),
            border = Color(0xFF334155)
        )
        KeyboardTheme.CHARCOAL -> KeyboardColorScheme(
            background = Color(0xFF18181B),
            keyBackground = Color(0xFF27272A),
            keyBackgroundPressed = Color(0xFF3F3F46),
            keyText = Color(0xFFFAFAFA),
            actionKeyBackground = Color(0xFF27272A),
            accent = Color(0xFF10B981),
            suggestionBarBg = Color(0xFF18181B),
            border = Color(0xFF3F3F46)
        )
        KeyboardTheme.LIGHT -> KeyboardColorScheme(
            background = Color(0xFFF4F4F5),
            keyBackground = Color(0xFFFFFFFF),
            keyBackgroundPressed = Color(0xFFE4E4E7),
            keyText = Color(0xFF18181B),
            actionKeyBackground = Color(0xFFE4E4E7),
            accent = Color(0xFF2563EB),
            suggestionBarBg = Color(0xFFF4F4F5),
            border = Color(0xFFD4D4D8)
        )
        KeyboardTheme.CYBER_NEON -> KeyboardColorScheme(
            background = Color(0xFF09090B),
            keyBackground = Color(0xFF18181B),
            keyBackgroundPressed = Color(0xFF27272A),
            keyText = Color(0xFF38BDF8),
            actionKeyBackground = Color(0xFF27272A),
            accent = Color(0xFFF43F5E),
            suggestionBarBg = Color(0xFF09090B),
            border = Color(0xFF38BDF8)
        )
    }
}
