package com.example.revizo.data.dao

import androidx.room.*
import com.example.revizo.data.entity.BattleHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface BattleHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBattleHistory(history: BattleHistory): Long

    @Query("SELECT * FROM battle_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentBattlesFlow(limit: Int = 30): Flow<List<BattleHistory>>

    @Query("SELECT * FROM battle_history WHERE timestamp >= :startTime ORDER BY timestamp ASC")
    suspend fun getBattlesSince(startTime: Long): List<BattleHistory>

    @Query("SELECT * FROM battle_history WHERE isDailyChallenge = 1 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastDailyChallenge(): BattleHistory?

    @Query("SELECT AVG(averageResponseTime) FROM battle_history WHERE timestamp >= :startTime")
    suspend fun getAverageResponseTimeSince(startTime: Long): Long?

    @Query("DELETE FROM battle_history")
    suspend fun deleteAllHistory()

    @Query("""
        SELECT COUNT(*) FROM battle_history 
        WHERE timestamp >= :startTime 
        AND timestamp < :endTime
    """)
    suspend fun getBattleCountBetween(startTime: Long, endTime: Long): Int
}

