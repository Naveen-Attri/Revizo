package com.example.revizo.data.repository

import com.example.revizo.data.dao.*
import com.example.revizo.data.entity.*
import com.example.revizo.util.DateUtils
import com.example.revizo.util.SpacedRepetitionScheduler
import com.example.revizo.util.XPCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

/**
 * Enhanced Repository for game logic with SM-2 spaced repetition support
 */
class EnhancedGameRepository(
    private val flashcardDao: FlashcardDao,
    private val userStatsDao: UserStatsDao,
    private val deckDao: DeckDao,
    private val tagDao: TagDao,
    private val flashcardTagDao: FlashcardTagDao,
    private val battleHistoryDao: BattleHistoryDao
) {

    // User Stats Operations
    fun getUserStatsFlow(): Flow<UserStats?> = userStatsDao.getUserStatsFlow()

    suspend fun getUserStats(): UserStats? = userStatsDao.getUserStats()

    suspend fun updateUserStats(stats: UserStats) = userStatsDao.updateUserStats(stats)

    // Flashcard Operations with SM-2
    suspend fun getDueFlashcards(limit: Int, deckId: Int? = null): List<Flashcard> {
        val currentTime = System.currentTimeMillis()
        return if (deckId != null) {
            flashcardDao.getDueFlashcardsByDeck(deckId, currentTime, limit)
        } else {
            flashcardDao.getDueFlashcards(currentTime, limit)
        }
    }

    suspend fun getDueCardCount(deckId: Int? = null): Int {
        val currentTime = System.currentTimeMillis()
        return if (deckId != null) {
            flashcardDao.getDueCardCountByDeck(deckId, currentTime)
        } else {
            flashcardDao.getDueCardCount(currentTime)
        }
    }

    suspend fun getPracticeFlashcards(limit: Int, deckId: Int? = null): List<Flashcard> {
        return if (deckId != null) {
            flashcardDao.getFlashcardsByDeck(deckId, limit)
        } else {
            flashcardDao.getRandomFlashcards(limit)
        }
    }

    /**
     * Process answer and update flashcard using SM-2 algorithm
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
        val xp = if (isCorrect) XPCalculator.calculateXP(flashcard.difficulty) else 0

        // Update user stats
        if (isCorrect) {
            userStatsDao.incrementCorrect()
        } else {
            userStatsDao.incrementIncorrect()
        }
        userStatsDao.addResponseTime(responseTimeMs)

        return xp
    }

    /**
     * Complete a battle and update stats
     */
    suspend fun completeBattle(
        correctAnswers: Int,
        totalQuestions: Int,
        totalXP: Int,
        averageResponseTime: Long,
        deckId: Int? = null,
        isDailyChallenge: Boolean = false
    ) {
        // Add XP
        userStatsDao.addXp(totalXP)

        // Check for level up
        val stats = getUserStats() ?: return
        val newLevel = XPCalculator.calculateLevel(stats.totalXp + totalXP)
        if (newLevel > stats.currentLevel) {
            userStatsDao.updateLevel(newLevel)
        }

        // Update streak
        val currentTime = System.currentTimeMillis()
        val lastPlayed = stats.lastPlayedDate
        if (DateUtils.isToday(lastPlayed)) {
            // Already played today, keep streak
        } else if (DateUtils.isSameDay(lastPlayed, currentTime - 24 * 60 * 60 * 1000)) {
            // Played yesterday, increment streak
            userStatsDao.updateStreak(stats.dailyStreak + 1)
        } else {
            // Missed a day, reset streak
            userStatsDao.updateStreak(1)
        }
        userStatsDao.updateLastPlayedDate(currentTime)
        userStatsDao.incrementBattles()

        // Save battle history
        val history = BattleHistory(
            timestamp = currentTime,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            xpEarned = totalXP,
            averageResponseTime = averageResponseTime,
            deckId = deckId,
            isDailyChallenge = isDailyChallenge
        )
        battleHistoryDao.insertBattleHistory(history)

        // Update daily challenge if applicable
        if (isDailyChallenge) {
            userStatsDao.updateLastDailyChallengeDate(DateUtils.getStartOfDay())
        }
    }

    /**
     * Check if daily challenge is available
     */
    suspend fun isDailyChallengeAvailable(): Boolean {
        val stats = getUserStats() ?: return true
        return !DateUtils.isToday(stats.lastDailyChallengeDate)
    }

    /**
     * Get daily challenge flashcards (5 random due cards or practice cards)
     */
    suspend fun getDailyChallengeCards(): List<Flashcard> {
        val dueCards = getDueFlashcards(5)
        return if (dueCards.size >= 5) {
            dueCards
        } else {
            val remaining = 5 - dueCards.size
            dueCards + getPracticeFlashcards(remaining)
        }
    }

    // Deck Operations
    fun getAllDecks(): Flow<List<Deck>> = deckDao.getAllDecksFlow()

    suspend fun insertDeck(deck: Deck): Long = deckDao.insertDeck(deck)

    suspend fun updateDeck(deck: Deck) = deckDao.updateDeck(deck)

    suspend fun deleteDeck(deck: Deck) {
        flashcardDao.deleteFlashcardsByDeck(deck.id)
        deckDao.deleteDeck(deck)
    }

    suspend fun getDeckById(id: Int): Deck? = deckDao.getDeckById(id)

    // Tag Operations
    fun getAllTags(): Flow<List<Tag>> = tagDao.getAllTagsFlow()

    suspend fun insertTag(tag: Tag): Long = tagDao.insertTag(tag)

    suspend fun getOrCreateTag(name: String): Tag {
        return tagDao.getTagByName(name) ?: run {
            val id = tagDao.insertTag(Tag(name = name))
            Tag(id = id.toInt(), name = name)
        }
    }

    // Battle History & Analytics
    fun getRecentBattles(limit: Int = 30): Flow<List<BattleHistory>> =
        battleHistoryDao.getRecentBattlesFlow(limit)

    suspend fun getBattlesInLast30Days(): List<BattleHistory> {
        val thirtyDaysAgo = DateUtils.getDaysAgo(30)
        return battleHistoryDao.getBattlesSince(thirtyDaysAgo)
    }

    suspend fun getAverageResponseTime(): Long {
        val thirtyDaysAgo = DateUtils.getDaysAgo(30)
        return battleHistoryDao.getAverageResponseTimeSince(thirtyDaysAgo) ?: 0L
    }

    /**
     * Calculate accuracy by deck
     */
    suspend fun getAccuracyByDeck(deckId: Int): Int {
        val cards = flashcardDao.getFlashcardsByDeckFlow(deckId).firstOrNull() ?: emptyList()
        val totalCorrect = cards.sumOf { it.correctCount }
        val totalWrong = cards.sumOf { it.wrongCount }
        return XPCalculator.calculateAccuracy(totalCorrect, totalWrong)
    }

    /**
     * Get weakest tags (tags with lowest accuracy)
     */
    suspend fun getWeakestTags(limit: Int = 5): List<Pair<Tag, Int>> {
        // TODO: Implement complex query joining flashcards with tags
        // For now, return empty list
        return emptyList()
    }

    /**
     * Reset all progress (for settings)
     */
    suspend fun resetAllProgress() {
        val defaultStats = UserStats()
        userStatsDao.updateUserStats(defaultStats)
        battleHistoryDao.deleteAllHistory()

        // Reset all flashcards to initial SM-2 state
        val allCards = flashcardDao.getAllFlashcardsFlow().firstOrNull() ?: emptyList()
        allCards.forEach { card ->
            val resetCard = SpacedRepetitionScheduler.resetCard(card).copy(
                correctCount = 0,
                wrongCount = 0,
                lastSeenTimestamp = 0L
            )
            flashcardDao.updateFlashcard(resetCard)
        }
    }
}

