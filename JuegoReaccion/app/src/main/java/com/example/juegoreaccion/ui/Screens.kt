package com.example.juegoreaccion.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.juegoreaccion.data.Difficulty
import com.example.juegoreaccion.data.GameSettings
import com.example.juegoreaccion.data.PlayerResult
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainMenuScreen(
    settings: GameSettings,
    onStartGame: () -> Unit,
    onViewStats: () -> Unit,
    onSettingsChange: (GameSettings) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Juego de Reacción", fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))

        Text("Dificultad:", fontWeight = FontWeight.SemiBold)
        Difficulty.values().forEach { diff ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onSettingsChange(settings.copy(difficulty = diff)) }
            ) {
                RadioButton(
                    selected = settings.difficulty == diff,
                    onClick = { onSettingsChange(settings.copy(difficulty = diff)) }
                )
                Text(diff.name)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { onSettingsChange(settings.copy(isReverseMode = !settings.isReverseMode)) }
        ) {
            Checkbox(
                checked = settings.isReverseMode,
                onCheckedChange = { onSettingsChange(settings.copy(isReverseMode = it)) }
            )
            Text("Modo Reacción Inversa")
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onStartGame, modifier = Modifier.fillMaxWidth()) {
            Text("JUGAR")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onViewStats, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
            Text("VER ESTADÍSTICAS")
        }
    }
}

@Composable
fun InstructionsScreen(
    isReverseMode: Boolean,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Cómo Jugar", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        InstructionItem("Colores", "Toca la pantalla solo si el color de la fuente coincide con la palabra (ej: 'ROJO' escrito en rojo).")
        InstructionItem("Palabras", "Toca si aparece 'AHORA' o 'YA'. NO toques si aparece 'ALTO' o 'NO'.")
        InstructionItem("Números", "Toca solo si el número que aparece es un número PRIMO.")
        
        if (isReverseMode) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("¡MODO INVERSO ACTIVADO!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                    Text("Debes hacer exactamente lo contrario a lo indicado arriba para sumar puntos.", color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) {
            Text("¡ENTENDIDO, EMPEZAR!")
        }
        TextButton(onClick = onBack) {
            Text("Volver")
        }
    }
}

@Composable
fun InstructionItem(title: String, description: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(description, textAlign = TextAlign.Center)
    }
}

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onExit: () -> Unit
) {
    val state by viewModel.gameState
    val settings by viewModel.settings
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(enabled = state.canReact && !state.isPaused) { viewModel.onUserAction() }
            .background(if (state.showResult) (if (state.lastResultCorrect) Color.Green.copy(alpha = 0.2f) else Color.Red.copy(alpha = 0.2f)) else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        // Pause Button at Top Right
        IconButton(
            onClick = { viewModel.togglePause() },
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Pause, contentDescription = "Pausar")
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Iteración: ${state.currentIteration + 1} / ${settings.iterations}", fontSize = 14.sp)
            Text("Puntuación: ${state.score}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(64.dp))
            
            if (!state.isPaused) {
                state.currentStimulus?.let { stimulus ->
                    Text(
                        text = stimulus.value,
                        color = Color(stimulus.color),
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black
                    )
                } ?: Text(state.message, fontSize = 24.sp, color = Color.Gray)
            } else {
                Text("JUEGO EN PAUSA", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        // Pause Menu Dialog
        if (state.isPaused) {
            AlertDialog(
                onDismissRequest = { viewModel.togglePause() },
                title = { Text("Juego Pausado") },
                text = { Text("¿Qué deseas hacer?") },
                confirmButton = {
                    Button(onClick = { viewModel.togglePause() }) { Text("Reanudar") }
                },
                dismissButton = {
                    TextButton(onClick = onExit) { Text("Salir al Menú") }
                }
            )
        }

        if (state.isGameOver) {
            AlertDialog(
                onDismissRequest = { },
                title = { Text("¡Juego Terminado!") },
                text = { Text("Puntuación final: ${state.score}\nReacción media: ${if(state.reactionTimes.isNotEmpty()) state.reactionTimes.average().toInt().toString() + "ms" else "N/A"}") },
                confirmButton = {
                    Button(onClick = { viewModel.startGame() }) { Text("Reiniciar") }
                },
                dismissButton = {
                    Button(onClick = onExit) { Text("Menú Principal") }
                }
            )
        }
    }
}

@Composable
fun StatsScreen(
    results: List<PlayerResult>,
    onBack: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Mejores Resultados", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        if (results.isEmpty()) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text("No hay resultados aún", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(results) { result ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(result.playerName, fontWeight = FontWeight.Bold)
                                Text("${result.score} pts", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Text("Reacción: ${result.averageReactionTime}ms")
                            Text("Dificultad: ${result.difficulty}", fontSize = 12.sp)
                            Text("Fecha: ${dateFormat.format(Date(result.timestamp))}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
        
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
            Text("VOLVER")
        }
    }
}
