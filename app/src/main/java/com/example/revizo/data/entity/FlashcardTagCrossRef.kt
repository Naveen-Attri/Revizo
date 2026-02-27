package com.example.revizo.data.entity

import androidx.room.Entity

@Entity(primaryKeys = ["flashcardId", "tagId"], tableName = "flashcard_tag_cross_ref")
data class FlashcardTagCrossRef(
    val flashcardId: Int,
    val tagId: Int
)

