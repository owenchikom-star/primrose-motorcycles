package com.owen.primrosemotorcycles.userInterface.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        viewModel = AuthViewModel()
    }

    @Test
    fun initialState_isNotAuthenticated() {
        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertFalse(state.isRegistering)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertNull(state.errorMsg)
    }

    @Test
    fun updateEmail_updatesStateAndClearsError() {
        viewModel.updateEmail("test@example.com")
        assertEquals("test@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.errorMsg)
    }

    @Test
    fun updatePassword_updatesState() {
        viewModel.updatePassword("secret123")
        assertEquals("secret123", viewModel.uiState.value.password)
    }

    @Test
    fun updateName_updatesState() {
        viewModel.updateName("John Motor")
        assertEquals("John Motor", viewModel.uiState.value.name)
    }

    @Test
    fun toggleMode_switchesRegistrationMode() {
        assertFalse(viewModel.uiState.value.isRegistering)

        viewModel.toggleMode()
        assertTrue(viewModel.uiState.value.isRegistering)

        viewModel.toggleMode()
        assertFalse(viewModel.uiState.value.isRegistering)
    }

    @Test
    fun authenticate_blankFields_showsError() {
        viewModel.authenticate()
        assertEquals("Email and password cannot be blank.", viewModel.uiState.value.errorMsg)
    }

    @Test
    fun authenticate_registeringBlankName_showsError() {
        viewModel.toggleMode()
        viewModel.updateEmail("user@example.com")
        viewModel.updatePassword("password")

        viewModel.authenticate()
        assertEquals("Name cannot be blank for registration.", viewModel.uiState.value.errorMsg)
    }

    @Test
    fun signOut_clearsCredentialsAndAuthentication() {
        viewModel.updateEmail("user@example.com")
        viewModel.updatePassword("password")
        viewModel.updateName("User")

        viewModel.signOut()

        val state = viewModel.uiState.value
        assertFalse(state.isAuthenticated)
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertEquals("", state.name)
    }
}
