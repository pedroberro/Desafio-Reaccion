package com.example.juegoreaccion.data

enum class Difficulty(val maxReactionTime: Long) {
    TRAINING(10000L),
    EASY(5000L),
    MEDIUM(2500L),
    HARD(1200L)
}

enum class StimulusType {
    WORD, NUMBER, COLOR
}

data class GameSettings(
    val difficulty: Difficulty = Difficulty.EASY,
    val iterations: Int = 10,
    val isReverseMode: Boolean = false,
    val stimulusTypes: List<StimulusType> = listOf(StimulusType.WORD, StimulusType.NUMBER, StimulusType.COLOR)
)
