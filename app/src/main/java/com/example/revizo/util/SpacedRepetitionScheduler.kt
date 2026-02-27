package com.example.revizo.util

import com.example.revizo.data.entity.Flashcard
import kotlin.math.max

/**
 * SM-2 (SuperMemo 2) Spaced Repetition Algorithm Implementation
 *
 * This scheduler calculates optimal review intervals based on user performance.
 * Quality ratings: 0-2 (fail), 3-5 (pass)
 */
object SpacedRepetitionScheduler {

    /**
     * Update flashcard based on answer quality using SM-2 algorithm
     *
     * @param flashcard Current flashcard
     * @param quality Answer quality (0-5):
     *   0 = Complete blackout
     *   1 = Incorrect, but remembered upon seeing answer
     *   2 = Incorrect, but seemed easy to recall
     *   3 = Correct, but required significant effort
     *   4 = Correct, with some hesitation
     *   5 = Perfect recall
     * @return Updated flashcard with new SM-2 parameters
     */
    fun updateCard(flashcard: Flashcard, quality: Int): Flashcard {
        require(quality in 0..5) { "Quality must be between 0 and 5" }

        val currentTime = System.currentTimeMillis()

        // If quality < 3, reset repetition count and start over
        if (quality < 3) {
            return flashcard.copy(
                repetitionCount = 0,
                intervalDays = 0,
                nextReviewTimestamp = currentTime, // Review again immediately (in current session)
                lastReviewedTimestamp = currentTime,
                easeFactor = calculateNewEaseFactor(flashcard.easeFactor, quality)
            )
        }

        // Calculate new interval
        val newInterval = calculateInterval(flashcard.repetitionCount, flashcard.intervalDays)
        val newEaseFactor = calculateNewEaseFactor(flashcard.easeFactor, quality)
        val nextReview = currentTime + (newInterval * 24 * 60 * 60 * 1000L) // Convert days to milliseconds

        return flashcard.copy(
            repetitionCount = flashcard.repetitionCount + 1,
            intervalDays = newInterval,
            nextReviewTimestamp = nextReview,
            lastReviewedTimestamp = currentTime,
            easeFactor = newEaseFactor
        )
    }

    /**
     * Calculate new interval based on repetition count
     */
    private fun calculateInterval(repetitionCount: Int, currentInterval: Int): Int {
        return when (repetitionCount) {
            0 -> 1      // First repetition: 1 day
            1 -> 6      // Second repetition: 6 days
            else -> (currentInterval * 2.5).toInt() // Subsequent: multiply by 2.5
        }
    }

    /**
     * Calculate new ease factor based on quality rating
     * Formula: EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
     * Where q is quality and EF is ease factor
     */
    private fun calculateNewEaseFactor(currentEF: Double, quality: Int): Double {
        val newEF = currentEF + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
        // Ease factor minimum is 1.3
        return max(1.3, newEF)
    }

    /**
     * Convert answer correctness and response time to SM-2 quality rating
     *
     * @param isCorrect Whether answer was correct
     * @param responseTimeMs Time taken to answer in milliseconds
     * @param difficultyLevel Card difficulty ("EASY", "MEDIUM", "HARD")
     * @return Quality rating 0-5
     */
    fun calculateQuality(isCorrect: Boolean, responseTimeMs: Long, difficultyLevel: String): Int {
        if (!isCorrect) {
            // Incorrect answers get 0-2 based on speed (faster = more confident wrong answer)
            return when {
                responseTimeMs < 3000 -> 0  // Very fast wrong answer = complete guess
                responseTimeMs < 7000 -> 1  // Medium speed = partial knowledge
                else -> 2                    // Slow = close to recalling
            }
        }

        // Correct answers get 3-5 based on difficulty and response time
        val timeThreshold = when (difficultyLevel) {
            "EASY" -> 5000L
            "MEDIUM" -> 8000L
            "HARD" -> 12000L
            else -> 8000L
        }

        return when {
            responseTimeMs < timeThreshold / 2 -> 5  // Very fast = perfect recall
            responseTimeMs < timeThreshold -> 4      // Normal speed = good recall
            else -> 3                                 // Slow = required effort
        }
    }

    /**
     * Reset a flashcard to initial SM-2 state
     */
    fun resetCard(flashcard: Flashcard): Flashcard {
        return flashcard.copy(
            easeFactor = 2.5,
            repetitionCount = 0,
            intervalDays = 0,
            nextReviewTimestamp = System.currentTimeMillis(),
            lastReviewedTimestamp = 0L
        )
    }

    /**
     * Check if a card is due for review
     */
    fun isDue(flashcard: Flashcard, currentTime: Long = System.currentTimeMillis()): Boolean {
        return flashcard.nextReviewTimestamp <= currentTime
    }
}

