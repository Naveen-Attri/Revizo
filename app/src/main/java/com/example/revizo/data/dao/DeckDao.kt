package com.example.revizo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.revizo.data.entity.Deck
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: Deck): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecks(decks: List<Deck>)

    @Update
    suspend fun updateDeck(deck: Deck)

    @Delete
    suspend fun deleteDeck(deck: Deck)

    @Query("SELECT * FROM decks ORDER BY name ASC")
    fun getAllDecksFlow(): Flow<List<Deck>>

    @Query("SELECT * FROM decks ORDER BY name ASC")
    fun getAllDecksLive(): LiveData<List<Deck>>

    @Query("SELECT * FROM decks WHERE id = :id")
    suspend fun getDeckById(id: Int): Deck?

    @Query("SELECT COUNT(*) FROM decks")
    suspend fun getDeckCount(): Int

    @Query("DELETE FROM decks WHERE id = :id")
    suspend fun deleteDeckById(id: Int)
}

