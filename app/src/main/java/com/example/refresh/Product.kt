package com.example.refresh.model

data class Product(
    val name: String,
    val price: Double,
    val description: String,
    val category: String,
    val userEmail: String,
    val imageBase64: String?

)
