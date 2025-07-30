package com.example.noteapp.ui.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.noteapp.data.models.User
import com.example.noteapp.data.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val userRepository : UserRepository by lazy {
        UserRepository()
    }


    /**
     * Add User to firebase database
     * @param user: User
     */
    fun addUser(user: User) {
        viewModelScope.launch(Dispatchers.Main) {
            userRepository.addNewUser(user)
        }
    }
}