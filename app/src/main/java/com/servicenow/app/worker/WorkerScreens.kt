package com.servicenow.app.worker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WorkerDashboardScreen(
    uiState: WorkerUiState,
    onAcceptJob: (WorkerJob) -> Unit,
    onMarkInProgress: (WorkerJob) -> Unit,
    onMarkCompleted: (WorkerJob) -> Unit,
    onErrorConsumed: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onErrorConsumed()
        }
    }

    LaunchedEffect(uiState.lastGeneratedOtp) {
        val otp = uiState.lastGeneratedOtp
        if (otp != null) {
            snackbarHostState.showSnackbar("Completion OTP: $otp")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = "Worker Dashboard")
            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.jobs.isEmpty()) {
                Text(text = "No requested jobs right now.")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.jobs) { job ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Text(text = job.description)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { onAcceptJob(job) }) { Text(text = "Accept") }
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(onClick = { onMarkInProgress(job) }) { Text(text = "Mark In Progress") }
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(onClick = { onMarkCompleted(job) }) { Text(text = "Mark Completed") }
                        }
                    }
                }
            }
        }
    }
}

