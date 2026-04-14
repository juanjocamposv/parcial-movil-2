package com.example.parcial2.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.parcial2.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val email: String      = "",
    val snakeBest: Int     = 0,
    val memoryBest: Int    = Int.MAX_VALUE,
    val isLoading: Boolean = true
)

class HomeViewModel : ViewModel() {

    private val repo = UserRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState

    init { loadUserData() }

    fun loadUserData() {
        viewModelScope.launch {
            val email = repo.currentUserEmail() ?: ""
            repo.loadStats().onSuccess { (snake, memory) ->
                _uiState.value = HomeUiState(
                    email      = email,
                    snakeBest  = snake,
                    memoryBest = memory,
                    isLoading  = false
                )
            }.onFailure {
                _uiState.value = HomeUiState(email = email, isLoading = false)
            }
        }
    }
}
