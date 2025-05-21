    package com.example.refresh

    import com.example.refresh.model.Product

    data class CartItem(
        val _id: String? = null,
        val userEmail: String,
        val productId: Product,
        var quantity: Int
    )
