package com.example.revizo.data.dao

import androidx.room.*
import com.example.revizo.data.entity.FlashcardTagCrossRef

@Dao
interface FlashcardTagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcardTag(crossRef: FlashcardTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlashcardTags(crossRefs: List<FlashcardTagCrossRef>)

    @Delete
    suspend fun deleteFlashcardTag(crossRef: FlashcardTagCrossRef)

    @Query("DELETE FROM flashcard_tag_cross_ref WHERE flashcardId = :flashcardId")
    suspend fun deleteAllTagsForFlashcard(flashcardId: Int)

    @Query("SELECT tagId FROM flashcard_tag_cross_ref WHERE flashcardId = :flashcardId")
    suspend fun getTagIdsForFlashcard(flashcardId: Int): List<Int>

    @Query("SELECT flashcardId FROM flashcard_tag_cross_ref WHERE tagId = :tagId")
    suspend fun getFlashcardIdsForTag(tagId: Int): List<Int>
}

