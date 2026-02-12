package com.servicenow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.servicenow.app.auth.AuthViewModel
import com.servicenow.app.job.JobViewModel
import com.servicenow.app.navigation.AppEntry
import com.servicenow.app.worker.WorkerViewModel
import com.servicenow.app.ui.theme.ServiceNowTheme

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val jobViewModel: JobViewModel by viewModels()
    private val workerViewModel: WorkerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ServiceNowTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppEntry(
                        authViewModel = authViewModel,
                        jobViewModel = jobViewModel,
                        workerViewModel = workerViewModel
                    )
                }
            }
        }
    }
}