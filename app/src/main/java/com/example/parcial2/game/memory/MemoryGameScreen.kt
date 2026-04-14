package com.example.parcial2.game.memory

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MemoryGameSection(vm: MemoryViewModel = viewModel()) {
    val state by vm.state.collectAsState()

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
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
                Text("🧠 Memory", style = MaterialTheme.typography.titleLarge)
                Column(horizontalAlignment = Alignment.End) {
                    Text("Moves: ${state.moves}", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (state.bestMoves == Int.MAX_VALUE) "Best: —"
                        else "Best: ${state.bestMoves} moves",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            // Win banner
            if (state.isWon) {
                Card(
                    colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "🎉 You won in ${state.moves} moves!",
                        modifier = Modifier.padding(12.dp).align(Alignment.CenterHorizontally),
                        style    = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(Modifier.height(8.dp))
            }

            // Card Grid — non-scrollable, fixed height
            LazyVerticalGrid(
                columns             = GridCells.Fixed(4),
                modifier            = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp),
                contentPadding      = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement   = Arrangement.spacedBy(6.dp),
                userScrollEnabled     = false
            ) {
                items(state.cards, key = { it.id }) { card ->
                    MemoryCardItem(
                        card    = card,
                        onClick = { vm.flipCard(card.id) }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = { vm.startGame() },
                shape   = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) { Text("New Game") }
        }
    }
}

/** Single card with 3-D flip animation. */
@Composable
fun MemoryCardItem(card: MemoryCard, onClick: () -> Unit) {
    val rotation by animateFloatAsState(
        targetValue  = if (card.isFlipped || card.isMatched) 180f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label        = "cardFlip"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .graphicsLayer { rotationY = rotation; cameraDistance = 12f * density }
            .background(
                when {
                    card.isMatched -> MaterialTheme.colorScheme.primaryContainer
                    card.isFlipped -> MaterialTheme.colorScheme.secondaryContainer
                    else           -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
            .clickable(enabled = !card.isMatched) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (rotation > 90f) {
            // Back-face correction when rotated past 90°
            Text(
                text     = card.emoji,
                fontSize = 24.sp,
                modifier = Modifier.graphicsLayer { rotationY = 180f }
            )
        } else {
            // Card back — show "?"
            Text(
                text     = "❓",
                fontSize = 20.sp,
                color    = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}
