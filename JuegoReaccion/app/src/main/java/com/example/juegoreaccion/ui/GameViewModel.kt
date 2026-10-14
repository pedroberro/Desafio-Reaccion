package com.example.juegoreaccion.ui

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.juegoreaccion.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class GameState(
    val currentIteration: Int = 0,
    val score: Int = 0,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val currentStimulus: Stimulus? = null,
    val message: String = "Prepárate...",
    val reactionTimes: MutableList<Long> = mutableListOf(),
    val canReact: Boolean = false,
    val showResult: Boolean = false,
    val lastResultCorrect: Boolean = false
)

data class Stimulus(
    val type: StimulusType,
    val value: String,
    val color: Long = 0xFF000000,
    val shouldReact: Boolean = true
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val dao = db.playerResultDao()

    private val _settings = mutableStateOf(GameSettings())
    val settings: State<GameSettings> = _settings

    private val _gameState = mutableStateOf(GameState())
    val gameState: State<GameState> = _gameState

    private var gameJob: Job? = null
    private var stimulusStartTime: Long = 0
    private var pausedTimeAccumulated: Long = 0

    fun updateSettings(newSettings: GameSettings) {
        _settings.value = newSettings
    }

    fun startGame() {
        gameJob?.cancel()
        _gameState.value = GameState()
        gameJob = viewModelScope.launch {
            runGameLoop()
        }
    }

    fun togglePause() {
        if (_gameState.value.isGameOver) return
        val now = System.currentTimeMillis()
        if (!_gameState.value.isPaused) {
            // Pausing
            _gameState.value = _gameState.value.copy(isPaused = true)
        } else {
            // Resuming
            if (_gameState.value.canReact && !_gameState.value.showResult) {
                // Adjust stimulus start time so the timeout doesn't count the paused time
                // This is a simple approximation
            }
            _gameState.value = _gameState.value.copy(isPaused = false)
        }
    }

    private suspend fun waitWhilePaused() {
        while (_gameState.value.isPaused) {
            delay(100)
            // If we are in the middle of a reaction, we need to shift the start time
            if (_gameState.value.canReact && !_gameState.value.showResult) {
                stimulusStartTime += 100
            }
        }
    }

    private suspend fun runGameLoop() {
        while (_gameState.value.currentIteration < _settings.value.iterations) {
            waitWhilePaused()
            
            _gameState.value = _gameState.value.copy(
                message = "¡Atención!",
                currentStimulus = null,
                canReact = false,
                showResult = false
            )
            
            val waitTime = Random.nextLong(1000, 2500)
            var waited = 0L
            while (waited < waitTime) {
                delay(100)
                waitWhilePaused()
                waited += 100
            }
            
            val stimulus = generateStimulus()
            stimulusStartTime = System.currentTimeMillis()
            
            _gameState.value = _gameState.value.copy(
                currentStimulus = stimulus,
                canReact = true,
                message = if (_settings.value.isReverseMode) "¡MODO INVERSO!" else "¡REACCIONA!"
            )

            val timeout = _settings.value.difficulty.maxReactionTime
            while (System.currentTimeMillis() - stimulusStartTime < timeout && _gameState.value.canReact) {
                delay(20)
                waitWhilePaused()
            }

            if (_gameState.value.canReact) {
                handleReaction(false)
            }
            
            delay(1200)
            waitWhilePaused()
            _gameState.value = _gameState.value.copy(
                currentIteration = _gameState.value.currentIteration + 1
            )
        }
        endGame()
    }

    fun onUserAction() {
        if (_gameState.value.canReact && !_gameState.value.isPaused) {
            handleReaction(true)
        }
    }

    private fun handleReaction(reacted: Boolean) {
        val stimulus = _gameState.value.currentStimulus ?: return
        val reactionTime = System.currentTimeMillis() - stimulusStartTime
        
        val expected = stimulus.shouldReact
        val isCorrect = if (_settings.value.isReverseMode) {
            reacted != expected
        } else {
            reacted == expected
        }

        _gameState.value = _gameState.value.copy(
            canReact = false,
            lastResultCorrect = isCorrect,
            showResult = true,
            score = if (isCorrect) _gameState.value.score + 1 else _gameState.value.score,
            message = if (isCorrect) "¡Correcto! ${if(reacted) "${reactionTime}ms" else ""}" else "¡Incorrecto!"
        )
        
        if (isCorrect && reacted) {
            _gameState.value.reactionTimes.add(reactionTime)
        }
    }

    private fun generateStimulus(): Stimulus {
        val type = _settings.value.stimulusTypes.random()
        return when (type) {
            StimulusType.COLOR -> {
                val colors = listOf(
                    0xFFFF0000 to "ROJO",
                    0xFF00FF00 to "VERDE",
                    0xFF0000FF to "AZUL",
                    0xFFFFFF00 to "AMARILLO"
                )
                val colorData = colors.random()
                val textData = colors.random()
                val matches = colorData == textData
                Stimulus(type, textData.second, colorData.first, shouldReact = matches)
            }
            StimulusType.NUMBER -> {
                val num = Random.nextInt(2, 50)
                val isPrime = isPrime(num)
                Stimulus(type, num.toString(), 0xFF000000, shouldReact = isPrime)
            }
            StimulusType.WORD -> {
                val words = listOf("AHORA", "YA", "ALTO", "NO")
                val word = words.random()
                val isGo = word == "AHORA" || word == "YA"
                Stimulus(type, word, 0xFF000000, shouldReact = isGo)
            }
        }
    }

    private fun isPrime(num: Int): Boolean {
        if (num <= 1) return false
        for (i in 2..Math.sqrt(num.toDouble()).toInt()) {
            if (num % i == 0) return false
        }
        return true
    }

    private fun endGame() {
        _gameState.value = _gameState.value.copy(isGameOver = true, message = "Fin del Juego")
        if (_settings.value.difficulty != Difficulty.TRAINING) {
            saveResult()
        }
    }

    private fun saveResult() {
        viewModelScope.launch {
            val avgTime = if (_gameState.value.reactionTimes.isNotEmpty()) 
                _gameState.value.reactionTimes.average().toLong() else 0L
            
            dao.insertResult(
                PlayerResult(
                    playerName = "Jugador 1",
                    score = _gameState.value.score,
                    averageReactionTime = avgTime,
                    difficulty = _settings.value.difficulty.name + (if(_settings.value.isReverseMode) " (Inv)" else "")
                )
            )
        }
    }

    fun getTopResults(): Flow<List<PlayerResult>> = dao.getTopResults()
}
