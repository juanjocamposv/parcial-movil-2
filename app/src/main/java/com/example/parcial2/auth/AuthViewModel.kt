package com.example.parcial2.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parcial2.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

/** Shared ViewModel for Login / Register / ForgotPassword screens. */
class AuthViewModel : ViewModel() {

    private val repo = UserRepository()

    // Loading & error states exposed to the UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // ── Login ─────────────────────────────────────────────────────────────────
    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (!validateLoginInputs(email, password)) return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repo.login(email.trim(), password).fold(
                onSuccess = { onSuccess() },
                onFailure = { _errorMessage.value = friendlyError(it) }
            )
            _isLoading.value = false
        }
    }

    // ── Register ──────────────────────────────────────────────────────────────
    fun register(email: String, password: String, confirm: String, birthDate: String, onSuccess: () -> Unit) {
        if (!validateRegisterInputs(email, password, confirm, birthDate)) return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repo.register(email.trim(), password).fold(
                onSuccess = { onSuccess() },
                onFailure = { _errorMessage.value = friendlyError(it) }
            )
            _isLoading.value = false
        }
    }

    // ── Password Reset ────────────────────────────────────────────────────────
    private val _resetSent = MutableStateFlow(false)
    val resetSent: StateFlow<Boolean> = _resetSent

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) { _errorMessage.value = "Enter your email address."; return }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repo.sendPasswordReset(email.trim()).fold(
                onSuccess = { _resetSent.value = true },
                onFailure = { _errorMessage.value = friendlyError(it) }
            )
            _isLoading.value = false
        }
    }

    fun clearError() { _errorMessage.value = null }

    // ── Validation helpers ────────────────────────────────────────────────────
    private fun validateLoginInputs(email: String, password: String): Boolean {
        return when {
            email.isBlank()    -> { _errorMessage.value = "Email cannot be empty."; false }
            password.isBlank() -> { _errorMessage.value = "Password cannot be empty."; false }
            else               -> true
        }
    }

    private fun validateRegisterInputs(email: String, password: String, confirm: String, birthDate: String): Boolean {
        if (email.isBlank()) { _errorMessage.value = "Email cannot be empty."; return false }
        if (password.length < 6) { _errorMessage.value = "Password must be at least 6 characters."; return false }
        if (password != confirm) { _errorMessage.value = "Passwords do not match."; return false }
        
        // Birth date validation (format YYYY-MM-DD or simple presence)
        if (birthDate.isBlank()) { _errorMessage.value = "Please enter your birth date."; return false }
        
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val birth = LocalDate.parse(birthDate, formatter)
            val today = LocalDate.now()
            if (birth.isAfter(today)) {
                _errorMessage.value = "You can't be born in the future!"; false
            } else if (Period.between(birth, today).years < 13) {
                _errorMessage.value = "You must be at least 13 years old to play."; false
            } else {
                true
            }
        } catch (e: Exception) {
            _errorMessage.value = "Invalid date format. Use YYYY-MM-DD."; false
        }
    }

    /** Converts Firebase exceptions into readable messages. */
    private fun friendlyError(t: Throwable): String {
        val msg = t.message ?: "Unknown error"
        return when {
            "no user record"    in msg.lowercase() -> "No account found with that email."
            "password is invalid" in msg.lowercase() ||
            "wrong password"      in msg.lowercase() -> "Incorrect password."
            "email address is already" in msg.lowercase() -> "This email is already registered."
            "badly formatted"   in msg.lowercase() -> "Invalid email format."
            "network"           in msg.lowercase() -> "Network error. Check your connection."
            else -> msg
        }
    }
}
