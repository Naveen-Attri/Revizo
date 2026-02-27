package com.example.revizo.ui.stats

import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.ViewModelProvider
import com.example.revizo.R

class StatsActivity : AppCompatActivity() {

    private lateinit var viewModel: StatsViewModel

    private lateinit var tvLevel: TextView
    private lateinit var tvXpProgress: TextView
    private lateinit var progressXp: ProgressBar
    private lateinit var tvTotalXp: TextView
    private lateinit var tvTotalBattles: TextView
    private lateinit var tvCorrect: TextView
    private lateinit var tvIncorrect: TextView
    private lateinit var tvAccuracy: TextView
    private lateinit var btnBack: CardView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        initViews()
        setupViewModel()
        setupClickListeners()
    }

    private fun initViews() {
        tvLevel = findViewById(R.id.tvLevel)
        tvXpProgress = findViewById(R.id.tvXpProgress)
        progressXp = findViewById(R.id.progressXp)
        tvTotalXp = findViewById(R.id.tvTotalXp)
        tvTotalBattles = findViewById(R.id.tvTotalBattles)
        tvCorrect = findViewById(R.id.tvCorrect)
        tvIncorrect = findViewById(R.id.tvIncorrect)
        tvAccuracy = findViewById(R.id.tvAccuracy)
        btnBack = findViewById(R.id.btnBack)
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[StatsViewModel::class.java]

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

                // Update stats
                tvTotalXp.text = it.totalXp.toString()
                tvTotalBattles.text = it.totalBattles.toString()
                tvCorrect.text = it.totalCorrect.toString()
                tvIncorrect.text = it.totalIncorrect.toString()

                val accuracy = viewModel.getAccuracy(it.totalCorrect, it.totalIncorrect)
                tvAccuracy.text = getString(R.string.overall_accuracy, accuracy)
            }
        }
    }

    private fun setupClickListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }
}

