package com.servicenow.app.job

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MyJobsUiState(
    val jobs: List<JobRepository.UserJob> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class MyJobsViewModel(
    private val jobRepository: JobRepository = JobRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyJobsUiState())
    val uiState: StateFlow<MyJobsUiState> = _uiState.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null

    init {
        subscribeToJobs()
    }

    private fun subscribeToJobs() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        listenerRegistration = jobRepository.observeUserJobs(
            onJobsChanged = { jobs ->
                _uiState.value = _uiState.value.copy(
                    jobs = jobs,
                    isLoading = false
                )
            },
            onError = { message ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = message
                )
            }
        )
    }

    fun confirmCompletion(jobId: String, otp: String) {
        if (otp.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter OTP")
            return
        }

        jobRepository.confirmJobCompletion(
            jobId = jobId,
            otpInput = otp,
            onSuccess = {
                // Listener will deliver updated job; nothing else to do
            },
            onError = { message ->
                _uiState.value = _uiState.value.copy(errorMessage = message)
            }
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        super.onCleared()
    }
}

