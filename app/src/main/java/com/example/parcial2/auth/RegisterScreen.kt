package com.example.parcial2.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val isLoading    by vm.isLoading.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm  by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
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
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🚀", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "Join Game Hub",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(32.dp))

            StitchTextField(
                value         = email,
                onValueChange = { email = it; vm.clearError() },
                label         = "Email"
            )
            Spacer(Modifier.height(16.dp))
            StitchTextField(
                value         = password,
                onValueChange = { password = it; vm.clearError() },
                label         = "Password",
                isPassword    = true
            )
            Spacer(Modifier.height(16.dp))
            StitchTextField(
                value         = confirm,
                onValueChange = { confirm = it; vm.clearError() },
                label         = "Confirm Password",
                isPassword    = true,
                imeAction     = ImeAction.Done
            )

            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text      = errorMessage ?: "",
                    color     = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier  = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(Modifier.height(24.dp))
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                StitchButton(
                    text    = "Create Account",
                    onClick = { vm.register(email, password, confirm, onRegisterSuccess) }
                )
            }
        }
    }
}
