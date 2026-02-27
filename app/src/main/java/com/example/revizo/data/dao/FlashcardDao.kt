package com.example.revizo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.revizo.data.entity.Flashcard
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcard(flashcard: Flashcard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcards(flashcards: List<Flashcard>)

    @Query("SELECT * FROM flashcards ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomFlashcards(limit: Int = 10): List<Flashcard>

    @Query("SELECT * FROM flashcards WHERE id = :id")
    suspend fun getFlashcardById(id: Int): Flashcard?

    @Update
    suspend fun updateFlashcard(flashcard: Flashcard)

    @Query("UPDATE flashcards SET correctCount = correctCount + 1, lastSeenTimestamp = :timestamp WHERE id = :id")
    suspend fun incrementCorrectCount(id: Int, timestamp: Long)

    @Query("UPDATE flashcards SET wrongCount = wrongCount + 1, lastSeenTimestamp = :timestamp WHERE id = :id")
    suspend fun incrementWrongCount(id: Int, timestamp: Long)

    @Query("SELECT COUNT(*) FROM flashcards")
    suspend fun getFlashcardCount(): Int

    @Query("SELECT * FROM flashcards")
    fun getAllFlashcardsLive(): LiveData<List<Flashcard>>

    @Query("SELECT * FROM flashcards")
    fun getAllFlashcardsFlow(): Flow<List<Flashcard>>

    @Query("DELETE FROM flashcards")
    suspend fun deleteAllFlashcards()

    // SM-2 Spaced Repetition Queries
    @Query("""
        SELECT * FROM flashcards 
        WHERE nextReviewTimestamp <= :currentTime 
        ORDER BY nextReviewTimestamp ASC 
        LIMIT :limit
    """)
    suspend fun getDueFlashcards(currentTime: Long, limit: Int): List<Flashcard>

    @Query("""
        SELECT * FROM flashcards 
        WHERE deckId = :deckId AND nextReviewTimestamp <= :currentTime 
        ORDER BY nextReviewTimestamp ASC 
        LIMIT :limit
    """)
    suspend fun getDueFlashcardsByDeck(deckId: Int, currentTime: Long, limit: Int): List<Flashcard>

    @Query("SELECT COUNT(*) FROM flashcards WHERE nextReviewTimestamp <= :currentTime")
    suspend fun getDueCardCount(currentTime: Long): Int

    @Query("SELECT COUNT(*) FROM flashcards WHERE deckId = :deckId AND nextReviewTimestamp <= :currentTime")
    suspend fun getDueCardCountByDeck(deckId: Int, currentTime: Long): Int

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId ORDER BY RANDOM() LIMIT :limit")
    suspend fun getFlashcardsByDeck(deckId: Int, limit: Int): List<Flashcard>

    @Query("SELECT * FROM flashcards WHERE deckId = :deckId")
    fun getFlashcardsByDeckFlow(deckId: Int): Flow<List<Flashcard>>

    @Query("DELETE FROM flashcards WHERE deckId = :deckId")
    suspend fun deleteFlashcardsByDeck(deckId: Int)
}
