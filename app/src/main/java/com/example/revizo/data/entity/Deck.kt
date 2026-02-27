package com.example.revizo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class Deck(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val description: String = "",
    val colorHex: String = "#6200EE", // Material Purple
    val createdAt: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false
)

