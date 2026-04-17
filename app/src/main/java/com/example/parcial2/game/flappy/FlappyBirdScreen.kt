package com.example.parcial2.game.flappy

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun FlappyBirdScreen(
    viewModel: FlappyBirdViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // ── Animaciones ──────────────────────────────────────────────────────────

    val birdWingAngle by animateFloatAsState(
        targetValue = if (state.isStarted && !state.isGameOver) {
            if (System.currentTimeMillis() % 400 < 200) 25f else -25f
        } else 0f,
        animationSpec = tween(120),
        label = "wing"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "bob")
    val birdBob by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "birdBob"
    )

    // ── UI Principal ─────────────────────────────────────────────────────────

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF6BB5FF), Color(0xFFB8E1FF))))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                viewModel.onScreenTapped()
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val scaleX = size.width / 1000f
            val scaleY = size.height / 1000f

            drawClouds()

            state.pipes.forEach { pipe ->
                drawPipe(pipe, scaleX, scaleY)
            }

            drawGround()

            val birdX = 250f * scaleX
            val birdY = (if (state.isStarted) state.birdY else state.birdY + birdBob) * scaleY
            val birdSize = 55f * maxOf(scaleX, scaleY)
            val rotation = (state.birdVelocity * 2.5f).coerceIn(-30f, 70f)

            rotate(rotation, Offset(birdX, birdY)) {
                drawBird(birdX, birdY, birdSize, birdWingAngle)
            }
        }

        // HUD
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.wrapContentSize().shadow(8.dp, CircleShape),
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.9f)
            ) {
                AnimatedContent(
                    targetState = state.score,
                    label = "score"
                ) { score ->
                    Text(
                        text = score.toString(),
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A2E),
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
            }

            if (!state.isStarted && !state.isGameOver) {
                Column(
                    modifier = Modifier.padding(top = 100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("FLAPPY BIRD", fontSize = 42.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Spacer(Modifier.height(40.dp))
                    Surface(shape = RoundedCornerShape(24.dp), color = Color.White.copy(alpha = 0.9f)) {
                        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.KeyboardArrowUp, null, tint = Color(0xFF6BB5FF))
                            Spacer(Modifier.width(12.dp))
                            Text("Tap to Fly", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier.padding(20.dp).align(Alignment.TopStart).background(Color.Black.copy(alpha = 0.2f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
        }

        if (state.isGameOver) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)), contentAlignment = Alignment.Center) {
                Card(
                    modifier = Modifier.fillMaxWidth(0.85f).padding(16.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Game Over", fontSize = 32.sp, fontWeight = FontWeight.Black)
                        Spacer(Modifier.height(24.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("SCORE", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(state.score.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("BEST", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                                Text(state.bestScore.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.height(32.dp))
                        Button(
                            onClick = { viewModel.restartGame() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6BB5FF))
                        ) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Try Again", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawClouds() {
    val cloudColor = Color.White.copy(alpha = 0.9f)
    drawCircle(cloudColor, 60f, Offset(150f, 120f))
    drawCircle(cloudColor, 80f, Offset(400f, 80f))
    drawCircle(cloudColor, 70f, Offset(700f, 140f))
}

private fun DrawScope.drawPipe(pipe: Pipe, scaleX: Float, scaleY: Float) {
    val pipeWidth = pipe.width * scaleX
    val pipeX = pipe.x * scaleX
    val topHeight = pipe.gapY * scaleY
    val bottomTop = (pipe.gapY + pipe.gapHeight) * scaleY
    val pipeBrush = Brush.verticalGradient(listOf(Color(0xFF34C759), Color(0xFF28A745)))
    drawRoundRect(pipeBrush, Offset(pipeX, 0f), Size(pipeWidth, topHeight), CornerRadius(24f, 24f))
    drawRoundRect(pipeBrush, Offset(pipeX, bottomTop), Size(pipeWidth, size.height - bottomTop), CornerRadius(24f, 24f))
}

private fun DrawScope.drawGround() {
    drawRect(Brush.verticalGradient(listOf(Color(0xFF7CB342), Color(0xFF558B2F))), Offset(0f, size.height - 85f), Size(size.width, 85f))
}

private fun DrawScope.drawBird(x: Float, y: Float, size: Float, wingAngle: Float) {
    drawCircle(Color(0xFFFFD93D), size / 2, Offset(x, y))
    drawCircle(Color.White, size * 0.15f, Offset(x + size * 0.2f, y - size * 0.15f))
    drawCircle(Color.Black, size * 0.08f, Offset(x + size * 0.22f, y - size * 0.15f))
}
