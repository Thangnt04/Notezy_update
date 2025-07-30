package com.example.noteapp.utils.response

sealed class EmptyResult(
    val message: String? = null
) {
    class Success() : EmptyResult()
    class Error(message: String) : EmptyResult(message)
}