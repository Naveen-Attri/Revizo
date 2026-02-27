package com.example.revizo.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.revizo.data.database.AppDatabase
import com.example.revizo.data.entity.UserStats
import com.example.revizo.data.repository.GameRepository

class StatsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    val userStats: LiveData<UserStats?>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = GameRepository(database.flashcardDao(), database.userStatsDao())
        userStats = repository.getUserStatsLive()
    }

    fun getAccuracy(correct: Int, incorrect: Int): Float {
        val total = correct + incorrect
        return if (total > 0) {
            (correct.toFloat() / total.toFloat()) * 100
        } else {
            0f
        }
    }

    fun getLevel(totalXp: Int): Int = repository.calculateLevel(totalXp)

    fun getXpForNextLevel(currentLevel: Int): Int = repository.getXpForNextLevel(currentLevel)

    fun getXpProgressInLevel(totalXp: Int): Int = repository.getXpProgressInLevel(totalXp)
}

