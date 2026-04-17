package com.example.parcial2.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parcial2.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import java.text.SimpleDateFormat

class AuthViewModel : ViewModel() {

    private val repo = UserRepository()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _resetSent = MutableStateFlow(false)
    val resetSent: StateFlow<Boolean> = _resetSent

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Correo y contraseña son obligatorios."
            return
        }
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

    fun register(
        email: String,
        password: String,
        confirm: String,
        birthDate: String,
        idType: String,
        idNumber: String,
        onSuccess: () -> Unit
    ) {
        if (!validateInputs(email, password, confirm, birthDate, idNumber)) return
        
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            repo.register(email.trim(), password, idType, idNumber, birthDate).fold(
                onSuccess = { onSuccess() },
                onFailure = { _errorMessage.value = friendlyError(it) }
            )
            _isLoading.value = false
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) { _errorMessage.value = "Ingresa tu correo."; return }
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

    private fun validateInputs(email: String, pass: String, conf: String, date: String, id: String): Boolean {
        if (email.isBlank() || pass.isBlank() || id.isBlank() || date.isBlank()) {
            _errorMessage.value = "Todos los campos son obligatorios."; return false
        }
        if (id.length !in 5..15) {
            _errorMessage.value = "El documento debe tener entre 5 y 15 dígitos."; return false
        }
        if (pass.length < 6) {
            _errorMessage.value = "La contraseña debe tener al menos 6 caracteres."; return false
        }
        if (pass != conf) {
            _errorMessage.value = "Las contraseñas no coinciden."; return false
        }

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val birthDateObj = sdf.parse(date) ?: return false
            val birth = Calendar.getInstance().apply { time = birthDateObj }
            val today = Calendar.getInstance()
            
            var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            
            if (age < 18) {
                _errorMessage.value = "Debes ser mayor de 18 años para registrarte."; return false
            }
        } catch (e: Exception) {
            _errorMessage.value = "Formato de fecha inválido."; return false
        }
        return true
    }

    fun clearError() { 
        _errorMessage.value = null 
        _resetSent.value = false
    }

    private fun friendlyError(t: Throwable): String {
        val msg = t.message ?: "Error desconocido"
        return when {
            "no user record" in msg.lowercase() -> "No se encontró cuenta con este correo."
            "password is invalid" in msg.lowercase() || "wrong password" in msg.lowercase() -> "Contraseña incorrecta."
            "already in use" in msg.lowercase() -> "Este correo ya está registrado."
            "badly formatted" in msg.lowercase() -> "Formato de correo inválido."
            "network" in msg.lowercase() -> "Error de red. Revisa tu conexión."
            else -> msg
        }
    }
}
