package com.example.refresh.model

data class Product(
    val id: String = "",
    val name: String,
    val basePrice: Double,
    val markdownRate: Double = 0.0,
    val currentPrice: Double,
    val dateAdded: String = "",
    val daysOnShelf: Int = 0,
    val isPriceApproved: Boolean = true,
    val description: String,
    val category: String,
    val userEmail: String,
    val imageBase64: String?,
    val lastChecked: String = ""
)
