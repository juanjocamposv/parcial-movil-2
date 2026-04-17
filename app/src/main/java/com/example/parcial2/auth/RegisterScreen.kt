package com.example.parcial2.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    vm: AuthViewModel = viewModel()
) {
    val isLoading    by vm.isLoading.collectAsState()
    val errorMessage by vm.errorMessage.collectAsState()

    // Estados del formulario
    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var confirm   by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var idNumber  by remember { mutableStateOf("") }
    var idType    by remember { mutableStateOf("CC") }

    // Estados de la UI
    var idTypeExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Configuración del DatePicker (Lógica para mayores de 18)
    val calendar = Calendar.getInstance()
    val todayMillis = calendar.timeInMillis
    calendar.add(Calendar.YEAR, -18)
    val maxBirthMillis = calendar.timeInMillis

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = maxBirthMillis
    )

    // Lógica de validación para habilitar el botón
    val isEmailValid = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isIdValid = idNumber.length in 5..15 && idNumber.all { it.isDigit() }
    val isDateValid = birthDate.isNotBlank()
    val isPassValid = password.length >= 6
    val isConfirmValid = confirm == password && confirm.isNotBlank()

    val canRegister = isEmailValid && isIdValid && isDateValid && isPassValid && isConfirmValid && !isLoading

    // Diálogo del Calendario
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        birthDate = sdf.format(Date(millis))
                    }
                    showDatePicker = false
                    vm.clearError()
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Cuenta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
            Text("🎮", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(8.dp))
            Text(
                "Únete a Vici",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(32.dp))

            // Campo de Email
            StitchTextField(
                value = email,
                onValueChange = { email = it; vm.clearError() },
                label = "Correo Electrónico",
                keyboardType = KeyboardType.Email,
                isError = email.isNotEmpty() && !isEmailValid,
                leadingIcon = { Icon(Icons.Default.Email, null) }
            )
            Spacer(Modifier.height(16.dp))

            // Selector de Tipo de Documento
            Box(modifier = Modifier.fillMaxWidth()) {
                StitchTextField(
                    value = if (idType == "CC") "Cédula de Ciudadanía" else "Cédula de Extranjería",
                    onValueChange = {},
                    label = "Tipo de Documento",
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { idTypeExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    }
                )
                // Capa invisible para detectar clics en todo el campo
                Box(Modifier.matchParentSize().clickable { idTypeExpanded = true })

                DropdownMenu(
                    expanded = idTypeExpanded,
                    onDismissRequest = { idTypeExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    DropdownMenuItem(
                        text = { Text("Cédula de Ciudadanía") },
                        onClick = { idType = "CC"; idTypeExpanded = false }
                    )
                    DropdownMenuItem(
                        text = { Text("Cédula de Extranjería") },
                        onClick = { idType = "CE"; idTypeExpanded = false }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            // Número de Documento
            StitchTextField(
                value = idNumber,
                onValueChange = { if (it.all { c -> c.isDigit() }) idNumber = it; vm.clearError() },
                label = "Número de Documento",
                keyboardType = KeyboardType.Number,
                isError = idNumber.isNotEmpty() && !isIdValid,
                leadingIcon = { Icon(Icons.Default.Person, null) }
            )
            Spacer(Modifier.height(16.dp))

            // Fecha de Nacimiento
            Box(modifier = Modifier.fillMaxWidth()) {
                StitchTextField(
                    value = birthDate,
                    onValueChange = {},
                    label = "Fecha de Nacimiento (18+)",
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.Refresh, null) } // Usamos Refresh como fallback seguro
                )
                Box(Modifier.matchParentSize().clickable { showDatePicker = true })
            }
            Spacer(Modifier.height(16.dp))

            // Contraseña
            StitchTextField(
                value = password,
                onValueChange = { password = it; vm.clearError() },
                label = "Contraseña (min 6)",
                isPassword = true,
                leadingIcon = { Icon(Icons.Default.Lock, null) }
            )
            Spacer(Modifier.height(16.dp))

            // Confirmar Contraseña
            StitchTextField(
                value = confirm,
                onValueChange = { confirm = it; vm.clearError() },
                label = "Confirmar Contraseña",
                isPassword = true,
                imeAction = ImeAction.Done,
                leadingIcon = { Icon(Icons.Default.Lock, null) }
            )

            AnimatedVisibility(visible = errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                StitchButton(
                    text = "Registrarse",
                    enabled = canRegister,
                    onClick = {
                        vm.register(email, password, confirm, birthDate, idType, idNumber, onRegisterSuccess)
                    }
                )
            }
        }
    }
}