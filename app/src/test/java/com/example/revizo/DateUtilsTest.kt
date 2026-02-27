package com.example.revizo

import com.example.revizo.util.DateUtils
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class DateUtilsTest {

    @Test
    fun `isSameDay returns true for same calendar day`() {
        val cal1 = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 15, 10, 30)
        }
        val cal2 = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 15, 18, 45)
        }

        assertTrue(DateUtils.isSameDay(cal1.timeInMillis, cal2.timeInMillis))
    }

    @Test
    fun `isSameDay returns false for different days`() {
        val cal1 = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 15, 23, 59)
        }
        val cal2 = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 16, 0, 1)
        }

        assertFalse(DateUtils.isSameDay(cal1.timeInMillis, cal2.timeInMillis))
    }

    @Test
    fun `isToday returns true for current timestamp`() {
        val now = System.currentTimeMillis()
        assertTrue(DateUtils.isToday(now))
    }

    @Test
    fun `getStartOfDay returns midnight timestamp`() {
        val cal = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 15, 14, 30, 45)
            set(Calendar.MILLISECOND, 500)
        }

        val startOfDay = DateUtils.getStartOfDay(cal.timeInMillis)
        val resultCal = Calendar.getInstance().apply {
            timeInMillis = startOfDay
        }

        assertEquals(0, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultCal.get(Calendar.MINUTE))
        assertEquals(0, resultCal.get(Calendar.SECOND))
        assertEquals(0, resultCal.get(Calendar.MILLISECOND))
    }

    @Test
    fun `getEndOfDay returns last millisecond of day`() {
        val cal = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 15, 10, 30)
        }

        val endOfDay = DateUtils.getEndOfDay(cal.timeInMillis)
        val resultCal = Calendar.getInstance().apply {
            timeInMillis = endOfDay
        }

        assertEquals(23, resultCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, resultCal.get(Calendar.MINUTE))
        assertEquals(59, resultCal.get(Calendar.SECOND))
        assertEquals(999, resultCal.get(Calendar.MILLISECOND))
    }

    @Test
    fun `getDaysAgo returns correct timestamp`() {
        val now = System.currentTimeMillis()
        val threeDaysAgo = DateUtils.getDaysAgo(3)

        val diff = now - threeDaysAgo
        val daysDiff = diff / (24 * 60 * 60 * 1000)

        assertTrue("Should be approximately 3 days ago", daysDiff in 2..4)
    }

    @Test
    fun `formatDate returns readable date string`() {
        val cal = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 15)
        }

        val formatted = DateUtils.formatDate(cal.timeInMillis)

        assertTrue(formatted.contains("Jan"))
        assertTrue(formatted.contains("15"))
        assertTrue(formatted.contains("2024"))
    }

    @Test
    fun `formatTimeAgo returns 'Just now' for recent times`() {
        val now = System.currentTimeMillis()
        assertEquals("Just now", DateUtils.formatTimeAgo(now - 30000)) // 30 seconds ago
    }

    @Test
    fun `formatTimeAgo returns minutes for recent past`() {
        val thirtyMinutesAgo = System.currentTimeMillis() - (30 * 60 * 1000)
        val formatted = DateUtils.formatTimeAgo(thirtyMinutesAgo)

        assertTrue(formatted.contains("min"))
    }

    @Test
    fun `formatTimeAgo returns hours for same day`() {
        val threeHoursAgo = System.currentTimeMillis() - (3 * 60 * 60 * 1000)
        val formatted = DateUtils.formatTimeAgo(threeHoursAgo)

        assertTrue(formatted.contains("hour"))
    }

    @Test
    fun `formatTimeAgo returns days for recent week`() {
        val threeDaysAgo = System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000)
        val formatted = DateUtils.formatTimeAgo(threeDaysAgo)

        assertTrue(formatted.contains("day"))
    }

    @Test
    fun `daysBetween calculates correct difference`() {
        val start = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 1, 0, 0, 0)
        }.timeInMillis

        val end = Calendar.getInstance().apply {
            set(2024, Calendar.JANUARY, 11, 0, 0, 0)
        }.timeInMillis

        val days = DateUtils.daysBetween(start, end)

        assertEquals(10, days)
    }

    @Test
    fun `daysBetween handles same day`() {
        val start = System.currentTimeMillis()
        val end = start + (2 * 60 * 60 * 1000) // 2 hours later

        val days = DateUtils.daysBetween(start, end)

        assertEquals(0, days)
    }
}

