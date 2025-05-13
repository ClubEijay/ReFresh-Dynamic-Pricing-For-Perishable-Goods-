package com.example.refresh.model

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val name: String? = null,
    val email: String? = null
)
