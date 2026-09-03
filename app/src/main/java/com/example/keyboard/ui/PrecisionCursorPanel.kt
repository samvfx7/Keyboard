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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrecisionCursorPanel(
    colors: KeyboardColorScheme,
    onMoveCursor: (dx: Int, dy: Int) -> Unit,
    onSelectAll: () -> Unit,
    onSelectWord: () -> Unit,
    onCut: () -> Unit,
    onCopy: () -> Unit,
    onPaste: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .background(colors.background)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Precision Cursor & Editing",
                color = colors.accent,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.keyText)
            }
        }

        // Action Buttons Row (Select All, Select Word, Cut, Copy, Paste, Undo, Redo)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            CursorActionButton("Select All", Icons.Default.SelectAll, colors, onSelectAll)
            CursorActionButton("Select Word", null, colors, onSelectWord)
            CursorActionButton("Cut", Icons.Default.ContentCut, colors, onCut)
            CursorActionButton("Copy", Icons.Default.ContentCopy, colors, onCopy)
            CursorActionButton("Paste", Icons.Default.ContentPaste, colors, onPaste)
            CursorActionButton("Undo", Icons.Default.Undo, colors, onUndo)
            CursorActionButton("Redo", Icons.Default.Redo, colors, onRedo)
        }

        // D-Pad Directional Controls
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Up Arrow
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.keyBackground)
                    .clickable { onMoveCursor(0, -1) }
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "Up", tint = colors.keyText)
            }

            // Left / Down / Right Arrows
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.keyBackground)
                        .clickable { onMoveCursor(-1, 0) }
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Left", tint = colors.keyText)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.keyBackground)
                        .clickable { onMoveCursor(0, 1) }
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = "Down", tint = colors.keyText)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.keyBackground)
                        .clickable { onMoveCursor(1, 0) }
                        .padding(horizontal = 24.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Right", tint = colors.keyText)
                }
            }
        }
    }
}

@Composable
private fun CursorActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector?,
    colors: KeyboardColorScheme,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.keyBackground)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = label, tint = colors.keyText, modifier = Modifier.padding(2.dp))
        } else {
            Text(text = label, color = colors.keyText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
