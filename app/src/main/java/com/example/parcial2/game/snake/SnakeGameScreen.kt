package com.example.parcial2.game.snake

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SnakeGameSection(vm: SnakeViewModel = viewModel()) {
    val state by vm.state.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🐍 Snake", style = MaterialTheme.typography.titleLarge)
                Column(horizontalAlignment = Alignment.End) {
                    Text("Score: ${state.score}", style = MaterialTheme.typography.bodyLarge)
                    Text("Best: ${state.bestScore}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(12.dp))

            // Game Board
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A2744))
                    .pointerInput(state.isRunning, state.isGameOver) {
                        detectTapGestures {
                            if (!state.isRunning || state.isGameOver) {
                                vm.startGame()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cellW = size.width  / BOARD_COLS
                    val cellH = size.height / BOARD_ROWS
                    val padding = 2f

                    // Draw grid (subtle)
                    for (x in 0 until BOARD_COLS) for (y in 0 until BOARD_ROWS) {
                        drawRect(
                            color    = Color(0xFF1E2D4E),
                            topLeft  = Offset(x * cellW + padding, y * cellH + padding),
                            size     = Size(cellW - padding * 2, cellH - padding * 2)
                        )
                    }
                    // Draw food
                    drawCircle(
                        color  = Color(0xFFFFD54F),
                        radius = (minOf(cellW, cellH) / 2f) - padding,
                        center = Offset(
                            state.food.x * cellW + cellW / 2,
                            state.food.y * cellH + cellH / 2
                        )
                    )
                    // Draw snake
                    state.snake.forEachIndexed { i, p ->
                        val color = if (i == 0) Color(0xFF4FC3F7) else Color(0xFF0288D1)
                        drawRoundRect(
                            color    = color,
                            topLeft  = Offset(p.x * cellW + padding, p.y * cellH + padding),
                            size     = Size(cellW - padding * 2, cellH - padding * 2),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
                        )
                    }
                }

                // Overlays
                if (!state.isRunning && !state.isGameOver) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tap Board to Start",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge)
                    }
                }
                if (state.isGameOver) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .background(Color(0xAA000000), RoundedCornerShape(16.dp))
                            .padding(24.dp)
                    ) {
                        Text("Game Over!", color = Color.White,
                            style = MaterialTheme.typography.headlineMedium)
                        Text("Score: ${state.score}", color = Color(0xFFFFD54F),
                            style = MaterialTheme.typography.titleLarge)
                        Text("Tap to Restart", color = Color.White,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // D-Pad controls
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                DPadButton(Icons.Default.KeyboardArrowUp) { vm.changeDirection(Direction.UP) }
                Row {
                    DPadButton(Icons.AutoMirrored.Filled.KeyboardArrowLeft) { vm.changeDirection(Direction.LEFT) }
                    Spacer(Modifier.width(8.dp))
                    // Play / Restart button in center
                    FilledIconButton(
                        onClick = { vm.startGame() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            if (state.isGameOver || !state.isRunning)
                                Icons.Default.PlayArrow else Icons.Default.Refresh,
                            contentDescription = "Play/Restart"
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    DPadButton(Icons.AutoMirrored.Filled.KeyboardArrowRight) { vm.changeDirection(Direction.RIGHT) }
                }
                DPadButton(Icons.Default.KeyboardArrowDown) { vm.changeDirection(Direction.DOWN) }
            }
        }
    }
}

@Composable
private fun DPadButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick  = onClick,
        modifier = Modifier.size(48.dp),
        shape    = CircleShape
    ) { Icon(icon, contentDescription = null) }
}
