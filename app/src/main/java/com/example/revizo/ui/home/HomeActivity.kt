package com.example.revizo.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.example.revizo.R
import com.example.revizo.ui.battle.BattleActivity
import com.example.revizo.ui.stats.StatsActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var viewModel: HomeViewModel

    private lateinit var tvLevel: TextView
    private lateinit var tvXpProgress: TextView
    private lateinit var tvStreak: TextView
    private lateinit var progressXp: ProgressBar
    private lateinit var btnStartBattle: CardView
    private lateinit var btnViewStats: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initViews()
        setupViewModel()
        setupClickListeners()
    }

    private fun initViews() {
        tvLevel = findViewById(R.id.tvLevel)
        tvXpProgress = findViewById(R.id.tvXpProgress)
        tvStreak = findViewById(R.id.tvStreak)
        progressXp = findViewById(R.id.progressXp)
        btnStartBattle = findViewById(R.id.btnStartBattle)
        btnViewStats = findViewById(R.id.btnViewStats)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        viewModel.userStats.observe(this) { stats ->
            stats?.let {
                // Update level
                val level = viewModel.getLevel(it.totalXp)
                tvLevel.text = getString(R.string.level, level)

                // Update XP progress
                val xpInLevel = viewModel.getXpProgressInLevel(it.totalXp)
                val xpForNextLevel = viewModel.getXpForNextLevel(level)
                tvXpProgress.text = getString(R.string.xp_progress, xpInLevel, xpForNextLevel)

                progressXp.max = xpForNextLevel
                progressXp.progress = xpInLevel

                // Update streak
                tvStreak.text = getString(R.string.daily_streak, it.dailyStreak)
            }
        }
    }

    private fun setupClickListeners() {
        btnStartBattle.setOnClickListener {
            startActivity(Intent(this, BattleActivity::class.java))
        }

        btnViewStats.setOnClickListener {
            startActivity(Intent(this, StatsActivity::class.java))
        }
    }
}

