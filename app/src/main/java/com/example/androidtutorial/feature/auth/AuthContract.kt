package com.example.androidtutorial.feature.auth

data class AuthState(val login: String = "", val password: String = "")

sealed interface AuthIntent {
    data class Login(val value: String) : AuthIntent
    data class Password(val value: String) : AuthIntent
    data object Submit : AuthIntent
}

sealed interface AuthEffect {
    data object Entered : AuthEffect
}
