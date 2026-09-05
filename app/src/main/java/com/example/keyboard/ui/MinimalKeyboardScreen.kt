package com.example.keyboard.ui

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardReturn
import androidx.compose.material.icons.filled.SpaceBar
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BgDark = Color(0xFF000000)
private val KeyBg = Color(0xFF141417)
private val KeyBgPressed = Color(0xFF27272A)
private val ActionKeyBg = Color(0xFF1F1F23)
private val KeyText = Color(0xFFFAFAFA)
private val TextMuted = Color(0xFF71717A)
private val AccentBlue = Color(0xFF3B82F6)
private val BorderColor = Color(0xFF27272A)

enum class SimpleKeyboardPage {
    LETTERS, SYMBOLS
}

@Composable
fun MinimalKeyboardScreen(
    onKeyChar: (Char) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit
) {
    var page by remember { mutableStateOf(SimpleKeyboardPage.LETTERS) }
    var isShifted by remember { mutableStateOf(false) }

    val keyHeight = 48.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgDark)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        when (page) {
            SimpleKeyboardPage.LETTERS -> {
                // Row 1: QWERTYUIOP
                val row1 = listOf('q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p')
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row1.forEach { ch ->
                        val charToSend = if (isShifted) ch.uppercaseChar() else ch
                        MinimalKey(
                            text = charToSend.toString(),
                            modifier = Modifier.weight(1f),
                            height = keyHeight,
                            onClick = { onKeyChar(charToSend) }
                        )
                    }
                }

                // Row 2: ASDFGHJKL
                val row2 = listOf('a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l')
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    row2.forEach { ch ->
                        val charToSend = if (isShifted) ch.uppercaseChar() else ch
                        MinimalKey(
                            text = charToSend.toString(),
                            modifier = Modifier.weight(1f),
                            height = keyHeight,
                            onClick = { onKeyChar(charToSend) }
                        )
                    }
                }

                // Row 3: Shift + ZXCVBNM + Backspace
                val row3 = listOf('z', 'x', 'c', 'v', 'b', 'n', 'm')
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Shift
                    MinimalKey(
                        text = "⇧",
                        modifier = Modifier.weight(1.4f),
                        height = keyHeight,
                        isAction = true,
                        textColor = if (isShifted) AccentBlue else KeyText,
                        onClick = { isShifted = !isShifted }
                    )

                    row3.forEach { ch ->
                        val charToSend = if (isShifted) ch.uppercaseChar() else ch
                        MinimalKey(
                            text = charToSend.toString(),
                            modifier = Modifier.weight(1f),
                            height = keyHeight,
                            onClick = { onKeyChar(charToSend) }
                        )
                    }

                    // Backspace
                    MinimalKey(
                        text = "⌫",
                        modifier = Modifier.weight(1.4f),
                        height = keyHeight,
                        isAction = true,
                        onClick = onBackspace
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Backspace",
                            tint = KeyText
                        )
                    }
                }

                // Row 4: ?123, Space, ., Enter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Symbol Switch
                    MinimalKey(
                        text = "?123",
                        modifier = Modifier.weight(1.4f),
                        height = keyHeight,
                        isAction = true,
                        onClick = { page = SimpleKeyboardPage.SYMBOLS }
                    )

                    // Spacebar
                    MinimalKey(
                        text = "space",
                        modifier = Modifier.weight(4.5f),
                        height = keyHeight,
                        onClick = onSpace
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SpaceBar,
                                contentDescription = "Space",
                                tint = TextMuted
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SPACE",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Period
                    MinimalKey(
                        text = ".",
                        modifier = Modifier.weight(1.2f),
                        height = keyHeight,
                        onClick = { onKeyChar('.') }
                    )

                    // Enter
                    MinimalKey(
                        text = "enter",
                        modifier = Modifier.weight(1.5f),
                        height = keyHeight,
                        isAction = true,
                        onClick = onEnter
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                            contentDescription = "Enter",
                            tint = AccentBlue
                        )
                    }
                }
            }

            SimpleKeyboardPage.SYMBOLS -> {
                // Row 1: 1234567890
                val symRow1 = listOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0')
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    symRow1.forEach { ch ->
                        MinimalKey(
                            text = ch.toString(),
                            modifier = Modifier.weight(1f),
                            height = keyHeight,
                            onClick = { onKeyChar(ch) }
                        )
                    }
                }

                // Row 2: @#$%&-+()/
                val symRow2 = listOf('@', '#', '$', '%', '&', '-', '+', '(', ')', '/')
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    symRow2.forEach { ch ->
                        MinimalKey(
                            text = ch.toString(),
                            modifier = Modifier.weight(1f),
                            height = keyHeight,
                            onClick = { onKeyChar(ch) }
                        )
                    }
                }

                // Row 3: *"' :;!? + Backspace
                val symRow3 = listOf('*', '"', '\'', ':', ';', '!', '?')
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    symRow3.forEach { ch ->
                        MinimalKey(
                            text = ch.toString(),
                            modifier = Modifier.weight(1f),
                            height = keyHeight,
                            onClick = { onKeyChar(ch) }
                        )
                    }

                    // Backspace
                    MinimalKey(
                        text = "⌫",
                        modifier = Modifier.weight(1.4f),
                        height = keyHeight,
                        isAction = true,
                        onClick = onBackspace
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Backspace",
                            tint = KeyText
                        )
                    }
                }

                // Row 4: ABC, Comma, Space, ., Enter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Switch back to ABC
                    MinimalKey(
                        text = "ABC",
                        modifier = Modifier.weight(1.4f),
                        height = keyHeight,
                        isAction = true,
                        onClick = { page = SimpleKeyboardPage.LETTERS }
                    )

                    // Comma
                    MinimalKey(
                        text = ",",
                        modifier = Modifier.weight(1.2f),
                        height = keyHeight,
                        onClick = { onKeyChar(',') }
                    )

                    // Spacebar
                    MinimalKey(
                        text = "space",
                        modifier = Modifier.weight(3.5f),
                        height = keyHeight,
                        onClick = onSpace
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SpaceBar,
                                contentDescription = "Space",
                                tint = TextMuted
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SPACE",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Period
                    MinimalKey(
                        text = ".",
                        modifier = Modifier.weight(1.2f),
                        height = keyHeight,
                        onClick = { onKeyChar('.') }
                    )

                    // Enter
                    MinimalKey(
                        text = "enter",
                        modifier = Modifier.weight(1.5f),
                        height = keyHeight,
                        isAction = true,
                        onClick = onEnter
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardReturn,
                            contentDescription = "Enter",
                            tint = AccentBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MinimalKey(
    text: String,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    isAction: Boolean = false,
    textColor: Color = KeyText,
    onClick: () -> Unit,
    content: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val view = LocalView.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = tween(durationMillis = 50),
        label = "keyPressScale"
    )

    val shape = RoundedCornerShape(10.dp)
    val bgColor = when {
        isPressed -> KeyBgPressed
        isAction -> ActionKeyBg
        else -> KeyBg
    }

    Box(
        modifier = modifier
            .height(height)
            .scale(scale)
            .shadow(if (isPressed) 1.dp else 2.dp, shape = shape, clip = false)
            .clip(shape)
            .background(bgColor)
            .border(1.dp, BorderColor, shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    try {
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    } catch (_: Exception) {}
                    try {
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    } catch (_: Exception) {}
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (content != null) {
            content()
        } else {
            Text(
                text = text,
                color = textColor,
                fontSize = if (text.length > 2) 13.sp else 18.sp,
                fontWeight = if (isAction) FontWeight.Bold else FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
