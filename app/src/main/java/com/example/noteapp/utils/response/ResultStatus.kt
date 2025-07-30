package com.example.noteapp.utils.response

sealed class ResultStatus<out T> {
    data class Success<T>(val data: T) : ResultStatus<T>()
    data class Error(val message: String) : ResultStatus<Nothing>()
    object Loading : ResultStatus<Nothing>()
}