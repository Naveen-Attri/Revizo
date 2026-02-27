package com.example.revizo.data.repository

import androidx.lifecycle.LiveData
import com.example.revizo.data.dao.FlashcardDao
import com.example.revizo.data.dao.UserStatsDao
import com.example.revizo.data.dao.DeckDao
import com.example.revizo.data.dao.TagDao
import com.example.revizo.data.dao.FlashcardTagDao
import com.example.revizo.data.dao.BattleHistoryDao
import com.example.revizo.data.entity.Flashcard
import com.example.revizo.data.entity.UserStats
import com.example.revizo.data.entity.BattleHistory
import com.example.revizo.util.DateUtils
import com.example.revizo.util.SpacedRepetitionScheduler
import com.example.revizo.util.XPCalculator
import java.util.*

class GameRepository(
    private val flashcardDao: FlashcardDao,
    private val userStatsDao: UserStatsDao,
    private val deckDao: DeckDao? = null,
    private val tagDao: TagDao? = null,
    private val flashcardTagDao: FlashcardTagDao? = null,
    private val battleHistoryDao: BattleHistoryDao? = null
) {

    // User Stats
    fun getUserStatsLive(): LiveData<UserStats?> = userStatsDao.getUserStatsLive()

    suspend fun getUserStats(): UserStats? = userStatsDao.getUserStats()

    suspend fun updateUserStats(userStats: UserStats) = userStatsDao.updateUserStats(userStats)

    suspend fun addXp(xp: Int) = userStatsDao.addXp(xp)

    suspend fun updateLevel(level: Int) = userStatsDao.updateLevel(level)

    suspend fun incrementCorrect() = userStatsDao.incrementCorrect()

    suspend fun incrementIncorrect() = userStatsDao.incrementIncorrect()

    suspend fun incrementBattles() = userStatsDao.incrementBattles()

    suspend fun updateStreak(streak: Int) = userStatsDao.updateStreak(streak)

    suspend fun checkAndUpdateStreak() {
        val stats = getUserStats() ?: return
        val lastPlayed = stats.lastPlayedDate
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val yesterday = today - 86400000L // 24 hours in milliseconds

        val newStreak = when {
            lastPlayed >= today -> stats.dailyStreak // Already played today
            lastPlayed >= yesterday -> stats.dailyStreak + 1 // Played yesterday, increment
            else -> 1 // Streak broken, reset to 1
        }

        val updatedStats = stats.copy(
            dailyStreak = newStreak,
            lastPlayedDate = System.currentTimeMillis()
        )
        updateUserStats(updatedStats)
    }

    // Flashcards
    suspend fun getRandomFlashcards(limit: Int = 10): List<Flashcard> =
        flashcardDao.getRandomFlashcards(limit)

    suspend fun getDueFlashcards(limit: Int = 10, deckId: Int? = null): List<Flashcard>? {
        val currentTime = System.currentTimeMillis()
        return if (deckId != null) {
            flashcardDao.getDueFlashcardsByDeck(deckId, currentTime, limit)
        } else {
            flashcardDao.getDueFlashcards(currentTime, limit)
        }
    }

    suspend fun getPracticeFlashcards(limit: Int = 10, deckId: Int? = null): List<Flashcard>? {
        return if (deckId != null) {
            flashcardDao.getFlashcardsByDeck(deckId, limit)
        } else {
            flashcardDao.getRandomFlashcards(limit)
        }
    }

    suspend fun updateFlashcardStats(flashcardId: Int, isCorrect: Boolean) {
        val timestamp = System.currentTimeMillis()
        if (isCorrect) {
            flashcardDao.incrementCorrectCount(flashcardId, timestamp)
        } else {
            flashcardDao.incrementWrongCount(flashcardId, timestamp)
        }
    }

    /**
     * Process answer using SM-2 algorithm
     */
    suspend fun processAnswer(
        flashcard: Flashcard,
        isCorrect: Boolean,
        responseTimeMs: Long
    ): Int {
        // Calculate SM-2 quality rating
        val quality = SpacedRepetitionScheduler.calculateQuality(
            isCorrect, responseTimeMs, flashcard.difficulty
        )

        // Update flashcard with SM-2
        val updatedCard = SpacedRepetitionScheduler.updateCard(flashcard, quality)
        flashcardDao.updateFlashcard(updatedCard.copy(
            correctCount = if (isCorrect) flashcard.correctCount + 1 else flashcard.correctCount,
            wrongCount = if (isCorrect) flashcard.wrongCount else flashcard.wrongCount + 1,
            lastSeenTimestamp = System.currentTimeMillis()
        ))

        // Calculate XP
        return if (isCorrect) XPCalculator.calculateXP(flashcard.difficulty) else 0
    }

    /**
     * Complete battle with all stats updates
     */
    suspend fun completeBattle(
        correctAnswers: Int,
        totalQuestions: Int,
        totalXP: Int,
        averageResponseTime: Long = 0L,
        deckId: Int? = null,
        isDailyChallenge: Boolean = false
    ) {
        // Add XP
        addXp(totalXP)

        // Check for level up
        val stats = getUserStats() ?: return
        val newLevel = XPCalculator.calculateLevel(stats.totalXp + totalXP)
        if (newLevel > stats.currentLevel) {
            updateLevel(newLevel)
        }

        // Update streak
        val currentTime = System.currentTimeMillis()
        val lastPlayed = stats.lastPlayedDate
        if (DateUtils.isToday(lastPlayed)) {
            // Already played today, keep streak
        } else if (lastPlayed > 0 && DateUtils.isSameDay(lastPlayed, currentTime - 24 * 60 * 60 * 1000)) {
            // Played yesterday, increment streak
            updateStreak(stats.dailyStreak + 1)
        } else {
            // Missed a day or first time, reset to 1
            updateStreak(1)
        }
        userStatsDao.updateLastPlayedDate(currentTime)
        incrementBattles()

        // Save battle history if DAO available
        battleHistoryDao?.let {
            val history = BattleHistory(
                timestamp = currentTime,
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                xpEarned = totalXP,
                averageResponseTime = averageResponseTime,
                deckId = deckId,
                isDailyChallenge = isDailyChallenge
            )
            it.insertBattleHistory(history)
        }

        // Update daily challenge if applicable
        if (isDailyChallenge) {
            userStatsDao.updateLastDailyChallengeDate(DateUtils.getStartOfDay())
        }
    }

    // Game Logic
    fun calculateXp(difficulty: String): Int {
        return when (difficulty) {
            "EASY" -> 10
            "MEDIUM" -> 20
            "HARD" -> 40
            else -> 10
        }
    }

    fun calculateLevel(totalXp: Int): Int {
        return (totalXp / 500) + 1
    }

    fun getXpForNextLevel(currentLevel: Int): Int {
        return currentLevel * 500
    }

    fun getXpProgressInLevel(totalXp: Int): Int {
        return totalXp % 500
    }

    fun calculateStreakBonus(consecutiveCorrect: Int): Int {
        return if (consecutiveCorrect >= 3 && consecutiveCorrect % 3 == 0) {
            25 // Bonus XP for every 3 correct in a row
        } else {
            0
        }
    }
}
