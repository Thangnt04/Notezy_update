package com.example.noteapp.ui.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.data.models.User
import com.example.noteapp.data.repositories.AuthenticationRepository
import com.example.noteapp.utils.response.AuthResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthenticationViewModel : ViewModel() {
    private val authenticationRepository: AuthenticationRepository by lazy {
        AuthenticationRepository()
    }

    private val _userAuthResult = MutableLiveData<AuthResult<User>>()
    val userAuthResult: LiveData<AuthResult<User>>
        get() = _userAuthResult

    fun signInWithGoogle(account: GoogleSignInAccount) {
        Log.d("AuthViewModel", "Starting sign-in with Google: ${account.email}")
        viewModelScope.launch(Dispatchers.Main) {
            val authResult = authenticationRepository.signInWithGoogle(account)
            Log.d("AuthViewModel", "AuthResult from repository: ${authResult.value}")
            _userAuthResult.postValue(authResult.value)
        }
    }
}