package com.example.keyboard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.keyboard.engine.EmojiEngine

@Composable
fun EmojiPickerPanel(
    emojiEngine: EmojiEngine,
    colors: KeyboardColorScheme,
    onEmojiSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Smileys") }

    val categories = listOf("Smileys", "Animals", "Food", "Activities", "Objects", "Symbols")

    val displayedEmojis = remember(searchQuery, selectedCategory) {
        if (searchQuery.isNotEmpty()) {
            emojiEngine.searchEmojis(searchQuery)
        } else {
            emojiEngine.getEmojisByCategory(selectedCategory)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(colors.background)
            .padding(6.dp)
    ) {
        // Search & Category Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Search Input
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.keyBackground)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = colors.keyText.copy(alpha = 0.5f)
                )
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    cursorBrush = SolidColor(colors.accent),
                    textStyle = androidx.compose.ui.text.TextStyle(color = colors.keyText, fontSize = 13.sp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                )
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Emojis",
                    tint = colors.keyText
                )
            }
        }

        // Categories Row
        if (searchQuery.isEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                categories.forEach { cat ->
                    val isSelected = cat == selectedCategory
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) colors.accent.copy(alpha = 0.25f) else colors.keyBackground)
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = cat,
                            color = if (isSelected) colors.accent else colors.keyText.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        // Emoji Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 4.dp)
        ) {
            items(displayedEmojis) { emoji ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onEmojiSelected(emoji) }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 22.sp)
                }
            }
        }
    }
}
