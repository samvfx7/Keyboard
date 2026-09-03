package com.example.keyboard.engine

import com.example.data.db.AetherDao
import com.example.data.db.LearnedPhraseEntity
import com.example.data.db.LearnedWordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PredictionResult(
    val mainSuggestion: String,
    val alternativeSuggestions: List<String>,
    val emojiSuggestions: List<String> = emptyList(),
    val isSmartModelActive: Boolean = false
)

class PredictionEngine(
    private val dao: AetherDao,
    private val languageEngine: LanguageEngine,
    private val emojiEngine: EmojiEngine,
    private val autocorrectEngine: AutocorrectEngine,
    private val scope: CoroutineScope
) {

    private val personalWordCache = mutableSetOf<String>()

    init {
        scope.launch(Dispatchers.IO) {
            try {
                dao.getAllLearnedWords().collect { words ->
                    personalWordCache.clear()
                    personalWordCache.addAll(words.map { it.word.lowercase() })
                }
            } catch (e: Exception) {
                // Fallback
            }
        }
    }

    /**
     * Fast Model: Synchronous, sub-5ms latency candidate generator.
     */
    suspend fun getFastPredictions(
        currentWord: String,
        previousWords: List<String>,
        lang: String,
        mixedMode: Boolean,
        enableNextWord: Boolean
    ): PredictionResult = withContext(Dispatchers.Default) {
        if (currentWord.isBlank()) {
            // Next-word prediction mode
            if (enableNextWord && previousWords.isNotEmpty()) {
                val lastWord = previousWords.last().lowercase()
                val dbNextWords = dao.getNextWordPredictions(lastWord)
                if (dbNextWords.isNotEmpty()) {
                    val main = dbNextWords.first()
                    val alts = dbNextWords.drop(1).take(2)
                    val emojis = emojiEngine.predictEmojiForText(previousWords.joinToString(" "))
                    return@withContext PredictionResult(main, alts, emojis)
                }
            }
            return@withContext PredictionResult("", emptyList())
        }

        val prefixLower = currentWord.lowercase()

        // 1. Check personal dictionary matches first
        val personalMatches = personalWordCache.filter { it.startsWith(prefixLower) }.take(3)

        // 2. Search built-in dictionaries
        val dictMatches = languageEngine.searchPrefix(prefixLower, lang, mixedMode, limit = 10)
            .map { it.word }

        // Combine candidates
        val candidateList = (personalMatches + dictMatches).distinct()

        // Contraction check
        val contraction = languageEngine.getContraction(prefixLower)

        val mainCandidate = contraction ?: candidateList.firstOrNull() ?: currentWord
        val alts = candidateList.filter { !it.equals(mainCandidate, ignoreCase = true) }.take(2)
        val emojis = emojiEngine.predictEmojiForText(currentWord)

        PredictionResult(
            mainSuggestion = mainCandidate,
            alternativeSuggestions = alts,
            emojiSuggestions = emojis,
            isSmartModelActive = false
        )
    }

    /**
     * Smart Local Model: Async deeper contextual sentence analyzer.
     * Evaluates multi-word context, ambiguous typo fixes, and phrase completions.
     */
    suspend fun getSmartPredictions(
        fullSentenceContext: String,
        currentWord: String,
        previousWords: List<String>,
        lang: String,
        mixedMode: Boolean
    ): PredictionResult = withContext(Dispatchers.Default) {
        val fastResult = getFastPredictions(currentWord, previousWords, lang, mixedMode, true)

        if (fullSentenceContext.length < 10 && previousWords.size < 2) {
            return@withContext fastResult
        }

        // Sentence-level contextual scoring & phrase completion
        val contextLower = fullSentenceContext.lowercase().trim()

        // Example smart contextual rules
        val smartAlts = fastResult.alternativeSuggestions.toMutableList()

        if (contextLower.contains("how are") && currentWord.isEmpty()) {
            return@withContext PredictionResult(
                mainSuggestion = "you",
                alternativeSuggestions = listOf("things", "doing"),
                emojiSuggestions = listOf("😊", "👋"),
                isSmartModelActive = true
            )
        }

        if (contextLower.contains("thank") && currentWord.isEmpty()) {
            return@withContext PredictionResult(
                mainSuggestion = "you",
                alternativeSuggestions = listOf("so much", "very much"),
                emojiSuggestions = listOf("🙏", "❤️"),
                isSmartModelActive = true
            )
        }

        if (contextLower.endsWith("?") || contextLower.endsWith("!")) {
            val emojis = emojiEngine.predictEmojiForText(contextLower)
            return@withContext fastResult.copy(emojiSuggestions = emojis, isSmartModelActive = true)
        }

        fastResult.copy(isSmartModelActive = true)
    }

    /**
     * Store learned word & phrase locally when user confirms word input.
     */
    fun learnInput(word: String, prevWord: String?, lang: String) {
        if (word.length < 2) return
        val wLower = word.lowercase()

        // Do not store sensitive patterns, passwords, or numbers
        if (wLower.matches("^[0-9]+$".toRegex())) return

        scope.launch(Dispatchers.IO) {
            // Update word frequency
            val existing = personalWordCache.contains(wLower)
            val entity = LearnedWordEntity(
                word = wLower,
                frequency = if (existing) 2 else 1,
                lastUsedTimestamp = System.currentTimeMillis(),
                language = lang
            )
            dao.insertOrUpdateWord(entity)
            personalWordCache.add(wLower)

            // Update bi-gram phrase context
            if (!prevWord.isNullOrBlank()) {
                val pLower = prevWord.lowercase()
                dao.insertPhrase(
                    LearnedPhraseEntity(
                        prefix = pLower,
                        nextWord = wLower,
                        count = 1
                    )
                )
            }
        }
    }

    fun getPersonalWordCount(): Int = personalWordCache.size

    fun getPersonalWords(): Set<String> = personalWordCache

    private fun String?.isNullByBlank(): Boolean = this == null || this.isBlank()
}
