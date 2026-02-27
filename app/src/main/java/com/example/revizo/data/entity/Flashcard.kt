package com.example.revizo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val deckId: Int = 0, // Reference to deck
    val subject: String,
    val question: String,
    val correctAnswer: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val difficulty: String, // "EASY", "MEDIUM", "HARD"

    // Statistics
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val lastSeenTimestamp: Long = 0L,

    // SM-2 Spaced Repetition Fields
    val easeFactor: Double = 2.5, // Default ease factor
    val repetitionCount: Int = 0, // Number of successful reviews
    val intervalDays: Int = 0, // Days until next review
    val nextReviewTimestamp: Long = System.currentTimeMillis(), // When card is due
    val lastReviewedTimestamp: Long = 0L // Last review time
)
