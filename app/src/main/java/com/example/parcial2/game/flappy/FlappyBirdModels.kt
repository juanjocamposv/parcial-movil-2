package com.example.parcial2.game.flappy

import androidx.compose.ui.graphics.Color

data class Pipe(
    val x: Float,
    val gapY: Float,
    val gapHeight: Float = 280f,
    val width: Float = 120f,
    var passed: Boolean = false
) {
    companion object {
        // Colores mejorados para las tuberías
        val PIPE_MAIN_COLOR = Color(0xFF228B22) // Verde bosque
        val PIPE_BORDER_COLOR = Color(0xFF166B16) // Borde más oscuro
        val PIPE_CAP_COLOR = Color(0xFF1E7A1E) // Color del borde de la tubería
    }
}

data class FlappyBirdState(
    val birdY: Float = 500f,
    val birdVelocity: Float = 0f,
    val pipes: List<Pipe> = emptyList(),
    val score: Int = 0,
    val bestScore: Int = 0,
    val isStarted: Boolean = false,
    val isGameOver: Boolean = false
)
