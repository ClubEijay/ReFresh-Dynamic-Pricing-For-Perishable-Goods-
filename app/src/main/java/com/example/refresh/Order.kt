package com.example.refresh

import com.example.refresh.model.Product
import com.google.gson.annotations.SerializedName

// Create a new class for order product items that matches server expectations
data class OrderProductItem(
    @SerializedName("productId") val productId: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Double,
    @SerializedName("quantity") val quantity: Int = 1
)

// Update your Order class to use OrderProductItem
data class Order(
    @SerializedName("_id") val _id: String? = null,
    @SerializedName("userEmail") val userEmail: String,
    @SerializedName("products") val products: List<OrderProductItem>, // Changed from List<Product>
    @SerializedName("totalAmount") val totalAmount: Double,
    @SerializedName("shippingAddress") val shippingAddress: ShippingAddress,
    @SerializedName("paymentInfo") val paymentInfo: PaymentInfo,
    @SerializedName("orderDate") val orderDate: Long = System.currentTimeMillis(),
    @SerializedName("status") val status: String = "Pending" // Note: capitalized to match server enum
)

// Fix field name mismatches in ShippingAddress
data class ShippingAddress(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("addressLine1") val addressLine1: String, // Matches server expectation
    @SerializedName("city") val city: String,
    @SerializedName("state") val state: String,
    @SerializedName("zipCode") val zipCode: String, // Matches server expectation
    @SerializedName("phoneNumber") val phoneNumber: String // Changed from "phone" to "phoneNumber"
)

data class PaymentInfo(
    @SerializedName("cardNumberLast4") val cardNumberLast4: String,
    @SerializedName("cardType") val cardType: String
)