package com.example.revizo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.revizo.data.entity.UserStats
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(userStats: UserStats)

    @Update
    suspend fun updateUserStats(userStats: UserStats)

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStats(): UserStats?

    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStatsLive(): LiveData<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStatsFlow(): Flow<UserStats?>

    @Query("UPDATE user_stats SET totalXp = totalXp + :xp WHERE id = 1")
    suspend fun addXp(xp: Int)

    @Query("UPDATE user_stats SET currentLevel = :level WHERE id = 1")
    suspend fun updateLevel(level: Int)

    @Query("UPDATE user_stats SET totalCorrect = totalCorrect + 1 WHERE id = 1")
    suspend fun incrementCorrect()

    @Query("UPDATE user_stats SET totalIncorrect = totalIncorrect + 1 WHERE id = 1")
    suspend fun incrementIncorrect()

    @Query("UPDATE user_stats SET totalBattles = totalBattles + 1 WHERE id = 1")
    suspend fun incrementBattles()

    @Query("UPDATE user_stats SET dailyStreak = :streak WHERE id = 1")
    suspend fun updateStreak(streak: Int)

    @Query("UPDATE user_stats SET lastDailyChallengeDate = :date WHERE id = 1")
    suspend fun updateLastDailyChallengeDate(date: Long)

    @Query("UPDATE user_stats SET dailyChallengeStreak = :streak WHERE id = 1")
    suspend fun updateDailyChallengeStreak(streak: Int)

    @Query("UPDATE user_stats SET battleLength = :length WHERE id = 1")
    suspend fun updateBattleLength(length: Int)

    @Query("UPDATE user_stats SET notificationsEnabled = :enabled WHERE id = 1")
    suspend fun updateNotificationsEnabled(enabled: Boolean)

    @Query("UPDATE user_stats SET dailyTarget = :target WHERE id = 1")
    suspend fun updateDailyTarget(target: Int)

    @Query("UPDATE user_stats SET totalResponseTime = totalResponseTime + :time, totalAnswers = totalAnswers + 1 WHERE id = 1")
    suspend fun addResponseTime(time: Long)

    @Query("UPDATE user_stats SET lastPlayedDate = :date WHERE id = 1")
    suspend fun updateLastPlayedDate(date: Long)
}
