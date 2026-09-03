package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AetherDao {

    // Learned Words
    @Query("SELECT * FROM learned_words ORDER BY frequency DESC LIMIT 1000")
    fun getAllLearnedWords(): Flow<List<LearnedWordEntity>>

    @Query("SELECT * FROM learned_words WHERE word LIKE :query || '%' ORDER BY frequency DESC LIMIT 20")
    suspend fun findMatchingLearnedWords(query: String): List<LearnedWordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWord(word: LearnedWordEntity)

    @Query("DELETE FROM learned_words")
    suspend fun clearLearnedWords()

    @Query("SELECT COUNT(*) FROM learned_words")
    fun getLearnedWordsCount(): Flow<Int>

    // Learned Phrases
    @Query("SELECT nextWord FROM learned_phrases WHERE prefix = :prefix ORDER BY count DESC LIMIT 5")
    suspend fun getNextWordPredictions(prefix: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrase(phrase: LearnedPhraseEntity)

    @Query("DELETE FROM learned_phrases")
    suspend fun clearLearnedPhrases()

    @Query("SELECT COUNT(*) FROM learned_phrases")
    fun getLearnedPhrasesCount(): Flow<Int>

    // Personal Corrections
    @Query("SELECT correction FROM personal_corrections WHERE misspelled = :misspelled LIMIT 1")
    suspend fun getCorrectionFor(misspelled: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorrection(correction: PersonalCorrectionEntity)

    @Query("DELETE FROM personal_corrections")
    suspend fun clearCorrections()

    // Clipboard History
    @Query("SELECT * FROM clipboard_history ORDER BY isPinned DESC, timestamp DESC")
    fun getClipboardHistory(): Flow<List<ClipboardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClipboardItem(item: ClipboardEntity)

    @Query("DELETE FROM clipboard_history WHERE id = :id")
    suspend fun deleteClipboardItem(id: Long)

    @Query("DELETE FROM clipboard_history WHERE isPinned = 0")
    suspend fun clearUnpinnedClipboard()

    @Query("DELETE FROM clipboard_history WHERE timestamp < :cutoffTime AND isPinned = 0")
    suspend fun expireOldClipboard(cutoffTime: Long)

    // Snippets
    @Query("SELECT * FROM user_snippets ORDER BY shortcut ASC")
    fun getAllSnippets(): Flow<List<SnippetEntity>>

    @Query("SELECT expansion FROM user_snippets WHERE shortcut = :shortcut LIMIT 1")
    suspend fun getSnippetExpansion(shortcut: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: SnippetEntity)

    @Query("DELETE FROM user_snippets WHERE shortcut = :shortcut")
    suspend fun deleteSnippet(shortcut: String)

    // Touch Heatmap / Offsets
    @Query("SELECT * FROM touch_offsets WHERE keyChar = :keyChar LIMIT 1")
    suspend fun getTouchOffset(keyChar: String): TouchOffsetEntity?

    @Query("SELECT * FROM touch_offsets")
    fun getAllTouchOffsets(): Flow<List<TouchOffsetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTouchOffset(offset: TouchOffsetEntity)

    @Query("DELETE FROM touch_offsets")
    suspend fun clearTouchOffsets()
}
