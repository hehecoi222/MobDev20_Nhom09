package com.mobdev20.nhom09.quicknote.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.mobdev20.nhom09.quicknote.state.UserState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

class AccountViewModel @Inject constructor(): ViewModel() {
    private val _userState = MutableStateFlow(UserState())
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    val isSignIn = mutableStateOf(false)

    fun updateUser(user: FirebaseUser) {
        isSignIn.value = true
        _userState.update {
            it.copy(
                id = user.uid,
                username = user.displayName ?: "User"
            )
        }
    }

    fun signOut() {
        isSignIn.value = false
        _userState.value = UserState()
    }
}