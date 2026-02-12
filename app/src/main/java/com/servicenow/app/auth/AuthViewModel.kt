package com.servicenow.app.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val phoneNumber: String = "",
    val otp: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val verificationId: String? = null,
    val isLoggedIn: Boolean = false
)

sealed class AuthStatus {
    object Loading : AuthStatus()
    object Authenticated : AuthStatus()
    object Unauthenticated : AuthStatus()
}

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _authStatus = MutableStateFlow<AuthStatus>(AuthStatus.Loading)
    val authStatus: StateFlow<AuthStatus> = _authStatus.asStateFlow()

    init {
        val currentUser = FirebaseAuth.getInstance().currentUser
        _authStatus.value = if (currentUser != null) {
            AuthStatus.Authenticated
        } else {
            AuthStatus.Unauthenticated
        }
    }

    fun onPhoneNumberChange(value: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = value)
    }

    fun onOtpChange(value: String) {
        _uiState.value = _uiState.value.copy(otp = value)
    }

    fun startPhoneVerification(activity: Activity) {
        val phone = _uiState.value.phoneNumber.trim()
        if (phone.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter phone number")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

        authRepository.startPhoneNumberVerification(
            phoneNumber = phone,
            activity = activity,
            onCodeSent = { verificationId ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    verificationId = verificationId
                )
            },
            onVerificationCompleted = {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true
                )
                _authStatus.value = AuthStatus.Authenticated
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = error
                )
            }
        )
    }

    fun verifyOtp() {
        val state = _uiState.value
        val verificationId = state.verificationId
        val otp = state.otp.trim()

        if (verificationId.isNullOrEmpty() || otp.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Invalid code")
            return
        }

        _uiState.value = state.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            authRepository.verifyOtp(
                verificationId = verificationId,
                otp = otp,
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
                    )
                    _authStatus.value = AuthStatus.Authenticated
                },
                onError = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
        _uiState.value = AuthUiState()
        _authStatus.value = AuthStatus.Unauthenticated
    }
}

