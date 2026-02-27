package com.example.revizo

import com.example.revizo.util.XPCalculator
import org.junit.Assert.*
import org.junit.Test

class XPCalculatorTest {

    @Test
    fun `calculateXP returns correct values for difficulties`() {
        assertEquals(10, XPCalculator.calculateXP("EASY"))
        assertEquals(20, XPCalculator.calculateXP("MEDIUM"))
        assertEquals(40, XPCalculator.calculateXP("HARD"))
    }

    @Test
    fun `calculateXP defaults to medium for unknown difficulty`() {
        assertEquals(20, XPCalculator.calculateXP("UNKNOWN"))
        assertEquals(20, XPCalculator.calculateXP(""))
    }

    @Test
    fun `calculateXP doubles for boss rounds`() {
        assertEquals(20, XPCalculator.calculateXP("EASY", isBossRound = true))
        assertEquals(40, XPCalculator.calculateXP("MEDIUM", isBossRound = true))
        assertEquals(80, XPCalculator.calculateXP("HARD", isBossRound = true))
    }

    @Test
    fun `calculateStreakBonus returns correct values`() {
        assertEquals(0, XPCalculator.calculateStreakBonus(0))
        assertEquals(0, XPCalculator.calculateStreakBonus(1))
        assertEquals(0, XPCalculator.calculateStreakBonus(2))
        assertEquals(10, XPCalculator.calculateStreakBonus(3))
        assertEquals(10, XPCalculator.calculateStreakBonus(4))
        assertEquals(25, XPCalculator.calculateStreakBonus(5))
        assertEquals(25, XPCalculator.calculateStreakBonus(10))
    }

    @Test
    fun `calculateLevel returns correct levels`() {
        assertEquals(1, XPCalculator.calculateLevel(0))
        assertEquals(1, XPCalculator.calculateLevel(499))
        assertEquals(2, XPCalculator.calculateLevel(500))
        assertEquals(2, XPCalculator.calculateLevel(999))
        assertEquals(3, XPCalculator.calculateLevel(1000))
        assertEquals(6, XPCalculator.calculateLevel(2500))
    }

    @Test
    fun `calculateLevelProgress returns correct progress`() {
        assertEquals(0f, XPCalculator.calculateLevelProgress(0), 0.01f)
        assertEquals(0.5f, XPCalculator.calculateLevelProgress(250), 0.01f)
        assertEquals(0.998f, XPCalculator.calculateLevelProgress(499), 0.01f)
        assertEquals(0f, XPCalculator.calculateLevelProgress(500), 0.01f) // New level
        assertEquals(0.2f, XPCalculator.calculateLevelProgress(600), 0.01f)
    }

    @Test
    fun `calculateXPForNextLevel returns correct values`() {
        assertEquals(500, XPCalculator.calculateXPForNextLevel(0))
        assertEquals(250, XPCalculator.calculateXPForNextLevel(250))
        assertEquals(1, XPCalculator.calculateXPForNextLevel(499))
        assertEquals(500, XPCalculator.calculateXPForNextLevel(500))
        assertEquals(400, XPCalculator.calculateXPForNextLevel(600))
    }

    @Test
    fun `didLevelUp detects level changes correctly`() {
        assertTrue(XPCalculator.didLevelUp(450, 550))
        assertTrue(XPCalculator.didLevelUp(999, 1000))
        assertFalse(XPCalculator.didLevelUp(400, 499))
        assertFalse(XPCalculator.didLevelUp(500, 999))
        assertTrue(XPCalculator.didLevelUp(0, 500))
    }

    @Test
    fun `didLevelUp handles multiple level jumps`() {
        // Jump from level 1 to level 3
        assertTrue(XPCalculator.didLevelUp(400, 1100))

        // Jump from level 2 to level 5
        assertTrue(XPCalculator.didLevelUp(700, 2200))
    }

    @Test
    fun `calculateAccuracy returns correct percentage`() {
        assertEquals(100, XPCalculator.calculateAccuracy(10, 0))
        assertEquals(0, XPCalculator.calculateAccuracy(0, 10))
        assertEquals(50, XPCalculator.calculateAccuracy(5, 5))
        assertEquals(75, XPCalculator.calculateAccuracy(75, 25))
        assertEquals(33, XPCalculator.calculateAccuracy(1, 2))
    }

    @Test
    fun `calculateAccuracy handles zero total`() {
        assertEquals(0, XPCalculator.calculateAccuracy(0, 0))
    }

    @Test
    fun `XP constants have expected values`() {
        assertEquals(500, XPCalculator.XP_PER_LEVEL)
        assertEquals(10, XPCalculator.XP_EASY)
        assertEquals(20, XPCalculator.XP_MEDIUM)
        assertEquals(40, XPCalculator.XP_HARD)
        assertEquals(10, XPCalculator.XP_STREAK_BONUS_3)
        assertEquals(25, XPCalculator.XP_STREAK_BONUS_5)
        assertEquals(100, XPCalculator.XP_DAILY_CHALLENGE)
        assertEquals(2, XPCalculator.XP_BOSS_MULTIPLIER)
    }
}

