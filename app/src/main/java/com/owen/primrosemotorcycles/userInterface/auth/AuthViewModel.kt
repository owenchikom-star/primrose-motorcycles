package com.owen.primrosemotorcycles.userInterface.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val isRegistering: Boolean = false,
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val isAuthenticated: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState(isAuthenticated = auth?.currentUser != null))
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email, errorMsg = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, errorMsg = null) }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name, errorMsg = null) }
    }

    fun toggleMode() {
        _uiState.update { it.copy(isRegistering = !it.isRegistering, errorMsg = null) }
    }

    fun authenticate() {
        val state = _uiState.value
        val email = state.email.trim()
        val password = state.password.trim()
        val name = state.name.trim()

        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMsg = "Email and password cannot be blank.") }
            return
        }

        if (state.isRegistering && name.isBlank()) {
            _uiState.update { it.copy(errorMsg = "Name cannot be blank for registration.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMsg = null) }
            try {
                if (state.isRegistering) {
                    val result = auth?.createUserWithEmailAndPassword(email, password)?.await()
                    val user = result?.user
                    if (user != null && name.isNotBlank()) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        user.updateProfile(profileUpdates).await()
                    }
                } else {
                    auth?.signInWithEmailAndPassword(email, password)?.await()
                }
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMsg = e.localizedMessage ?: "Authentication failed.") }
            }
        }
    }

    fun signOut() {
        auth?.signOut()
        _uiState.update { it.copy(isAuthenticated = false, email = "", password = "", name = "") }
    }
}
