package com.example.refresh.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.refresh.CartItem
import com.example.refresh.model.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CartManager(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val TAG = "CartManager"

    // ✅ Correct type: List<CartItem>
    fun getCartItems(): List<CartItem> {
        val cartJson = sharedPreferences.getString("cart_items", "")
        if (cartJson.isNullOrEmpty()) {
            return emptyList()
        }

        val type = object : TypeToken<List<CartItem>>() {}.type
        return try {
            gson.fromJson(cartJson, type)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting cart items: ${e.message}")
            emptyList()
        }
    }

    // ✅ Add product to cart (with default quantity = 1)
    fun addToCart(product: Product, quantity: Int = 1) {
        val currentCart = getCartItems().toMutableList()

        // Check if product already exists in cart
        val existingItem = currentCart.find { it.productId._id == product._id }
        if (existingItem != null) {
            existingItem.quantity += quantity
        } else {
            currentCart.add(CartItem(_id = product?._id, userEmail = "", productId = product, quantity = quantity))
        }

        saveCart(currentCart)
        Log.d(TAG, "Added to cart: ${product.name}, Cart size: ${currentCart.size}")
    }

    // ✅ Remove item by product ID
    fun removeFromCart(productId: String) {
        val currentCart = getCartItems().toMutableList()
        currentCart.removeIf { it.productId._id == productId }
        saveCart(currentCart)
    }

    // ✅ Clear the cart
    fun clearCart() {
        sharedPreferences.edit().remove("cart_items").apply()
    }

    // ✅ Total = sum of (product price * quantity)
    fun getCartTotal(): Double {
        val cartItems = getCartItems()
        return cartItems.sumOf { (it.productId.price ?: 0.0) * it.quantity }
    }

    // ✅ Save updated cart list
    private fun saveCart(cartItems: List<CartItem>) {
        val editor = sharedPreferences.edit()
        val cartJson = gson.toJson(cartItems)
        editor.putString("cart_items", cartJson)
        editor.apply()
    }
}
