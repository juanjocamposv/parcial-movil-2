package com.example.parcial2.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Handles all Firebase Auth and Firestore operations.
 * Keeps the ViewModels clean by exposing suspend functions that return Result<T>.
 */
class UserRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    // ── Auth ─────────────────────────────────────────────────────────────────

    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    suspend fun register(email: String, password: String): Result<Unit> = runCatching {
        auth.createUserWithEmailAndPassword(email, password).await()
        // Create a default stats document for the new user
        val uid = auth.currentUser!!.uid
        db.collection("users").document(uid)
            .set(mapOf("email" to email, "snakeBest" to 0, "memoryBest" to Int.MAX_VALUE))
            .await()
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email).await()
    }

    // ── Scores ───────────────────────────────────────────────────────────────

    /** Returns (snakeBestScore, memoryBestMoves). memoryBest = Int.MAX_VALUE means never played. */
    suspend fun loadStats(): Result<Pair<Int, Int>> = runCatching {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")
        val doc = db.collection("users").document(uid).get().await()
        val snake  = (doc.getLong("snakeBest")  ?: 0).toInt()
        val memory = (doc.getLong("memoryBest") ?: Int.MAX_VALUE).toInt()
        snake to memory
    }

    /** Save Snake best score only if it's higher than the stored one. */
    suspend fun updateSnakeBest(score: Int): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching
        val doc = db.collection("users").document(uid).get().await()
        val current = (doc.getLong("snakeBest") ?: 0).toInt()
        if (score > current) {
            db.collection("users").document(uid)
                .update("snakeBest", score).await()
        }
    }

    /** Save Memory best (fewer moves = better). */
    suspend fun updateMemoryBest(moves: Int): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching
        val doc = db.collection("users").document(uid).get().await()
        val current = (doc.getLong("memoryBest") ?: Int.MAX_VALUE).toInt()
        if (moves < current) {
            db.collection("users").document(uid)
                .update("memoryBest", moves).await()
        }
    }
    // Dentro de UserRepository.kt

    /** Carga el mejor puntaje de Flappy Bird */
    suspend fun loadFlappyBest(): Result<Int> = runCatching {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Not logged in")
        val doc = db.collection("users").document(uid).get().await()
        (doc.getLong("flappyBest") ?: 0).toInt()
    }

    /** Guarda el mejor puntaje de Flappy Bird */
    suspend fun updateFlappyBest(score: Int): Result<Unit> = runCatching {
        val uid = auth.currentUser?.uid ?: return@runCatching
        val doc = db.collection("users").document(uid).get().await()
        val current = (doc.getLong("flappyBest") ?: 0).toInt()
        if (score > current) {
            db.collection("users").document(uid)
                .update("flappyBest", score).await()
        }
    }

    fun currentUserEmail(): String? = auth.currentUser?.email
}
