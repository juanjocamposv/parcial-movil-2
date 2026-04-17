package com.example.parcial2.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val isLoading   by vm.isLoading.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 64.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        Text("👾", style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            text  = "Vici",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text  = "Sign in to continue",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(40.dp))

        // ── Form ──────────────────────────────────────────────────────────────
        StitchTextField(
            value         = email,
            onValueChange = { email = it; vm.clearError() },
            label         = "Email",
            keyboardType  = KeyboardType.Email,
            isError       = errorMessage != null
        )
        Spacer(Modifier.height(16.dp))
        StitchTextField(
            value         = password,
            onValueChange = { password = it; vm.clearError() },
            label         = "Password",
            isPassword    = true,
            imeAction     = ImeAction.Done,
            isError       = errorMessage != null
        )

        // ── Error message ─────────────────────────────────────────────────────
        AnimatedVisibility(visible = errorMessage != null) {
            Text(
                text      = errorMessage ?: "",
                color     = MaterialTheme.colorScheme.error,
                style     = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick  = onNavigateToForgotPassword,
            modifier = Modifier.align(Alignment.End)
        ) { Text("Forgot password?") }

        Spacer(Modifier.height(16.dp))

        // ── Login button ──────────────────────────────────────────────────────
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            StitchButton(
                text    = "Login",
                onClick = { vm.login(email, password, onLoginSuccess) }
            )
        }

        Spacer(Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(Modifier.height(24.dp))

        // ── Navigate to Register ──────────────────────────────────────────────
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Sign up")
        }
    }
}
