package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "learned_words")
data class LearnedWordEntity(
    @PrimaryKey val word: String,
    val frequency: Int = 1,
    val lastUsedTimestamp: Long = System.currentTimeMillis(),
    val language: String = "en"
)

@Entity(tableName = "learned_phrases")
data class LearnedPhraseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prefix: String,
    val nextWord: String,
    val count: Int = 1
)

@Entity(tableName = "personal_corrections")
data class PersonalCorrectionEntity(
    @PrimaryKey val misspelled: String,
    val correction: String,
    val count: Int = 1
)

@Entity(tableName = "clipboard_history")
data class ClipboardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isPinned: Boolean = false
)

@Entity(tableName = "user_snippets")
data class SnippetEntity(
    @PrimaryKey val shortcut: String, // e.g. "/email"
    val expansion: String             // e.g. "alex@example.com"
)

@Entity(tableName = "touch_offsets")
data class TouchOffsetEntity(
    @PrimaryKey val keyChar: String, // e.g. "e"
    val avgOffsetX: Float = 0f,
    val avgOffsetY: Float = 0f,
    val sampleCount: Int = 0
)
