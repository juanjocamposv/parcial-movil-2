package com.example.parcial2.game.snake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parcial2.data.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

// Board dimensions
const val BOARD_COLS = 20
const val BOARD_ROWS = 20

enum class Direction { UP, DOWN, LEFT, RIGHT }

data class Point(val x: Int, val y: Int)

data class SnakeState(
    val snake: List<Point> = listOf(Point(10, 10), Point(9, 10), Point(8, 10)),
    val food: Point        = Point(15, 15),
    val direction: Direction = Direction.RIGHT,
    val score: Int         = 0,
    val isGameOver: Boolean = false,
    val isRunning: Boolean  = false,
    val bestScore: Int      = 0
)

class SnakeViewModel : ViewModel() {

    private val repo = UserRepository()

    private val _state = MutableStateFlow(SnakeState())
    val state: StateFlow<SnakeState> = _state

    private var gameJob: Job? = null
    // Buffer the next direction to prevent 180° reversal within one tick
    private var pendingDirection: Direction = Direction.RIGHT

    init { loadBest() }

    private fun loadBest() {
        viewModelScope.launch {
            repo.loadStats().onSuccess { (snake, _) ->
                _state.value = _state.value.copy(bestScore = snake)
            }
        }
    }

    fun startGame() {
        val best = _state.value.bestScore
        _state.value = SnakeState(bestScore = best, isRunning = true)
        pendingDirection = Direction.RIGHT
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (_state.value.isRunning && !_state.value.isGameOver) {
                delay(160L)   // game tick ~6 FPS
                tick()
            }
        }
    }

    /** Called by UI when the player swipes/taps a direction button. */
    fun changeDirection(dir: Direction) {
        val current = _state.value.direction
        // Prevent the snake from immediately reversing
        val opposite = when (dir) {
            Direction.UP    -> Direction.DOWN
            Direction.DOWN  -> Direction.UP
            Direction.LEFT  -> Direction.RIGHT
            Direction.RIGHT -> Direction.LEFT
        }
        if (current != opposite) pendingDirection = dir
    }

    private fun tick() {
        val s = _state.value
        if (s.isGameOver || !s.isRunning) return

        val dir  = pendingDirection
        val head = s.snake.first()
        val newHead = when (dir) {
            Direction.UP    -> Point(head.x, head.y - 1)
            Direction.DOWN  -> Point(head.x, head.y + 1)
            Direction.LEFT  -> Point(head.x - 1, head.y)
            Direction.RIGHT -> Point(head.x + 1, head.y)
        }

        // ── Collision: walls ───────────────────────────────────────────────
        if (newHead.x !in 0 until BOARD_COLS || newHead.y !in 0 until BOARD_ROWS) {
            gameOver(s); return
        }
        // ── Collision: self ────────────────────────────────────────────────
        if (newHead in s.snake) { gameOver(s); return }

        // ── Eat food ───────────────────────────────────────────────────────
        val ate   = newHead == s.food
        val newSnake = if (ate) listOf(newHead) + s.snake
                       else     (listOf(newHead) + s.snake).dropLast(1)
        val newScore = if (ate) s.score + 10 else s.score
        val newFood  = if (ate) spawnFood(newSnake) else s.food
        val newBest  = maxOf(s.bestScore, newScore)

        _state.value = s.copy(
            snake     = newSnake,
            food      = newFood,
            direction = dir,
            score     = newScore,
            bestScore = newBest
        )
    }

    private fun gameOver(s: SnakeState) {
        gameJob?.cancel()
        _state.value = s.copy(isGameOver = true, isRunning = false)
        viewModelScope.launch { repo.updateSnakeBest(s.score) }
    }

    private fun spawnFood(snake: List<Point>): Point {
        var p: Point
        do { p = Point(Random.nextInt(BOARD_COLS), Random.nextInt(BOARD_ROWS)) }
        while (p in snake)
        return p
    }
}
