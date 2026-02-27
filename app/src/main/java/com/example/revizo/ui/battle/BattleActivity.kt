package com.example.revizo.ui.battle

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.example.revizo.R

class BattleActivity : AppCompatActivity() {

    private lateinit var viewModel: BattleViewModel

    private lateinit var tvQuestionProgress: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvDifficulty: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var btnOptionA: CardView
    private lateinit var btnOptionB: CardView
    private lateinit var btnOptionC: CardView
    private lateinit var btnOptionD: CardView
    private lateinit var tvOptionA: TextView
    private lateinit var tvOptionB: TextView
    private lateinit var tvOptionC: TextView
    private lateinit var tvOptionD: TextView
    private lateinit var cardFeedback: CardView
    private lateinit var tvFeedback: TextView
    private lateinit var btnNext: CardView
    private lateinit var tvNext: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle)

        initViews()
        setupViewModel()
        setupClickListeners()
    }

    private fun initViews() {
        tvQuestionProgress = findViewById(R.id.tvQuestionProgress)
        tvTimer = findViewById(R.id.tvTimer)
        tvDifficulty = findViewById(R.id.tvDifficulty)
        tvQuestion = findViewById(R.id.tvQuestion)
        btnOptionA = findViewById(R.id.btnOptionA)
        btnOptionB = findViewById(R.id.btnOptionB)
        btnOptionC = findViewById(R.id.btnOptionC)
        btnOptionD = findViewById(R.id.btnOptionD)
        tvOptionA = findViewById(R.id.tvOptionA)
        tvOptionB = findViewById(R.id.tvOptionB)
        tvOptionC = findViewById(R.id.tvOptionC)
        tvOptionD = findViewById(R.id.tvOptionD)
        cardFeedback = findViewById(R.id.cardFeedback)
        tvFeedback = findViewById(R.id.tvFeedback)
        btnNext = findViewById(R.id.btnNext)
        tvNext = findViewById(R.id.tvNext)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[BattleViewModel::class.java]

        viewModel.battleState.observe(this) { state ->
            updateUI(state)

            if (state.isBattleComplete) {
                navigateToResult(state)
            }
        }
    }

    private fun updateUI(state: BattleState) {
        // Update question progress
        tvQuestionProgress.text = getString(R.string.question_progress, state.currentQuestionIndex + 1, 10)

        // Update timer
        tvTimer.text = getString(R.string.time_remaining, state.timeRemaining)
        tvTimer.setTextColor(if (state.timeRemaining <= 5) Color.RED else getColor(R.color.warning_orange))

        // Update question
        state.currentFlashcard?.let { flashcard ->
            tvQuestion.text = flashcard.question
            tvDifficulty.text = flashcard.difficulty

            // Set difficulty color
            val difficultyColor = when (flashcard.difficulty) {
                "EASY" -> getColor(R.color.easy_color)
                "MEDIUM" -> getColor(R.color.medium_color)
                "HARD" -> getColor(R.color.hard_color)
                else -> getColor(R.color.medium_color)
            }
            tvDifficulty.setBackgroundColor(difficultyColor)

            // Update options
            tvOptionA.text = "A. ${flashcard.optionA}"
            tvOptionB.text = "B. ${flashcard.optionB}"
            tvOptionC.text = "C. ${flashcard.optionC}"
            tvOptionD.text = "D. ${flashcard.optionD}"
        }

        // Show feedback if answered
        if (state.isAnswered) {
            cardFeedback.visibility = View.VISIBLE
            btnNext.visibility = View.VISIBLE

            // Set feedback color and text
            if (state.isCorrect) {
                cardFeedback.setCardBackgroundColor(getColor(R.color.correct_green))
                tvFeedback.text = getString(R.string.correct, state.earnedXp)
            } else {
                cardFeedback.setCardBackgroundColor(getColor(R.color.incorrect_red))
                tvFeedback.text = getString(R.string.incorrect)
            }

            // Highlight selected and correct answers
            highlightAnswers(state)

            // Disable option clicks
            disableOptions()

            // Update next button text
            tvNext.text = if (state.currentQuestionIndex >= 9) {
                getString(R.string.finish)
            } else {
                getString(R.string.next)
            }
        } else {
            cardFeedback.visibility = View.GONE
            btnNext.visibility = View.GONE
            resetOptions()
            enableOptions()
        }
    }

    private fun highlightAnswers(state: BattleState) {
        val correctAnswer = state.currentFlashcard?.correctAnswer ?: return
        val selectedOption = state.selectedOption

        // Reset all colors first
        resetOptions()

        // Highlight correct answer
        when (correctAnswer) {
            state.currentFlashcard.optionA -> btnOptionA.setCardBackgroundColor(getColor(R.color.correct_green))
            state.currentFlashcard.optionB -> btnOptionB.setCardBackgroundColor(getColor(R.color.correct_green))
            state.currentFlashcard.optionC -> btnOptionC.setCardBackgroundColor(getColor(R.color.correct_green))
            state.currentFlashcard.optionD -> btnOptionD.setCardBackgroundColor(getColor(R.color.correct_green))
        }

        // Highlight wrong answer if different from correct
        if (selectedOption != correctAnswer && !selectedOption.isNullOrEmpty()) {
            when (selectedOption) {
                state.currentFlashcard.optionA -> btnOptionA.setCardBackgroundColor(getColor(R.color.incorrect_red))
                state.currentFlashcard.optionB -> btnOptionB.setCardBackgroundColor(getColor(R.color.incorrect_red))
                state.currentFlashcard.optionC -> btnOptionC.setCardBackgroundColor(getColor(R.color.incorrect_red))
                state.currentFlashcard.optionD -> btnOptionD.setCardBackgroundColor(getColor(R.color.incorrect_red))
            }
        }
    }

    private fun resetOptions() {
        val defaultColor = getColor(R.color.surface_dark)
        btnOptionA.setCardBackgroundColor(defaultColor)
        btnOptionB.setCardBackgroundColor(defaultColor)
        btnOptionC.setCardBackgroundColor(defaultColor)
        btnOptionD.setCardBackgroundColor(defaultColor)
    }

    private fun enableOptions() {
        btnOptionA.isClickable = true
        btnOptionB.isClickable = true
        btnOptionC.isClickable = true
        btnOptionD.isClickable = true
    }

    private fun disableOptions() {
        btnOptionA.isClickable = false
        btnOptionB.isClickable = false
        btnOptionC.isClickable = false
        btnOptionD.isClickable = false
    }

    private fun setupClickListeners() {
        btnOptionA.setOnClickListener {
            val flashcard = viewModel.battleState.value?.currentFlashcard
            flashcard?.let { viewModel.submitAnswer(it.optionA) }
        }

        btnOptionB.setOnClickListener {
            val flashcard = viewModel.battleState.value?.currentFlashcard
            flashcard?.let { viewModel.submitAnswer(it.optionB) }
        }

        btnOptionC.setOnClickListener {
            val flashcard = viewModel.battleState.value?.currentFlashcard
            flashcard?.let { viewModel.submitAnswer(it.optionC) }
        }

        btnOptionD.setOnClickListener {
            val flashcard = viewModel.battleState.value?.currentFlashcard
            flashcard?.let { viewModel.submitAnswer(it.optionD) }
        }

        btnNext.setOnClickListener {
            viewModel.nextQuestion()
        }
    }

    private fun navigateToResult(state: BattleState) {
        val intent = Intent(this, BattleResultActivity::class.java).apply {
            putExtra("TOTAL_XP", state.totalXp)
            putExtra("CORRECT_COUNT", state.correctCount)
            putExtra("INCORRECT_COUNT", state.incorrectCount)
            putExtra("STREAK_BONUS", state.streakBonus)
        }
        startActivity(intent)
        finish()
    }
}

