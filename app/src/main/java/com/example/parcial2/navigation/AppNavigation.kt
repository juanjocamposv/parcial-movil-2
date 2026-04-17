package com.example.parcial2.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.parcial2.auth.ForgotPasswordScreen
import com.example.parcial2.auth.LoginScreen
import com.example.parcial2.auth.RegisterScreen
import com.example.parcial2.game.flappy.FlappyBirdScreen
import com.example.parcial2.game.flappy.FlappyBirdViewModel
import com.example.parcial2.home.HomeScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavigation(navController: NavHostController = rememberNavController()) {
    val auth = remember { FirebaseAuth.getInstance() }
    val startDestination = if (auth.currentUser != null) NavRoutes.HOME else NavRoutes.LOGIN

    NavHost(navController = navController, startDestination = startDestination) {
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister       = { navController.navigate(NavRoutes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(NavRoutes.FORGOT_PASSWORD) },
                onLoginSuccess             = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                onNavigateBack    = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(NavRoutes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(NavRoutes.HOME) {
            HomeScreen(
                onLogout = {
                    auth.signOut()
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(NavRoutes.HOME) { inclusive = true }
                    }
                },
                onNavigateToFlappy = {
                    navController.navigate(NavRoutes.FLAPPY_BIRD)
                }
            )
        }
        composable(NavRoutes.FLAPPY_BIRD) {
            val flappyViewModel: FlappyBirdViewModel = viewModel()
            FlappyBirdScreen(
                viewModel = flappyViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
