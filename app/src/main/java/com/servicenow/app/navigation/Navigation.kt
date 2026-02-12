package com.servicenow.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.servicenow.app.auth.AuthStatus
import com.servicenow.app.auth.AuthViewModel
import com.servicenow.app.auth.LoginScreen
import com.servicenow.app.auth.OtpScreen


object Routes {
    const val LOGIN = "login"
    const val OTP = "otp"
    const val HOME = "home"
}

@Composable
fun AuthNavHost(
    authViewModel: AuthViewModel,
    navController: NavHostController = rememberNavController()
) {
    val uiState by authViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        androidx.navigation.compose.composable(Routes.LOGIN) {
            LoginScreen(
                uiState = uiState,
                onPhoneChange = authViewModel::onPhoneNumberChange,
                onSendOtp = { activity ->
                    authViewModel.startPhoneVerification(activity)
                },
                onErrorConsumed = { authViewModel.clearError() },
                navigateToOtp = {
                    navController.navigate(Routes.OTP)
                }
            )
        }

        androidx.navigation.compose.composable(Routes.OTP) {
            OtpScreen(
                uiState = uiState,
                onOtpChange = authViewModel::onOtpChange,
                onVerifyOtp = { authViewModel.verifyOtp() },
                onErrorConsumed = { authViewModel.clearError() },
                navigateToHome = {}
            )
        }
    }
}

@Composable
fun AppEntry(authViewModel: AuthViewModel) {
    val authStatus by authViewModel.authStatus.collectAsState()

    when (authStatus) {
        is AuthStatus.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Loading...")
            }
        }
        is AuthStatus.Authenticated -> {
            HomeScreen()
        }
        is AuthStatus.Unauthenticated -> {
            AuthNavHost(authViewModel = authViewModel)
        }
    }
}

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Home Screen")
    }
}

