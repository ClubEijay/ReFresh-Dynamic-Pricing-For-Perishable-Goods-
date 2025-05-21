package com.example.refresh

data class CartItemRequest(
    val userEmail: String,
    val productId: String,
    val quantity: Int
)
