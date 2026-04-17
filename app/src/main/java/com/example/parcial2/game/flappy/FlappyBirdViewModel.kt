package com.example.parcial2.game.flappy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parcial2.data.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

class FlappyBirdViewModel : ViewModel() {

    private val userRepository = UserRepository()

    private val _state = MutableStateFlow(FlappyBirdState())
    val state: StateFlow<FlappyBirdState> = _state.asStateFlow()

    private var gameJob: Job? = null

    // Constantes físicas mejoradas (Mundo lógico de 0 a 1000)
    private val gravity = 0.8f          // Gravedad más realista
    private val jumpStrength = -12f     // Salto más controlado
    private val pipeSpeed = 5f          // Velocidad de tuberías
    private val birdX = 250f
    private val birdRadius = 30f
    private val maxVelocity = 18f       // Velocidad máxima
    private val pipeSpawnInterval = 350f // Distancia entre tuberías
    private var lastPipeX = 0f

    init {
        // Cargar el mejor puntaje al iniciar
        viewModelScope.launch {
            userRepository.loadFlappyBest().onSuccess { savedBestScore ->
                _state.update { it.copy(bestScore = savedBestScore) }
            }
        }
    }

    fun onScreenTapped() {
        if (_state.value.isGameOver) return

        if (!_state.value.isStarted) {
            startGame()
        } else {
            // Solo saltar si el juego está activo
            _state.update {
                it.copy(birdVelocity = jumpStrength)
            }
        }
    }

    private fun startGame() {
        _state.update {
            FlappyBirdState(
                bestScore = it.bestScore,
                isStarted = true,
                pipes = listOf(generatePipe(800f)) // Empieza más cerca
            )
        }

        lastPipeX = 800f

        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (true) {
                val currentTime = System.currentTimeMillis()
                val deltaTime = (currentTime - lastTime) / 16f // Normalizar a 60 FPS
                lastTime = currentTime

                delay(16L) // ~60 FPS
                updateGameLoop(deltaTime.coerceIn(0.5f, 1.5f))
            }
        }
    }

    private fun updateGameLoop(deltaTime: Float = 1f) {
        val currentState = _state.value
        if (currentState.isGameOver || !currentState.isStarted) return

        // 1. Física del pájaro mejorada (con delta time)
        var newVelocity = currentState.birdVelocity + (gravity * deltaTime)
        if (newVelocity > maxVelocity) newVelocity = maxVelocity
        if (newVelocity < -maxVelocity) newVelocity = -maxVelocity

        val newBirdY = currentState.birdY + (newVelocity * deltaTime)

        // 2. Movimiento y generación de tuberías
        val newPipes = currentState.pipes.map {
            it.copy(x = it.x - (pipeSpeed * deltaTime))
        }.toMutableList()

        // Generar nueva tubería si es necesario
        if (newPipes.isEmpty() || newPipes.last().x < 900f - pipeSpawnInterval) {
            val newPipeX = if (newPipes.isEmpty()) 1000f else newPipes.last().x + pipeSpawnInterval
            newPipes.add(generatePipe(newPipeX))
            lastPipeX = newPipeX
        }

        // Eliminar tuberías fuera de pantalla
        newPipes.removeAll { it.x + it.width < 0 }

        // 3. Lógica de Puntuación mejorada
        var newScore = currentState.score
        newPipes.forEach { pipe ->
            if (!pipe.passed && pipe.x + pipe.width < birdX) {
                pipe.passed = true
                newScore++
            }
        }

        // 4. Detección de Colisiones mejorada (más precisa)
        val isColliding = checkCollision(newBirdY, newPipes)
        val hitGround = newBirdY + birdRadius >= 1000f - 80f // Considerando el suelo visual
        val hitCeiling = newBirdY - birdRadius <= 0f

        if (isColliding || hitGround || hitCeiling) {
            endGame(newScore)
            return
        }

        // Actualizar estado si el juego continúa
        _state.update {
            it.copy(
                birdY = newBirdY.coerceIn(birdRadius, 1000f - birdRadius - 80f),
                birdVelocity = newVelocity,
                pipes = newPipes,
                score = newScore
            )
        }
    }

    private fun generatePipe(startX: Float): Pipe {
        // Generar huecos más jugables
        val minGapY = 150f
        val maxGapY = 700f
        val gapHeight = 280f + Random.nextInt(-40, 41) // Variación en el tamaño del hueco

        return Pipe(
            x = startX,
            gapY = Random.nextFloat() * (maxGapY - minGapY) + minGapY,
            gapHeight = gapHeight
        )
    }

    private fun checkCollision(birdY: Float, pipes: List<Pipe>): Boolean {
        // Hitbox más precisa y justa
        val hitboxReduction = 8f // Hace el juego más indulgente
        val birdLeft = birdX - birdRadius + hitboxReduction
        val birdRight = birdX + birdRadius - hitboxReduction
        val birdTop = birdY - birdRadius + hitboxReduction
        val birdBottom = birdY + birdRadius - hitboxReduction

        for (pipe in pipes) {
            val pipeLeft = pipe.x
            val pipeRight = pipe.x + pipe.width
            val topPipeBottom = pipe.gapY
            val bottomPipeTop = pipe.gapY + pipe.gapHeight

            // Verifica colisión en X
            if (birdRight > pipeLeft && birdLeft < pipeRight) {
                // Verifica colisión con tuberías
                if (birdTop < topPipeBottom || birdBottom > bottomPipeTop) {
                    return true
                }
            }
        }
        return false
    }

    private fun endGame(finalScore: Int) {
        gameJob?.cancel()

        viewModelScope.launch {
            val currentBest = _state.value.bestScore
            val newBest = if (finalScore > currentBest) finalScore else currentBest

            if (finalScore > currentBest && finalScore > 0) {
                userRepository.updateFlappyBest(finalScore)
            }

            _state.update {
                it.copy(
                    isGameOver = true,
                    birdVelocity = 0f,
                    bestScore = newBest,
                    score = finalScore
                )
            }
        }
    }

    fun restartGame() {
        gameJob?.cancel()
        _state.update {
            FlappyBirdState(bestScore = it.bestScore)
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameJob?.cancel()
    }
}