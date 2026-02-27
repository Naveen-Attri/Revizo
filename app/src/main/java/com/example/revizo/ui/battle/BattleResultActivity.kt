package com.example.revizo.ui.battle

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.example.revizo.R
import com.example.revizo.data.database.AppDatabase
import com.example.revizo.data.repository.GameRepository
import com.example.revizo.ui.home.HomeActivity
import kotlinx.coroutines.launch

class BattleResultActivity : AppCompatActivity() {

    private lateinit var repository: GameRepository

    private lateinit var tvLevelUp: TextView
    private lateinit var tvTotalXp: TextView
    private lateinit var tvCorrect: TextView
    private lateinit var tvAccuracy: TextView
    private lateinit var layoutStreakBonus: View
    private lateinit var tvStreakBonus: TextView
    private lateinit var btnBackHome: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battle_result)

        val database = AppDatabase.getDatabase(this)
        repository = GameRepository(database.flashcardDao(), database.userStatsDao())

        initViews()
        loadResults()
        setupClickListeners()
        checkLevelUp()
        setupBackPressHandler()
    }

    private fun initViews() {
        tvLevelUp = findViewById(R.id.tvLevelUp)
        tvTotalXp = findViewById(R.id.tvTotalXp)
        tvCorrect = findViewById(R.id.tvCorrect)
        tvAccuracy = findViewById(R.id.tvAccuracy)
        layoutStreakBonus = findViewById(R.id.layoutStreakBonus)
        tvStreakBonus = findViewById(R.id.tvStreakBonus)
        btnBackHome = findViewById(R.id.btnBackHome)
    }

    private fun loadResults() {
        val totalXp = intent.getIntExtra("TOTAL_XP", 0)
        val correctCount = intent.getIntExtra("CORRECT_COUNT", 0)
        val incorrectCount = intent.getIntExtra("INCORRECT_COUNT", 0)
        val streakBonus = intent.getIntExtra("STREAK_BONUS", 0)

        tvTotalXp.text = getString(R.string.total_xp_earned, totalXp)
        tvCorrect.text = getString(R.string.correct_answers, correctCount)

        val accuracy = if (correctCount + incorrectCount > 0) {
            (correctCount.toFloat() / (correctCount + incorrectCount).toFloat() * 100).toInt()
        } else {
            0
        }
        tvAccuracy.text = getString(R.string.accuracy, accuracy)

        if (streakBonus > 0) {
            layoutStreakBonus.visibility = View.VISIBLE
            tvStreakBonus.text = getString(R.string.streak_bonus, streakBonus)
        } else {
            layoutStreakBonus.visibility = View.GONE
        }
    }

    private fun checkLevelUp() {
        lifecycleScope.launch {
            val stats = repository.getUserStats()
            stats?.let {
                val newLevel = repository.calculateLevel(it.totalXp)
                if (newLevel > it.currentLevel) {
                    tvLevelUp.visibility = View.VISIBLE
                } else {
                    tvLevelUp.visibility = View.GONE
                }
            }
        }
    }

    private fun setupClickListeners() {
        btnBackHome.setOnClickListener {
            navigateToHome()
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToHome()
            }
        })
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}

