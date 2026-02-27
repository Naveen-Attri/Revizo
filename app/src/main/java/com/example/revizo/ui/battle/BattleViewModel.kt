package com.example.revizo.ui.battle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.revizo.data.database.AppDatabase
import com.example.revizo.data.entity.Flashcard
import com.example.revizo.data.repository.GameRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class BattleState(
    val currentQuestionIndex: Int = 0,
    val currentFlashcard: Flashcard? = null,
    val timeRemaining: Int = 15,
    val isAnswered: Boolean = false,
    val selectedOption: String? = null,
    val isCorrect: Boolean = false,
    val earnedXp: Int = 0,
    val totalXp: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val consecutiveCorrect: Int = 0,
    val streakBonus: Int = 0,
    val isBattleComplete: Boolean = false
)

class BattleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GameRepository
    private val _battleState = MutableLiveData<BattleState>()
    val battleState: LiveData<BattleState> = _battleState

    private var flashcards: List<Flashcard> = emptyList()
    private var timerJob: kotlinx.coroutines.Job? = null

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
        loadFlashcards()
    }

    private fun loadFlashcards() {
        viewModelScope.launch {
            flashcards = repository.getRandomFlashcards(10)
            if (flashcards.isNotEmpty()) {
                _battleState.value = BattleState(
                    currentQuestionIndex = 0,
                    currentFlashcard = flashcards[0]
                )
                startTimer()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val currentState = _battleState.value ?: return@launch
            for (time in 15 downTo 0) {
                _battleState.value = currentState.copy(timeRemaining = time)
                delay(1000)

                // Auto move to next if time runs out
                if (time == 0 && !currentState.isAnswered) {
                    submitAnswer("")
                    break
                }
            }
        }
    }

    fun submitAnswer(selectedOption: String) {
        val currentState = _battleState.value ?: return
        val flashcard = currentState.currentFlashcard ?: return

        if (currentState.isAnswered) return

        timerJob?.cancel()

        val isCorrect = selectedOption == flashcard.correctAnswer
        val xp = if (isCorrect) repository.calculateXp(flashcard.difficulty) else 0
        val newConsecutiveCorrect = if (isCorrect) currentState.consecutiveCorrect + 1 else 0
        val streakBonus = repository.calculateStreakBonus(newConsecutiveCorrect)

        viewModelScope.launch {
            // Update flashcard stats
            repository.updateFlashcardStats(flashcard.id, isCorrect)

            // Update user stats
            if (isCorrect) {
                repository.incrementCorrect()
            } else {
                repository.incrementIncorrect()
            }
        }

        _battleState.value = currentState.copy(
            isAnswered = true,
            selectedOption = selectedOption,
            isCorrect = isCorrect,
            earnedXp = xp + streakBonus,
            totalXp = currentState.totalXp + xp + streakBonus,
            correctCount = if (isCorrect) currentState.correctCount + 1 else currentState.correctCount,
            incorrectCount = if (!isCorrect) currentState.incorrectCount + 1 else currentState.incorrectCount,
            consecutiveCorrect = newConsecutiveCorrect,
            streakBonus = currentState.streakBonus + streakBonus
        )
    }

    fun nextQuestion() {
        val currentState = _battleState.value ?: return
        val nextIndex = currentState.currentQuestionIndex + 1

        if (nextIndex >= flashcards.size) {
            // Battle complete
            completeBattle()
        } else {
            _battleState.value = BattleState(
                currentQuestionIndex = nextIndex,
                currentFlashcard = flashcards[nextIndex],
                totalXp = currentState.totalXp,
                correctCount = currentState.correctCount,
                incorrectCount = currentState.incorrectCount,
                consecutiveCorrect = currentState.consecutiveCorrect,
                streakBonus = currentState.streakBonus
            )
            startTimer()
        }
    }

    private fun completeBattle() {
        val currentState = _battleState.value ?: return

        viewModelScope.launch {
            // Complete battle with enhanced repository method
            repository.completeBattle(
                correctAnswers = currentState.correctCount,
                totalQuestions = flashcards.size,
                totalXP = currentState.totalXp,
                averageResponseTime = 0L, // TODO: Track properly
                deckId = null,
                isDailyChallenge = false
            )
        }

        _battleState.value = currentState.copy(isBattleComplete = true)
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

