package com.example.juegoreaccion.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_results")
data class PlayerResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val playerName: String,
    val score: Int,
    val averageReactionTime: Long,
    val difficulty: String,
    val timestamp: Long = System.currentTimeMillis()
)
