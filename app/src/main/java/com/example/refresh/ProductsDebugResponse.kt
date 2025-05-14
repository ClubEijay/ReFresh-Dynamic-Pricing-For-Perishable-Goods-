package com.example.refresh

import com.example.refresh.model.Product

data class ProductsDebugResponse(
    val success: Boolean,
    val count: Int,
    val products: List<Product>
)
