package com.servicenow.app.worker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WorkerUiState(
    val jobs: List<WorkerJob> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class WorkerViewModel(
    private val repository: WorkerRepository = WorkerRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkerUiState())
    val uiState: StateFlow<WorkerUiState> = _uiState.asStateFlow()

    private var listenerRegistration: ListenerRegistration? = null

    init {
        subscribeToJobs()
    }

    private fun subscribeToJobs() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        listenerRegistration = repository.observeRequestedJobs(
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

    fun acceptJob(jobId: String, onAccepted: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            repository.acceptJob(
                jobId = jobId,
                onSuccess = {
                    onAccepted()
                },
                onError = { message ->
                    _uiState.value = _uiState.value.copy(errorMessage = message)
                    onError(message)
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        super.onCleared()
    }
}

