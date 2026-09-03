package com.example.keyboard.engine

import kotlin.math.min

class AutocorrectEngine(private val languageEngine: LanguageEngine) {

    // Undo stack keeping raw string vs corrected string
    var lastCorrectionOriginal: String? = null
        private set
    var lastCorrectionReplacement: String? = null
        private set

    /**
     * Damerau-Levenshtein edit distance algorithm supporting transpositions.
     */
    fun calculateEditDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }

        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j

        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1].lowercaseChar() == s2[j - 1].lowercaseChar()) 0 else 1
                dp[i][j] = min(
                    dp[i - 1][j] + 1, // deletion
                    min(
                        dp[i][j - 1] + 1, // insertion
                        dp[i - 1][j - 1] + cost // substitution
                    )
                )
                // Transposition
                if (i > 1 && j > 1 &&
                    s1[i - 1].lowercaseChar() == s2[j - 2].lowercaseChar() &&
                    s1[i - 2].lowercaseChar() == s2[j - 1].lowercaseChar()
                ) {
                    dp[i][j] = min(dp[i][j], dp[i - 2][j - 2] + cost)
                }
            }
        }
        return dp[len1][len2]
    }

    /**
     * Confidence-based autocorrection logic.
     * Evaluates whether a raw typed word should be auto-replaced.
     * Returns the corrected string if confident, or null if original should be preserved.
     */
    fun evaluateAutocorrect(
        typedWord: String,
        candidateWords: List<String>,
        personalLearnedWords: Set<String>,
        lang: String,
        sensitivity: Float = 0.65f,
        isPasswordOrCode: Boolean = false
    ): String? {
        if (isPasswordOrCode || typedWord.length < 2) return null

        val lowerTyped = typedWord.lowercase()

        // 1. Check contraction override first (e.g. "dont" -> "don't", "im" -> "I'm")
        val contraction = languageEngine.getContraction(lowerTyped)
        if (contraction != null) {
            recordCorrection(typedWord, contraction)
            return contraction
        }

        // 2. Do not correct if the exact word is in dictionary or user's personal learned vocabulary
        if (personalLearnedWords.contains(lowerTyped)) return null

        val dictWords = languageEngine.getDictionaryForLanguage(lang)
        if (dictWords.any { it.word.lowercase() == lowerTyped }) return null

        // 3. Find closest candidates
        var bestCandidate: String? = null
        var minDistance = Int.MAX_VALUE
        var bestScore = 0f

        for (candidate in candidateWords) {
            val candLower = candidate.lowercase()
            val editDist = calculateEditDistance(lowerTyped, candLower)

            // Max allowed edit distance based on word length
            val maxAllowedDist = when {
                typedWord.length <= 3 -> 1
                typedWord.length <= 7 -> 2
                else -> 3
            }

            if (editDist <= maxAllowedDist) {
                // Score combines edit distance & word length similarity
                val lengthDelta = Math.abs(typedWord.length - candidate.length)
                val similarityScore = 1f - (editDist.toFloat() / typedWord.length.toFloat().coerceAtLeast(1f))

                if (similarityScore > bestScore && similarityScore >= (1f - sensitivity)) {
                    bestScore = similarityScore
                    minDistance = editDist
                    bestCandidate = candidate
                }
            }
        }

        // 4. Do not aggressively change slang, proper nouns, or words if confidence is low
        if (bestCandidate != null && minDistance > 0) {
            recordCorrection(typedWord, bestCandidate)
            return bestCandidate
        }

        return null
    }

    private fun recordCorrection(original: String, replacement: String) {
        lastCorrectionOriginal = original
        lastCorrectionReplacement = replacement
    }

    /**
     * Reverts last autocorrection if user pressed Backspace immediately after correction.
     */
    fun undoLastCorrection(currentText: String): String? {
        val replacement = lastCorrectionReplacement ?: return null
        val original = lastCorrectionOriginal ?: return null

        if (currentText.endsWith(replacement)) {
            val reverted = currentText.substring(0, currentText.length - replacement.length) + original
            clearUndo()
            return reverted
        }
        return null
    }

    fun clearUndo() {
        lastCorrectionOriginal = null
        lastCorrectionReplacement = null
    }
}
