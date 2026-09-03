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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ClipboardEntity
import com.example.keyboard.engine.ClipboardManagerEngine

@Composable
fun ClipboardPanel(
    clipboardEngine: ClipboardManagerEngine,
    colors: KeyboardColorScheme,
    onInsertText: (String) -> Unit,
    onClose: () -> Unit
) {
    val clips by clipboardEngine.clipboardHistory.collectAsState(initial = emptyList())
    val snippets by clipboardEngine.snippets.collectAsState(initial = emptyList())

    var searchQuery by remember { mutableStateOf("") }
    var showAddSnippetDialog by remember { mutableStateOf(false) }
    var snippetShortcut by remember { mutableStateOf("") }
    var snippetExpansion by remember { mutableStateOf("") }

    val filteredClips = remember(searchQuery, clips) {
        if (searchQuery.isEmpty()) clips
        else clips.filter { it.text.contains(searchQuery, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(colors.background)
            .padding(6.dp)
    ) {
        // Top Header & Search Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
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
                    textStyle = androidx.compose.ui.text.TextStyle(color = colors.keyText, fontSize = 12.sp),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 6.dp)
                )
            }

            IconButton(onClick = { showAddSnippetDialog = !showAddSnippetDialog }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Snippet Shortcut",
                    tint = colors.accent
                )
            }

            IconButton(onClick = { clipboardEngine.clearAllUnpinned() }) {
                Icon(
                    imageVector = Icons.Default.ClearAll,
                    contentDescription = "Clear Unpinned Clips",
                    tint = colors.keyText.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = colors.keyText
                )
            }
        }

        if (showAddSnippetDialog) {
            // Snippet Creator Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(colors.keyBackground, RoundedCornerShape(8.dp))
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = snippetShortcut,
                    onValueChange = { snippetShortcut = it },
                    placeholder = { Text("/shortcut", color = colors.keyText.copy(alpha = 0.4f), fontSize = 11.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    singleLine = true,
                    modifier = Modifier.width(110.dp)
                )

                OutlinedTextField(
                    value = snippetExpansion,
                    onValueChange = { snippetExpansion = it },
                    placeholder = { Text("expansion text...", color = colors.keyText.copy(alpha = 0.4f), fontSize = 11.sp) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = colors.keyText, fontSize = 12.sp),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        if (snippetShortcut.isNotEmpty() && snippetExpansion.isNotEmpty()) {
                            clipboardEngine.saveSnippet(snippetShortcut, snippetExpansion)
                            snippetShortcut = ""
                            snippetExpansion = ""
                            showAddSnippetDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("Save", fontSize = 11.sp, color = colors.background)
                }
            }
        }

        // Clips & Snippets List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Display active text snippets first
            if (snippets.isNotEmpty() && searchQuery.isEmpty()) {
                item {
                    Text(
                        text = "Text Shortcuts",
                        color = colors.accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                items(snippets) { snip ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.keyBackground)
                            .clickable { onInsertText(snip.expansion) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = snip.shortcut,
                                color = colors.accent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = snip.expansion,
                                color = colors.keyText,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { clipboardEngine.deleteSnippet(snip.shortcut) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Snippet", tint = colors.keyText.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Clipboard History",
                    color = colors.keyText.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                )
            }

            if (filteredClips.isEmpty()) {
                item {
                    Text(
                        text = "Clipboard is empty. Copied text will appear here securely.",
                        color = colors.keyText.copy(alpha = 0.4f),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            items(filteredClips) { clip ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (clip.isPinned) colors.accent.copy(alpha = 0.15f) else colors.keyBackground)
                        .clickable { onInsertText(clip.text) }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = clip.text,
                        color = colors.keyText,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { clipboardEngine.togglePin(clip) }) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pin",
                                tint = if (clip.isPinned) colors.accent else colors.keyText.copy(alpha = 0.4f)
                            )
                        }
                        IconButton(onClick = { clipboardEngine.deleteClip(clip.id) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = colors.keyText.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}
