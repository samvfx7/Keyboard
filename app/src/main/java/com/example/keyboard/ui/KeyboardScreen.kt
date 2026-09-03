package com.example.keyboard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardReturn
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.KeyboardPreferences
import com.example.keyboard.engine.ClipboardManagerEngine
import com.example.keyboard.engine.EmojiEngine
import com.example.keyboard.engine.KeyPosition
import com.example.keyboard.engine.PredictionResult
import com.example.keyboard.engine.TouchPoint

enum class KeyboardPage {
    LETTERS, SYMBOLS, EXTRA_SYMBOLS, NUMPAD
}

@Composable
fun KeyboardScreen(
    prefs: KeyboardPreferences,
    predictionResult: PredictionResult,
    emojiEngine: EmojiEngine,
    clipboardEngine: ClipboardManagerEngine,
    isPasswordMode: Boolean = false,
    isNumericMode: Boolean = false,
    onKeyChar: (Char, rawX: Float, rawY: Float) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onSuggestionSelected: (String) -> Unit,
    onEmojiSelected: (String) -> Unit,
    onMoveCursor: (dx: Int, dy: Int) -> Unit,
    onSwipePathCompleted: (List<TouchPoint>) -> Unit,
    onSelectAll: () -> Unit,
    onSelectWord: () -> Unit,
    onCut: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onOpenSettings: () -> Unit,
    onToggleLanguage: () -> Unit,
    onReportKeyPosition: (KeyPosition) -> Unit
) {
    val currentTheme by prefs.theme.collectAsState()
    val heightFactor by prefs.heightFactor.collectAsState()
    val squircleRadius by prefs.squircleRadiusDp.collectAsState()
    val keySpacing by prefs.keySpacingDp.collectAsState()
    val hapticEnabled by prefs.hapticStrength.collectAsState()
    val soundEnabled by prefs.soundEnabled.collectAsState()
    val showNumberRow by prefs.showNumberRow.collectAsState()
    val currentLanguage by prefs.currentLanguage.collectAsState()
    val swipeEnabled by prefs.swipeTypingEnabled.collectAsState()

    val colors = getKeyboardColorScheme(currentTheme)

    var keyboardPage by remember(isNumericMode) {
        mutableStateOf(if (isNumericMode) KeyboardPage.NUMPAD else KeyboardPage.LETTERS)
    }
    var isShifted by remember { mutableStateOf(false) }
    var activePanel by remember { mutableStateOf(KeyboardPanel.NONE) }

    // Swipe trail drawing path
    val swipeTrail = remember { mutableStateListOf<TouchPoint>() }

    val baseKeyHeight = (46 * heightFactor).dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
    ) {
        // 1. Suggestion & Toolbar Bar (Disabled in password fields for strict privacy)
        if (!isPasswordMode && !isNumericMode) {
            SuggestionBar(
                prediction = predictionResult,
                colors = colors,
                activePanel = activePanel,
                currentLanguage = currentLanguage,
                isSmartModelActive = predictionResult.isSmartModelActive,
                onSuggestionSelected = onSuggestionSelected,
                onEmojiSelected = onEmojiSelected,
                onPanelToggle = { activePanel = it },
                onOpenSettings = onOpenSettings,
                onToggleLanguage = onToggleLanguage
            )
        }

        // 2. Active Auxiliary Panel (Emoji, Clipboard, Precision Cursor)
        when (activePanel) {
            KeyboardPanel.EMOJI -> EmojiPickerPanel(
                emojiEngine = emojiEngine,
                colors = colors,
                onEmojiSelected = {
                    onEmojiSelected(it)
                    activePanel = KeyboardPanel.NONE
                },
                onClose = { activePanel = KeyboardPanel.NONE }
            )
            KeyboardPanel.CLIPBOARD -> ClipboardPanel(
                clipboardEngine = clipboardEngine,
                colors = colors,
                onInsertText = {
                    onSuggestionSelected(it)
                    activePanel = KeyboardPanel.NONE
                },
                onClose = { activePanel = KeyboardPanel.NONE }
            )
            KeyboardPanel.CURSOR_CONTROL -> PrecisionCursorPanel(
                colors = colors,
                onMoveCursor = onMoveCursor,
                onSelectAll = onSelectAll,
                onSelectWord = onSelectWord,
                onCut = onCut,
                onCopy = onCopy,
                onPaste = onPaste,
                onUndo = onUndo,
                onRedo = onRedo,
                onClose = { activePanel = KeyboardPanel.NONE }
            )
            KeyboardPanel.NONE -> {
                // 3. Main Keyboard Keys Container with Swipe Trail overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInput(swipeEnabled, keyboardPage) {
                            if (swipeEnabled && keyboardPage == KeyboardPage.LETTERS) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        swipeTrail.clear()
                                        swipeTrail.add(TouchPoint(offset.x, offset.y))
                                    },
                                    onDrag = { change, _ ->
                                        swipeTrail.add(TouchPoint(change.position.x, change.position.y))
                                    },
                                    onDragEnd = {
                                        if (swipeTrail.size > 5) {
                                            onSwipePathCompleted(swipeTrail.toList())
                                        }
                                        swipeTrail.clear()
                                    },
                                    onDragCancel = { swipeTrail.clear() }
                                )
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                    ) {
                        // Optional Top Number Row
                        if (showNumberRow && keyboardPage == KeyboardPage.LETTERS) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").forEach { num ->
                                    SquircleKey(
                                        char = num,
                                        modifier = Modifier.weight(1f),
                                        height = baseKeyHeight * 0.85f,
                                        squircleRadiusDp = squircleRadius,
                                        colors = colors,
                                        hapticEnabled = hapticEnabled > 0f,
                                        soundEnabled = soundEnabled,
                                        onKeyPress = { rx, ry -> onKeyChar(num[0], rx, ry) }
                                    )
                                }
                            }
                        }

                        // Pages Rendering
                        when (keyboardPage) {
                            KeyboardPage.LETTERS -> LettersLayout(
                                isShifted = isShifted,
                                baseKeyHeight = baseKeyHeight,
                                squircleRadius = squircleRadius,
                                colors = colors,
                                hapticEnabled = hapticEnabled > 0f,
                                soundEnabled = soundEnabled,
                                onReportKeyPosition = onReportKeyPosition,
                                onKeyChar = onKeyChar,
                                onShiftToggle = { isShifted = !isShifted },
                                onBackspace = onBackspace
                            )
                            KeyboardPage.SYMBOLS -> SymbolsLayout(
                                baseKeyHeight = baseKeyHeight,
                                squircleRadius = squircleRadius,
                                colors = colors,
                                hapticEnabled = hapticEnabled > 0f,
                                soundEnabled = soundEnabled,
                                onKeyChar = onKeyChar,
                                onPageToggle = { keyboardPage = KeyboardPage.EXTRA_SYMBOLS },
                                onBackspace = onBackspace
                            )
                            KeyboardPage.EXTRA_SYMBOLS -> ExtraSymbolsLayout(
                                baseKeyHeight = baseKeyHeight,
                                squircleRadius = squircleRadius,
                                colors = colors,
                                hapticEnabled = hapticEnabled > 0f,
                                soundEnabled = soundEnabled,
                                onKeyChar = onKeyChar,
                                onPageToggle = { keyboardPage = KeyboardPage.SYMBOLS },
                                onBackspace = onBackspace
                            )
                            KeyboardPage.NUMPAD -> NumpadLayout(
                                baseKeyHeight = baseKeyHeight,
                                squircleRadius = squircleRadius,
                                colors = colors,
                                hapticEnabled = hapticEnabled > 0f,
                                soundEnabled = soundEnabled,
                                onKeyChar = onKeyChar,
                                onBackspace = onBackspace
                            )
                        }

                        // Bottom Control Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Symbol Page Toggle Button
                            SquircleKey(
                                char = if (keyboardPage == KeyboardPage.LETTERS) "?123" else "ABC",
                                modifier = Modifier.weight(1.3f),
                                height = baseKeyHeight,
                                squircleRadiusDp = squircleRadius,
                                colors = colors,
                                isActionKey = true,
                                hapticEnabled = hapticEnabled > 0f,
                                soundEnabled = soundEnabled,
                                onKeyPress = { _, _ ->
                                    keyboardPage = if (keyboardPage == KeyboardPage.LETTERS) KeyboardPage.SYMBOLS else KeyboardPage.LETTERS
                                }
                            )

                            // Globe / Language Switch Key
                            SquircleKey(
                                char = "globe",
                                modifier = Modifier.weight(1f),
                                height = baseKeyHeight,
                                squircleRadiusDp = squircleRadius,
                                colors = colors,
                                isActionKey = true,
                                hapticEnabled = hapticEnabled > 0f,
                                soundEnabled = soundEnabled,
                                onKeyPress = { _, _ -> onToggleLanguage() }
                            ) {
                                Icon(Icons.Default.Language, contentDescription = "Switch Language", tint = colors.keyText)
                            }

                            // Spacebar with Swipe-to-move-cursor
                            SquircleKey(
                                char = "space",
                                modifier = Modifier.weight(4.5f),
                                height = baseKeyHeight,
                                squircleRadiusDp = squircleRadius,
                                colors = colors,
                                hapticEnabled = hapticEnabled > 0f,
                                soundEnabled = soundEnabled,
                                onKeyPress = { _, _ -> onSpace() },
                                onKeyLongPress = { activePanel = KeyboardPanel.CURSOR_CONTROL }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.SpaceBar,
                                        contentDescription = "Space",
                                        tint = colors.keyText.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = currentLanguage.uppercase(),
                                        color = colors.keyText.copy(alpha = 0.5f),
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }

                            // Period / Comma Key
                            SquircleKey(
                                char = ".",
                                modifier = Modifier.weight(1f),
                                height = baseKeyHeight,
                                squircleRadiusDp = squircleRadius,
                                colors = colors,
                                hapticEnabled = hapticEnabled > 0f,
                                soundEnabled = soundEnabled,
                                onKeyPress = { rx, ry -> onKeyChar('.', rx, ry) }
                            )

                            // Enter Key
                            SquircleKey(
                                char = "enter",
                                modifier = Modifier.weight(1.5f),
                                height = baseKeyHeight,
                                squircleRadiusDp = squircleRadius,
                                colors = colors,
                                isActionKey = true,
                                hapticEnabled = hapticEnabled > 0f,
                                soundEnabled = soundEnabled,
                                onKeyPress = { _, _ -> onEnter() }
                            ) {
                                Icon(Icons.Default.KeyboardReturn, contentDescription = "Enter", tint = colors.accent)
                            }
                        }
                    }

                    // Swipe Trail Canvas overlay
                    if (swipeTrail.isNotEmpty()) {
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val path = Path()
                            path.moveTo(swipeTrail.first().x, swipeTrail.first().y)
                            for (i in 1 until swipeTrail.size) {
                                path.lineTo(swipeTrail[i].x, swipeTrail[i].y)
                            }
                            drawPath(
                                path = path,
                                color = colors.accent,
                                style = Stroke(width = 8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LettersLayout(
    isShifted: Boolean,
    baseKeyHeight: androidx.compose.ui.unit.Dp,
    squircleRadius: Int,
    colors: KeyboardColorScheme,
    hapticEnabled: Boolean,
    soundEnabled: Boolean,
    onReportKeyPosition: (KeyPosition) -> Unit,
    onKeyChar: (Char, Float, Float) -> Unit,
    onShiftToggle: () -> Unit,
    onBackspace: () -> Unit
) {
    val row1 = listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p')
    val row2 = listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l')
    val row3 = listOf('z', 'x', 'c', 'v', 'b', 'n', 'm')

    Column {
        // Row 1
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            row1.forEach { ch ->
                val displayChar = if (isShifted) ch.uppercaseChar() else ch
                SquircleKey(
                    char = displayChar.toString(),
                    modifier = Modifier.weight(1f),
                    height = baseKeyHeight,
                    squircleRadiusDp = squircleRadius,
                    colors = colors,
                    hapticEnabled = hapticEnabled,
                    soundEnabled = soundEnabled,
                    onPositionReported = onReportKeyPosition,
                    onKeyPress = { rx, ry -> onKeyChar(displayChar, rx, ry) }
                )
            }
        }

        // Row 2
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            row2.forEach { ch ->
                val displayChar = if (isShifted) ch.uppercaseChar() else ch
                SquircleKey(
                    char = displayChar.toString(),
                    modifier = Modifier.weight(1f),
                    height = baseKeyHeight,
                    squircleRadiusDp = squircleRadius,
                    colors = colors,
                    hapticEnabled = hapticEnabled,
                    soundEnabled = soundEnabled,
                    onPositionReported = onReportKeyPosition,
                    onKeyPress = { rx, ry -> onKeyChar(displayChar, rx, ry) }
                )
            }
        }

        // Row 3
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            // Shift Key
            SquircleKey(
                char = "shift",
                modifier = Modifier.weight(1.4f),
                height = baseKeyHeight,
                squircleRadiusDp = squircleRadius,
                colors = colors,
                isActionKey = true,
                hapticEnabled = hapticEnabled,
                soundEnabled = soundEnabled,
                onKeyPress = { _, _ -> onShiftToggle() }
            ) {
                Text(
                    text = "⇧",
                    color = if (isShifted) colors.accent else colors.keyText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            row3.forEach { ch ->
                val displayChar = if (isShifted) ch.uppercaseChar() else ch
                SquircleKey(
                    char = displayChar.toString(),
                    modifier = Modifier.weight(1f),
                    height = baseKeyHeight,
                    squircleRadiusDp = squircleRadius,
                    colors = colors,
                    hapticEnabled = hapticEnabled,
                    soundEnabled = soundEnabled,
                    onPositionReported = onReportKeyPosition,
                    onKeyPress = { rx, ry -> onKeyChar(displayChar, rx, ry) }
                )
            }

            // Backspace Key
            SquircleKey(
                char = "backspace",
                modifier = Modifier.weight(1.4f),
                height = baseKeyHeight,
                squircleRadiusDp = squircleRadius,
                colors = colors,
                isActionKey = true,
                hapticEnabled = hapticEnabled,
                soundEnabled = soundEnabled,
                onKeyPress = { _, _ -> onBackspace() }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Backspace", tint = colors.keyText)
            }
        }
    }
}

@Composable
private fun SymbolsLayout(
    baseKeyHeight: androidx.compose.ui.unit.Dp,
    squircleRadius: Int,
    colors: KeyboardColorScheme,
    hapticEnabled: Boolean,
    soundEnabled: Boolean,
    onKeyChar: (Char, Float, Float) -> Unit,
    onPageToggle: () -> Unit,
    onBackspace: () -> Unit
) {
    val row1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
    val row2 = listOf("@", "#", "$", "_", "&", "-", "+", "(", ")", "/")
    val row3 = listOf("*", "\"", "'", ":", ";", "!", "?")

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            row1.forEach { sym ->
                SquircleKey(
                    char = sym,
                    modifier = Modifier.weight(1f),
                    height = baseKeyHeight,
                    squircleRadiusDp = squircleRadius,
                    colors = colors,
                    hapticEnabled = hapticEnabled,
                    soundEnabled = soundEnabled,
                    onKeyPress = { rx, ry -> onKeyChar(sym[0], rx, ry) }
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            row2.forEach { sym ->
                SquircleKey(
                    char = sym,
                    modifier = Modifier.weight(1f),
                    height = baseKeyHeight,
                    squircleRadiusDp = squircleRadius,
                    colors = colors,
                    hapticEnabled = hapticEnabled,
                    soundEnabled = soundEnabled,
                    onKeyPress = { rx, ry -> onKeyChar(sym[0], rx, ry) }
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            SquircleKey(
                char = "=/<",
                modifier = Modifier.weight(1.4f),
                height = baseKeyHeight,
                squircleRadiusDp = squircleRadius,
                colors = colors,
                isActionKey = true,
                hapticEnabled = hapticEnabled,
                soundEnabled = soundEnabled,
                onKeyPress = { _, _ -> onPageToggle() }
            )
            row3.forEach { sym ->
                SquircleKey(
                    char = sym,
                    modifier = Modifier.weight(1f),
                    height = baseKeyHeight,
                    squircleRadiusDp = squircleRadius,
                    colors = colors,
                    hapticEnabled = hapticEnabled,
                    soundEnabled = soundEnabled,
                    onKeyPress = { rx, ry -> onKeyChar(sym[0], rx, ry) }
                )
            }
            SquircleKey(
                char = "backspace",
                modifier = Modifier.weight(1.4f),
                height = baseKeyHeight,
                squircleRadiusDp = squircleRadius,
                colors = colors,
                isActionKey = true,
                hapticEnabled = hapticEnabled,
                soundEnabled = soundEnabled,
                onKeyPress = { _, _ -> onBackspace() }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Backspace", tint = colors.keyText)
            }
        }
    }
}

@Composable
private fun ExtraSymbolsLayout(
    baseKeyHeight: androidx.compose.ui.unit.Dp,
    squircleRadius: Int,
    colors: KeyboardColorScheme,
    hapticEnabled: Boolean,
    soundEnabled: Boolean,
    onKeyChar: (Char, Float, Float) -> Unit,
    onPageToggle: () -> Unit,
    onBackspace: () -> Unit
) {
    val row1 = listOf("~", "`", "|", "•", "√", "π", "÷", "×", "¶", "∆")
    val row2 = listOf("£", "¥", "€", "¢", "^", "°", "=", "{", "}", "\\")
    val row3 = listOf("%", "©", "®", "™", "✓", "[", "]")

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            row1.forEach { sym ->
                SquircleKey(
                    char = sym,
                    modifier = Modifier.weight(1f),
                    height = baseKeyHeight,
                    squircleRadiusDp = squircleRadius,
                    colors = colors,
                    hapticEnabled = hapticEnabled,
                    soundEnabled = soundEnabled,
                    onKeyPress = { rx, ry -> onKeyChar(sym[0], rx, ry) }
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            row2.forEach { sym ->
                SquircleKey(
                    char = sym,
                    modifier = Modifier.weight(1f),
                    height = baseKeyHeight,
                    squircleRadiusDp = squircleRadius,
                    colors = colors,
                    hapticEnabled = hapticEnabled,
                    soundEnabled = soundEnabled,
                    onKeyPress = { rx, ry -> onKeyChar(sym[0], rx, ry) }
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            SquircleKey(
                char = "?123",
                modifier = Modifier.weight(1.4f),
                height = baseKeyHeight,
                squircleRadiusDp = squircleRadius,
                colors = colors,
                isActionKey = true,
                hapticEnabled = hapticEnabled,
                soundEnabled = soundEnabled,
                onKeyPress = { _, _ -> onPageToggle() }
            )
            row3.forEach { sym ->
                SquircleKey(
                    char = sym,
                    modifier = Modifier.weight(1f),
                    height = baseKeyHeight,
                    squircleRadiusDp = squircleRadius,
                    colors = colors,
                    hapticEnabled = hapticEnabled,
                    soundEnabled = soundEnabled,
                    onKeyPress = { rx, ry -> onKeyChar(sym[0], rx, ry) }
                )
            }
            SquircleKey(
                char = "backspace",
                modifier = Modifier.weight(1.4f),
                height = baseKeyHeight,
                squircleRadiusDp = squircleRadius,
                colors = colors,
                isActionKey = true,
                hapticEnabled = hapticEnabled,
                soundEnabled = soundEnabled,
                onKeyPress = { _, _ -> onBackspace() }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Backspace", tint = colors.keyText)
            }
        }
    }
}

@Composable
private fun NumpadLayout(
    baseKeyHeight: androidx.compose.ui.unit.Dp,
    squircleRadius: Int,
    colors: KeyboardColorScheme,
    hapticEnabled: Boolean,
    soundEnabled: Boolean,
    onKeyChar: (Char, Float, Float) -> Unit,
    onBackspace: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("1", "2", "3").forEach { num ->
                SquircleKey(
                    char = num,
                    modifier = Modifier.weight(1f),
                    height = baseKeyHeight * 1.1f,
                    squircleRadiusDp = squircleRadius,
                    colors = colors,
                    hapticEnabled = hapticEnabled,
                    soundEnabled = soundEnabled,
                    onKeyPress = { rx, ry -> onKeyChar(num[0], rx, ry) }
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("4", "5", "6").forEach { num ->
                SquircleKey(
                    char = num,
                    modifier = Modifier.weight(1f),
                    height = baseKeyHeight * 1.1f,
                    squircleRadiusDp = squircleRadius,
                    colors = colors,
                    hapticEnabled = hapticEnabled,
                    soundEnabled = soundEnabled,
                    onKeyPress = { rx, ry -> onKeyChar(num[0], rx, ry) }
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("7", "8", "9").forEach { num ->
                SquircleKey(
                    char = num,
                    modifier = Modifier.weight(1f),
                    height = baseKeyHeight * 1.1f,
                    squircleRadiusDp = squircleRadius,
                    colors = colors,
                    hapticEnabled = hapticEnabled,
                    soundEnabled = soundEnabled,
                    onKeyPress = { rx, ry -> onKeyChar(num[0], rx, ry) }
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            SquircleKey(
                char = ".",
                modifier = Modifier.weight(1f),
                height = baseKeyHeight * 1.1f,
                squircleRadiusDp = squircleRadius,
                colors = colors,
                hapticEnabled = hapticEnabled,
                soundEnabled = soundEnabled,
                onKeyPress = { rx, ry -> onKeyChar('.', rx, ry) }
            )
            SquircleKey(
                char = "0",
                modifier = Modifier.weight(1f),
                height = baseKeyHeight * 1.1f,
                squircleRadiusDp = squircleRadius,
                colors = colors,
                hapticEnabled = hapticEnabled,
                soundEnabled = soundEnabled,
                onKeyPress = { rx, ry -> onKeyChar('0', rx, ry) }
            )
            SquircleKey(
                char = "backspace",
                modifier = Modifier.weight(1f),
                height = baseKeyHeight * 1.1f,
                squircleRadiusDp = squircleRadius,
                colors = colors,
                isActionKey = true,
                hapticEnabled = hapticEnabled,
                soundEnabled = soundEnabled,
                onKeyPress = { _, _ -> onBackspace() }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Backspace", tint = colors.keyText)
            }
        }
    }
}
