package com.example.keyboard.engine

import kotlin.math.hypot

data class TouchPoint(val x: Float, val y: Float, val timestamp: Long = System.currentTimeMillis())

class SwipeTypingEngine(private val languageEngine: LanguageEngine) {

    /**
     * Decodes a swipe touch path into candidate words by computing path distances
     * between touch points and key centroids of dictionary words.
     */
    fun decodeSwipePath(
        pathPoints: List<TouchPoint>,
        keyPositions: List<KeyPosition>,
        lang: String,
        limit: Int = 3
    ): List<String> {
        if (pathPoints.size < 5 || keyPositions.isEmpty()) return emptyList()

        val keyMap = keyPositions.associateBy { it.char.lowercaseChar() }
        val startPoint = pathPoints.first()
        val endPoint = pathPoints.last()

        // Identify candidate start/end key letters
        val startChar = findNearestKeyChar(startPoint.x, startPoint.y, keyPositions)
        val endChar = findNearestKeyChar(endPoint.x, endPoint.y, keyPositions)

        val candidates = languageEngine.getDictionaryForLanguage(lang, mixedMode = true)

        val scoredCandidates = candidates
            .filter { word ->
                val w = word.word.lowercase()
                if (w.length < 3) return@filter false
                if (startChar != null && w.first() != startChar) return@filter false
                if (endChar != null && w.last() != endChar) return@filter false
                true
            }
            .map { word ->
                val score = calculatePathDistanceScore(pathPoints, word.word.lowercase(), keyMap)
                Pair(word.word, score)
            }
            .filter { it.second < 250f } // Distance threshold
            .sortedBy { it.second }
            .map { it.first }
            .distinct()

        return scoredCandidates.take(limit)
    }

    private fun findNearestKeyChar(x: Float, y: Float, keys: List<KeyPosition>): Char? {
        return keys.minByOrNull { hypot(x - it.centerX, y - it.centerY) }?.char?.lowercaseChar()
    }

    private fun calculatePathDistanceScore(
        path: List<TouchPoint>,
        word: String,
        keyMap: Map<Char, KeyPosition>
    ): Float {
        // Collect key centers for the word
        val keyCenters = word.mapNotNull { ch -> keyMap[ch]?.let { TouchPoint(it.centerX, it.centerY) } }
        if (keyCenters.isEmpty()) return Float.MAX_VALUE

        // Sample path to match length of word key sequence
        var totalDistance = 0f
        val step = (path.size / keyCenters.size).coerceAtLeast(1)

        for (i in keyCenters.indices) {
            val pathIdx = (i * step).coerceAtMost(path.lastIndex)
            val pathPt = path[pathIdx]
            val keyPt = keyCenters[i]
            totalDistance += hypot(pathPt.x - keyPt.x, pathPt.y - keyPt.y)
        }

        return totalDistance / keyCenters.size
    }
}
