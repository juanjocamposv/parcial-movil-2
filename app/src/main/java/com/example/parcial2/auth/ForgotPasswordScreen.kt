package com.example.parcial2.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val isLoading    by vm.isLoading.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()
    val resetSent    by vm.resetSent.collectAsState()

    var email by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🔑", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(16.dp))
            Text(
                "Enter the email address associated with your account and we'll send a reset link.",
                textAlign = TextAlign.Center,
                style     = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.height(32.dp))

            if (resetSent) {
                // ── Success state ─────────────────────────────────────────────
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text     = "✅  Reset email sent! Check your inbox.",
                        modifier = Modifier.padding(16.dp),
                        style    = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(Modifier.height(24.dp))
                StitchButton(text = "Back to Login", onClick = onNavigateBack)
            } else {
                StitchTextField(
                    value         = email,
                    onValueChange = { email = it; vm.clearError() },
                    label         = "Email",
                    keyboardType  = KeyboardType.Email
                )
                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text  = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Spacer(Modifier.height(24.dp))
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    StitchButton(text = "Send Reset Link", onClick = { vm.sendPasswordReset(email) })
                }
            }
        }
    }
}
