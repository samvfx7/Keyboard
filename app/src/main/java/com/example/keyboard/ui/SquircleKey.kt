package com.example.keyboard.ui

import android.view.HapticFeedbackConstants
import android.view.SoundEffectConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyboard.engine.KeyPosition

@Composable
fun SquircleKey(
    char: String,
    modifier: Modifier = Modifier,
    height: Dp = 48.dp,
    squircleRadiusDp: Int = 10,
    colors: KeyboardColorScheme,
    isActionKey: Boolean = false,
    hapticEnabled: Boolean = true,
    soundEnabled: Boolean = false,
    onPositionReported: ((KeyPosition) -> Unit)? = null,
    onKeyPress: (rawX: Float, rawY: Float) -> Unit,
    onKeyLongPress: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    val view = LocalView.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1.0f,
        animationSpec = tween(durationMillis = 60),
        label = "keyScale"
    )

    val shape = RoundedCornerShape(squircleRadiusDp.dp)
    val bgColor = when {
        isPressed -> colors.keyBackgroundPressed
        isActionKey -> colors.actionKeyBackground
        else -> colors.keyBackground
    }

    Box(
        modifier = modifier
            .height(height)
            .padding(horizontal = 2.dp, vertical = 3.dp)
            .scale(scale)
            .shadow(if (isPressed) 1.dp else 2.dp, shape = shape, clip = false)
            .background(bgColor, shape)
            .border(0.5.dp, colors.border.copy(alpha = 0.6f), shape)
            .onGloballyPositioned { coordinates ->
                if (char.isNotEmpty() && onPositionReported != null) {
                    val pos = coordinates.positionInRoot()
                    val size = coordinates.size
                    val keyChar = char.firstOrNull() ?: ' '
                    onPositionReported(
                        KeyPosition(
                            char = keyChar,
                            centerX = pos.x + (size.width / 2f),
                            centerY = pos.y + (size.height / 2f),
                            width = size.width.toFloat(),
                            height = size.height.toFloat()
                        )
                    )
                }
            }
            .pointerInput(char) {
                detectTapGestures(
                    onPress = { offset ->
                        isPressed = true
                        if (hapticEnabled) {
                            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        }
                        if (soundEnabled) {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                        }
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { offset ->
                        onKeyPress(offset.x, offset.y)
                    },
                    onLongPress = {
                        onKeyLongPress?.invoke()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        if (content != null) {
            content()
        } else {
            Text(
                text = char,
                color = colors.keyText,
                fontSize = if (char.length > 1) 13.sp else 18.sp,
                fontWeight = if (isActionKey) FontWeight.SemiBold else FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }
    }
}
