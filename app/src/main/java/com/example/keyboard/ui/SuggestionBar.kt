package com.example.keyboard.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyboard.engine.PredictionResult

enum class KeyboardPanel {
    NONE, EMOJI, CLIPBOARD, CURSOR_CONTROL
}

@Composable
fun SuggestionBar(
    prediction: PredictionResult,
    colors: KeyboardColorScheme,
    activePanel: KeyboardPanel,
    currentLanguage: String,
    isSmartModelActive: Boolean,
    onSuggestionSelected: (String) -> Unit,
    onEmojiSelected: (String) -> Unit,
    onPanelToggle: (KeyboardPanel) -> Unit,
    onOpenSettings: () -> Unit,
    onToggleLanguage: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp)
            .background(colors.suggestionBarBg)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Quick Tool Icons on the Left
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            IconButton(
                onClick = { onPanelToggle(if (activePanel == KeyboardPanel.CLIPBOARD) KeyboardPanel.NONE else KeyboardPanel.CLIPBOARD) }
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Clipboard",
                    tint = if (activePanel == KeyboardPanel.CLIPBOARD) colors.accent else colors.keyText.copy(alpha = 0.6f)
                )
            }

            IconButton(
                onClick = { onPanelToggle(if (activePanel == KeyboardPanel.EMOJI) KeyboardPanel.NONE else KeyboardPanel.EMOJI) }
            ) {
                Icon(
                    imageVector = Icons.Default.SentimentSatisfied,
                    contentDescription = "Emojis",
                    tint = if (activePanel == KeyboardPanel.EMOJI) colors.accent else colors.keyText.copy(alpha = 0.6f)
                )
            }

            IconButton(
                onClick = { onPanelToggle(if (activePanel == KeyboardPanel.CURSOR_CONTROL) KeyboardPanel.NONE else KeyboardPanel.CURSOR_CONTROL) }
            ) {
                Icon(
                    imageVector = Icons.Default.OpenWith,
                    contentDescription = "Precision Cursor",
                    tint = if (activePanel == KeyboardPanel.CURSOR_CONTROL) colors.accent else colors.keyText.copy(alpha = 0.6f)
                )
            }
        }

        // Suggestions in Center
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (prediction.mainSuggestion.isNotEmpty()) {
                // Main Suggestion Pill
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.accent.copy(alpha = 0.2f))
                        .clickable { onSuggestionSelected(prediction.mainSuggestion) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = prediction.mainSuggestion,
                        color = colors.accent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Alternatives
            prediction.alternativeSuggestions.forEach { alt ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSuggestionSelected(alt) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = alt,
                        color = colors.keyText.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Emoji Suggestions
            prediction.emojiSuggestions.forEach { emoji ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .clip(CircleShape)
                        .clickable { onEmojiSelected(emoji) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    Text(text = emoji, fontSize = 16.sp)
                }
            }
        }

        // Status & Settings on Right
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // Language Indicator Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.keyBackground)
                    .clickable { onToggleLanguage() }
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = currentLanguage.uppercase(),
                    color = colors.keyText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Smart Model / Privacy Shield Indicator
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Offline Local AI Active",
                tint = if (isSmartModelActive) colors.accent else colors.keyText.copy(alpha = 0.3f),
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = colors.keyText.copy(alpha = 0.6f)
                )
            }
        }
    }
}
