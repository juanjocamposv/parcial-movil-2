package com.example.parcial2.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.parcial2.game.memory.MemoryGameSection
import com.example.parcial2.game.snake.SnakeGameSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToFlappy: () -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val uiState by vm.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vici 👾", style = MaterialTheme.typography.titleLarge) },
                actions = {
                    IconButton(onClick = { vm.loadUserData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh stats")
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ══════════════════════════════════════════════════════════════════
            // SECTION 1 — User Dashboard
            // ══════════════════════════════════════════════════════════════════
            UserDashboard(uiState = uiState, onLogout = onLogout)

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // ══════════════════════════════════════════════════════════════════
            // SECTION 2 — Snake Game
            // ══════════════════════════════════════════════════════════════════
            SnakeGameSection()

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // ══════════════════════════════════════════════════════════════════
            // SECTION 3 — Memory Card Game
            // ══════════════════════════════════════════════════════════════════
            MemoryGameSection()

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

            // ══════════════════════════════════════════════════════════════════
            // SECTION 4 — Sección Libre (Flappy Bird)
            // ══════════════════════════════════════════════════════════════════
            FreeSection(onNavigateToFlappy = onNavigateToFlappy)

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun FreeSection(onNavigateToFlappy: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Sección Libre 🚀",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigateToFlappy() },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐤", fontSize = 32.sp)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Flappy Bird",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "¡Nuevo reto disponible! Toca para jugar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// User Dashboard composable
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun UserDashboard(uiState: HomeUiState, onLogout: () -> Unit) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Avatar + Welcome
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = uiState.email.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        text  = "Welcome back!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text  = uiState.email,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier  = Modifier.weight(1f),
                    emoji     = "🐍",
                    label     = "Snake Best",
                    value     = "${uiState.snakeBest} pts"
                )
                StatCard(
                    modifier  = Modifier.weight(1f),
                    emoji     = "🧠",
                    label     = "Memory Best",
                    value     = if (uiState.memoryBest == Int.MAX_VALUE) "—"
                                else "${uiState.memoryBest} moves"
                )
            }

            Spacer(Modifier.height(16.dp))

            OutlinedButton(
                onClick  = onLogout,
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text       = value,
                style      = MaterialTheme.typography.titleLarge,
                color      = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
