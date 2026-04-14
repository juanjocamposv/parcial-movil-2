package com.example.parcial2.game.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parcial2.data.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// 8 pairs → 16 cards; each emoji appears twice
private val EMOJI_POOL = listOf("🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼")

data class MemoryCard(
    val id: Int,
    val emoji: String,
    val isFlipped: Boolean  = false,
    val isMatched: Boolean  = false
)

data class MemoryState(
    val cards: List<MemoryCard>  = emptyList(),
    val moves: Int               = 0,
    val isWon: Boolean           = false,
    val bestMoves: Int           = Int.MAX_VALUE,  // fewer = better
    val isProcessing: Boolean    = false            // prevent taps during flip-back delay
)

class MemoryViewModel : ViewModel() {

    private val repo = UserRepository()

    private val _state = MutableStateFlow(MemoryState())
    val state: StateFlow<MemoryState> = _state

    init {
        loadBest()
        startGame()
    }

    private fun loadBest() {
        viewModelScope.launch {
            repo.loadStats().onSuccess { (_, memory) ->
                _state.value = _state.value.copy(bestMoves = memory)
            }
        }
    }

    fun startGame() {
        val best = _state.value.bestMoves
        val cards = (EMOJI_POOL + EMOJI_POOL)
            .shuffled()
            .mapIndexed { idx, emoji -> MemoryCard(id = idx, emoji = emoji) }
        _state.value = MemoryState(cards = cards, bestMoves = best)
    }

    fun flipCard(cardId: Int) {
        val s = _state.value
        if (s.isProcessing) return

        val card = s.cards.find { it.id == cardId } ?: return
        if (card.isFlipped || card.isMatched) return

        // Count currently face-up (not yet matched) cards
        val flipped = s.cards.filter { it.isFlipped && !it.isMatched }

        if (flipped.size == 1) {
            // Second card flipped — check for match
            val first  = flipped[0]
            val newCards = s.cards.map {
                if (it.id == cardId) it.copy(isFlipped = true) else it
            }
            val matched = first.emoji == card.emoji

            if (matched) {
                val resolved = newCards.map {
                    if (it.emoji == card.emoji) it.copy(isMatched = true, isFlipped = false) else it
                }
                val won = resolved.all { it.isMatched }
                val newMoves = s.moves + 1
                val newBest  = if (won) minOf(s.bestMoves, newMoves) else s.bestMoves
                _state.value = s.copy(cards = resolved, moves = newMoves, isWon = won, bestMoves = newBest)
                if (won) viewModelScope.launch { repo.updateMemoryBest(newMoves) }
            } else {
                // No match: show both briefly then flip back
                _state.value = s.copy(cards = newCards, moves = s.moves + 1, isProcessing = true)
                viewModelScope.launch {
                    delay(900L)
                    _state.value = _state.value.copy(
                        cards = _state.value.cards.map {
                            if (!it.isMatched) it.copy(isFlipped = false) else it
                        },
                        isProcessing = false
                    )
                }
            }
        } else {
            // First card: just flip it
            _state.value = s.copy(
                cards = s.cards.map { if (it.id == cardId) it.copy(isFlipped = true) else it }
            )
        }
    }
}
