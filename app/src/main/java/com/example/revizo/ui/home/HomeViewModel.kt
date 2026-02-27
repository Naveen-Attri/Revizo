package com.example.revizo.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.revizo.data.database.AppDatabase
import com.example.revizo.data.entity.UserStats
import com.example.revizo.data.repository.GameRepository
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    val userStats: LiveData<UserStats?>

    private val _dueCardCount = MutableLiveData<Int>()
    val dueCardCount: LiveData<Int> = _dueCardCount

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(
            database.flashcardDao(),
            database.userStatsDao(),
            database.deckDao(),
            database.tagDao(),
            database.flashcardTagDao(),
            database.battleHistoryDao()
        )
        userStats = repository.getUserStatsLive()

        // Check and update streak on init
        viewModelScope.launch {
            repository.checkAndUpdateStreak()
            loadDueCardCount()
        }
    }

    private suspend fun loadDueCardCount() {
        val count = repository.getDueFlashcards(100)?.size ?: 0
        _dueCardCount.postValue(count)
    }

    fun refreshData() {
        viewModelScope.launch {
            loadDueCardCount()
        }
    }

    fun getLevel(totalXp: Int): Int = repository.calculateLevel(totalXp)

    fun getXpForNextLevel(currentLevel: Int): Int = repository.getXpForNextLevel(currentLevel)

    fun getXpProgressInLevel(totalXp: Int): Int = repository.getXpProgressInLevel(totalXp)
}
