package com.example.juegoreaccion.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerResultDao {
    @Insert
    suspend fun insertResult(result: PlayerResult)

    @Query("SELECT * FROM player_results ORDER BY score DESC, averageReactionTime ASC")
    fun getAllResults(): Flow<List<PlayerResult>>

    @Query("SELECT * FROM player_results ORDER BY score DESC LIMIT 10")
    fun getTopResults(): Flow<List<PlayerResult>>
}
