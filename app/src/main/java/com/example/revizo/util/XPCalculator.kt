package com.example.revizo.util

/**
 * XP and Leveling System Utilities
 */
object XPCalculator {

    const val XP_PER_LEVEL = 500
    const val XP_EASY = 10
    const val XP_MEDIUM = 20
    const val XP_HARD = 40
    const val XP_STREAK_BONUS_3 = 10
    const val XP_STREAK_BONUS_5 = 25
    const val XP_DAILY_CHALLENGE = 100
    const val XP_BOSS_MULTIPLIER = 2

    /**
     * Calculate XP for a correct answer
     */
    fun calculateXP(difficulty: String, isBossRound: Boolean = false): Int {
        val baseXP = when (difficulty.uppercase()) {
            "EASY" -> XP_EASY
            "MEDIUM" -> XP_MEDIUM
            "HARD" -> XP_HARD
            else -> XP_MEDIUM
        }
        return if (isBossRound) baseXP * XP_BOSS_MULTIPLIER else baseXP
    }

    /**
     * Calculate streak bonus XP
     */
    fun calculateStreakBonus(currentStreak: Int): Int {
        return when {
            currentStreak >= 5 -> XP_STREAK_BONUS_5
            currentStreak >= 3 -> XP_STREAK_BONUS_3
            else -> 0
        }
    }

    /**
     * Calculate level from total XP
     */
    fun calculateLevel(totalXP: Int): Int {
        return (totalXP / XP_PER_LEVEL) + 1
    }

    /**
     * Calculate XP progress within current level (0-1)
     */
    fun calculateLevelProgress(totalXP: Int): Float {
        val xpInCurrentLevel = totalXP % XP_PER_LEVEL
        return xpInCurrentLevel.toFloat() / XP_PER_LEVEL
    }

    /**
     * Calculate XP needed for next level
     */
    fun calculateXPForNextLevel(totalXP: Int): Int {
        val xpInCurrentLevel = totalXP % XP_PER_LEVEL
        return XP_PER_LEVEL - xpInCurrentLevel
    }

    /**
     * Check if user leveled up after gaining XP
     */
    fun didLevelUp(oldXP: Int, newXP: Int): Boolean {
        return calculateLevel(oldXP) < calculateLevel(newXP)
    }

    /**
     * Calculate accuracy percentage
     */
    fun calculateAccuracy(correct: Int, incorrect: Int): Int {
        val total = correct + incorrect
        if (total == 0) return 0
        return ((correct.toFloat() / total) * 100).toInt()
    }
}

