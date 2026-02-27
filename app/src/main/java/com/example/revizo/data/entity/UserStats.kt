package com.example.revizo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey
    val id: Int = 1,
    val totalXp: Int = 0,
    val currentLevel: Int = 1,
    val dailyStreak: Int = 0,
    val lastPlayedDate: Long = 0L,
    val totalCorrect: Int = 0,
    val totalIncorrect: Int = 0,
    val totalBattles: Int = 0,

    // Daily Challenge
    val lastDailyChallengeDate: Long = 0L, // Calendar day of last completion
    val dailyChallengeStreak: Int = 0,

    // Settings & Preferences
    val dailyTarget: Int = 20, // Daily goal for cards reviewed
    val battleLength: Int = 10, // Default battle length
    val notificationsEnabled: Boolean = true,

    // Analytics
    val totalResponseTime: Long = 0L, // Total time spent answering (ms)
    val totalAnswers: Int = 0 // For calculating average response time
)
