package com.servicenow.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.servicenow.app.auth.AuthStatus
import com.servicenow.app.auth.AuthViewModel
import com.servicenow.app.auth.LoginScreen
import com.servicenow.app.auth.OtpScreen
import com.servicenow.app.job.CreateJobScreen
import com.servicenow.app.job.JobViewModel
import com.servicenow.app.worker.WorkerDashboardScreen
import com.servicenow.app.worker.WorkerJob
import com.servicenow.app.worker.WorkerViewModel


object Routes {
    const val LOGIN = "login"
    const val OTP = "otp"
    const val HOME = "home"
    const val CREATE_JOB = "create_job"
    const val WORKER_DASHBOARD = "worker_dashboard"
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
        composable(Routes.LOGIN) {
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

        composable(Routes.OTP) {
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
fun HomeNavHost(
    authViewModel: AuthViewModel,
    jobViewModel: JobViewModel,
    workerViewModel: WorkerViewModel,
    navController: NavHostController = rememberNavController()
) {
    val jobUiState by jobViewModel.uiState.collectAsState()
    val workerUiState by workerViewModel.uiState.collectAsState()
    var homeMessage = remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onLogout = { authViewModel.logout() },
                onRequestElectrician = { navController.navigate(Routes.CREATE_JOB) },
                onGoOnlineAsWorker = { navController.navigate(Routes.WORKER_DASHBOARD) },
                homeMessage = homeMessage.value,
                onHomeMessageShown = { homeMessage.value = null }
            )
        }
        composable(Routes.CREATE_JOB) {
            CreateJobScreen(
                uiState = jobUiState,
                onDescriptionChange = jobViewModel::onDescriptionChange,
                onSubmit = {
                    jobViewModel.submitJob(
                        onJobCreated = {
                            homeMessage.value = "Job request created"
                            navController.navigate(Routes.HOME)
                        },
                        onError = { /* Snackbar handled inside screen */ }
                    )
                },
                onErrorConsumed = {
                    // Clear error in state by resetting message
                    jobViewModel.onDescriptionChange(jobUiState.description)
                }
            )
        }
        composable(Routes.WORKER_DASHBOARD) {
            WorkerDashboardScreen(
                uiState = workerUiState,
                onAcceptJob = { job: WorkerJob ->
                    workerViewModel.acceptJob(
                        jobId = job.id,
                        onAccepted = {
                            homeMessage.value = "Job accepted"
                            navController.navigate(Routes.HOME)
                        },
                        onError = { /* error snackbar handled in screen */ }
                    )
                },
                onErrorConsumed = { workerViewModel.clearError() }
            )
        }
    }
}

@Composable
fun AppEntry(
    authViewModel: AuthViewModel,
    jobViewModel: JobViewModel,
    workerViewModel: WorkerViewModel
) {
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
            HomeNavHost(
                authViewModel = authViewModel,
                jobViewModel = jobViewModel,
                workerViewModel = workerViewModel
            )
        }
        is AuthStatus.Unauthenticated -> {
            AuthNavHost(authViewModel = authViewModel)
        }
    }
}

@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onRequestElectrician: () -> Unit,
    onGoOnlineAsWorker: () -> Unit,
    homeMessage: String?,
    onHomeMessageShown: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(homeMessage) {
        val message = homeMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onHomeMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Home")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onGoOnlineAsWorker) {
                    Text(text = "Go Online as Worker")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onRequestElectrician) {
                    Text(text = "Request Electrician")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onLogout) {
                    Text(text = "Logout")
                }
            }
        }
    }
}

