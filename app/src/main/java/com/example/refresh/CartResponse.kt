package com.example.refresh


data class CartResponse(
    val success: Boolean,
    val cart: List<CartItem>
)

