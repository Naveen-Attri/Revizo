package com.example.revizo

import com.example.revizo.data.entity.Flashcard
import com.example.revizo.util.SpacedRepetitionScheduler
import org.junit.Assert.*
import org.junit.Test

class SpacedRepetitionSchedulerTest {

    @Test
    fun `perfect recall increases interval correctly`() {
        val card = createTestCard()

        val updated = SpacedRepetitionScheduler.updateCard(card, quality = 5)

        assertEquals(1, updated.repetitionCount)
        assertEquals(1, updated.intervalDays)
        assertTrue(updated.nextReviewTimestamp > System.currentTimeMillis())
        assertTrue(updated.easeFactor >= card.easeFactor) // Ease factor should increase or stay same
    }

    @Test
    fun `incorrect answer resets repetition count`() {
        val card = createTestCard(repetitionCount = 5, intervalDays = 30)

        val updated = SpacedRepetitionScheduler.updateCard(card, quality = 2)

        assertEquals(0, updated.repetitionCount)
        assertEquals(0, updated.intervalDays)
        // Next review should be soon (same session)
        assertTrue(updated.nextReviewTimestamp <= System.currentTimeMillis() + 1000)
    }

    @Test
    fun `ease factor has minimum threshold of 1_3`() {
        var card = createTestCard(easeFactor = 1.4)

        // Multiple mediocre ratings should not drop ease below 1.3
        repeat(20) {
            card = SpacedRepetitionScheduler.updateCard(card, quality = 3)
        }

        assertTrue("Ease factor should not drop below 1.3, was ${card.easeFactor}",
            card.easeFactor >= 1.3)
    }

    @Test
    fun `sequence of correct answers increases intervals exponentially`() {
        var card = createTestCard()

        // First review: quality 4
        card = SpacedRepetitionScheduler.updateCard(card, quality = 4)
        assertEquals(1, card.repetitionCount)
        assertEquals(1, card.intervalDays)

        // Second review: quality 4
        card = SpacedRepetitionScheduler.updateCard(card, quality = 4)
        assertEquals(2, card.repetitionCount)
        assertEquals(6, card.intervalDays)

        // Third review: quality 5
        card = SpacedRepetitionScheduler.updateCard(card, quality = 5)
        assertEquals(3, card.repetitionCount)
        assertTrue("Interval should be > 6 days, was ${card.intervalDays}",
            card.intervalDays > 6)
    }

    @Test
    fun `calculateQuality returns appropriate values for correct answers`() {
        // Very fast correct answer = perfect recall (5)
        assertEquals(5, SpacedRepetitionScheduler.calculateQuality(true, 2000, "EASY"))

        // Moderate speed = good recall (4)
        assertEquals(4, SpacedRepetitionScheduler.calculateQuality(true, 3000, "EASY"))

        // Slow correct answer = required effort (3)
        assertEquals(3, SpacedRepetitionScheduler.calculateQuality(true, 6000, "EASY"))
    }

    @Test
    fun `calculateQuality adjusts thresholds by difficulty`() {
        val responseTime = 5000L

        // Same response time should get different quality ratings based on difficulty
        val easyQuality = SpacedRepetitionScheduler.calculateQuality(true, responseTime, "EASY")
        val mediumQuality = SpacedRepetitionScheduler.calculateQuality(true, responseTime, "MEDIUM")
        val hardQuality = SpacedRepetitionScheduler.calculateQuality(true, responseTime, "HARD")

        // For hard questions, same time should result in better quality rating
        assertTrue("Hard questions should be more forgiving",
            hardQuality >= mediumQuality)
    }

    @Test
    fun `calculateQuality returns low values for incorrect answers`() {
        assertEquals(0, SpacedRepetitionScheduler.calculateQuality(false, 2000, "EASY"))
        assertEquals(1, SpacedRepetitionScheduler.calculateQuality(false, 5000, "EASY"))
        assertEquals(2, SpacedRepetitionScheduler.calculateQuality(false, 10000, "EASY"))
    }

    @Test
    fun `resetCard returns card to initial state`() {
        val card = createTestCard(
            repetitionCount = 10,
            intervalDays = 60,
            easeFactor = 2.8
        )

        val reset = SpacedRepetitionScheduler.resetCard(card)

        assertEquals(2.5, reset.easeFactor, 0.01)
        assertEquals(0, reset.repetitionCount)
        assertEquals(0, reset.intervalDays)
        assertEquals(0L, reset.lastReviewedTimestamp)
    }

    @Test
    fun `isDue returns true for overdue cards`() {
        val overdueCard = createTestCard(
            nextReviewTimestamp = System.currentTimeMillis() - 10000
        )

        assertTrue(SpacedRepetitionScheduler.isDue(overdueCard))
    }

    @Test
    fun `isDue returns false for future cards`() {
        val futureCard = createTestCard(
            nextReviewTimestamp = System.currentTimeMillis() + 100000
        )

        assertFalse(SpacedRepetitionScheduler.isDue(futureCard))
    }

    @Test
    fun `quality rating boundary test`() {
        val card = createTestCard()

        // Should not throw for valid quality values (0-5)
        for (quality in 0..5) {
            val updated = SpacedRepetitionScheduler.updateCard(card, quality)
            assertNotNull(updated)
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `quality rating below 0 throws exception`() {
        val card = createTestCard()
        SpacedRepetitionScheduler.updateCard(card, quality = -1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `quality rating above 5 throws exception`() {
        val card = createTestCard()
        SpacedRepetitionScheduler.updateCard(card, quality = 6)
    }

    // Helper function to create test cards
    private fun createTestCard(
        repetitionCount: Int = 0,
        intervalDays: Int = 0,
        easeFactor: Double = 2.5,
        nextReviewTimestamp: Long = System.currentTimeMillis()
    ): Flashcard {
        return Flashcard(
            id = 1,
            deckId = 1,
            subject = "Test",
            question = "Test Question",
            correctAnswer = "A",
            optionA = "A",
            optionB = "B",
            optionC = "C",
            optionD = "D",
            difficulty = "MEDIUM",
            repetitionCount = repetitionCount,
            intervalDays = intervalDays,
            easeFactor = easeFactor,
            nextReviewTimestamp = nextReviewTimestamp
        )
    }
}

