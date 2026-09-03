package com.example.keyboard.engine

import com.example.data.db.AetherDao
import com.example.data.db.TouchOffsetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.exp
import kotlin.math.hypot

data class KeyPosition(
    val char: Char,
    val centerX: Float,
    val centerY: Float,
    val width: Float,
    val height: Float
)

data class ProbableChar(
    val char: Char,
    val probability: Float
)

class AdaptiveHitboxEngine(private val dao: AetherDao, private val scope: CoroutineScope) {

    // Key centroids map (character -> offset)
    private val offsets = mutableMapOf<Char, Pair<Float, Float>>()
    private val sampleCounts = mutableMapOf<Char, Int>()

    init {
        scope.launch(Dispatchers.IO) {
            try {
                dao.getAllTouchOffsets().collect { list ->
                    list.forEach { offset ->
                        val ch = offset.keyChar.firstOrNull() ?: return@forEach
                        offsets[ch] = Pair(offset.avgOffsetX, offset.avgOffsetY)
                        sampleCounts[ch] = offset.sampleCount
                    }
                }
            } catch (e: Exception) {
                // Fallback
            }
        }
    }

    /**
     * Given a raw touch coordinate (x, y) and the layout's current key positions,
     * returns candidate characters sorted by probability.
     */
    fun decodeTouch(
        touchX: Float,
        touchY: Float,
        keys: List<KeyPosition>,
        topCount: Int = 3
    ): List<ProbableChar> {
        if (keys.isEmpty()) return emptyList()

        val probabilities = keys.map { key ->
            val charLower = key.char.lowercaseChar()
            val (offX, offY) = offsets[charLower] ?: Pair(0f, 0f)

            // Adjusted centroid based on learned user touch bias
            val adjustedCenterX = key.centerX + offX
            val adjustedCenterY = key.centerY + offY

            val dx = touchX - adjustedCenterX
            val dy = touchY - adjustedCenterY
            val dist = hypot(dx, dy)

            // Standard deviation tuned to key width (e.g. 0.45 * key width)
            val sigma = (key.width * 0.45f).coerceAtLeast(10f)
            val prob = exp(-(dist * dist) / (2 * sigma * sigma))

            ProbableChar(key.char, prob)
        }

        val totalProb = probabilities.sumOf { it.probability.toDouble() }.toFloat().coerceAtLeast(0.0001f)
        return probabilities
            .map { ProbableChar(it.char, it.probability / totalProb) }
            .sortedByDescending { it.probability }
            .take(topCount)
    }

    /**
     * Learn from user touch history when a word is confirmed.
     * Gradually shifts key touch centroids while capping maximum offset to prevent runaway distortion.
     */
    fun learnTouchOffset(typedChar: Char, rawTouchX: Float, rawTouchY: Float, keyPos: KeyPosition) {
        val charLower = typedChar.lowercaseChar()
        val currentOffset = offsets[charLower] ?: Pair(0f, 0f)
        val currentCount = sampleCounts[charLower] ?: 0

        val dx = rawTouchX - keyPos.centerX
        val dy = rawTouchY - keyPos.centerY

        // Max cap: 25% of key dimension
        val maxOffsetX = keyPos.width * 0.25f
        val maxOffsetY = keyPos.height * 0.25f

        // Exponential moving average update
        val alpha = 0.15f
        val newOffsetX = (currentOffset.first * (1 - alpha) + dx * alpha).coerceIn(-maxOffsetX, maxOffsetX)
        val newOffsetY = (currentOffset.second * (1 - alpha) + dy * alpha).coerceIn(-maxOffsetY, maxOffsetY)
        val newCount = (currentCount + 1).coerceAtMost(1000)

        offsets[charLower] = Pair(newOffsetX, newOffsetY)
        sampleCounts[charLower] = newCount

        scope.launch(Dispatchers.IO) {
            dao.insertTouchOffset(
                TouchOffsetEntity(
                    keyChar = charLower.toString(),
                    avgOffsetX = newOffsetX,
                    avgOffsetY = newOffsetY,
                    sampleCount = newCount
                )
            )
        }
    }

    fun clearCalibration() {
        offsets.clear()
        sampleCounts.clear()
        scope.launch(Dispatchers.IO) {
            dao.clearTouchOffsets()
        }
    }
}
