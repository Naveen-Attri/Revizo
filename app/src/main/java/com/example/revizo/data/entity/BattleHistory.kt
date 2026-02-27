package com.example.revizo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battle_history")
data class BattleHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val totalQuestions: Int,
    val correctAnswers: Int,
    val xpEarned: Int,
    val averageResponseTime: Long, // in milliseconds
    val deckId: Int? = null,
    val isDailyChallenge: Boolean = false
)

