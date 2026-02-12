package com.servicenow.app.job

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
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
fun CreateJobScreen(
    uiState: JobUiState,
    onDescriptionChange: (String) -> Unit,
    onSubmit: () -> Unit,
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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Request Electrician")
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                label = { Text("Describe the problem") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { if (!uiState.isLoading) onSubmit() },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Submit Request")
                }
            }
        }
    }
}

