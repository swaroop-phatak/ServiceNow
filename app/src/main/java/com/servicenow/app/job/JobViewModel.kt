package com.servicenow.app.job

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class JobUiState(
    val description: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class JobViewModel(
    private val jobRepository: JobRepository = JobRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(JobUiState())
    val uiState: StateFlow<JobUiState> = _uiState.asStateFlow()

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun submitJob(onJobCreated: () -> Unit, onError: (String) -> Unit) {
        val description = _uiState.value.description.trim()
        if (description.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please describe the problem")
            onError("Please describe the problem")
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        jobRepository.createElectricianJob(
            description = description,
            onSuccess = {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    description = ""
                )
                onJobCreated()
            },
            onError = { message ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = message
                )
                onError(message)
            }
        )
    }
}

