package com.example.refresh

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.refresh.network.RetrofitClient
import com.example.refresh.util.CartManager
import com.example.refresh.util.UserSessionManager
import kotlinx.coroutines.launch

class CheckoutActivity : AppCompatActivity() {
    private val TAG = "CheckoutActivity"
    private lateinit var fullName: EditText
    private lateinit var address1: EditText
    private lateinit var city: EditText
    private lateinit var state: EditText
    private lateinit var zip: EditText
    private lateinit var phone: EditText
    private lateinit var cardNumber: EditText
    private lateinit var expiry: EditText
    private lateinit var cvv: EditText
    private lateinit var checkoutBtn: Button
    private lateinit var sessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        // Initialize session manager to get user email
        sessionManager = UserSessionManager(this)

        fullName = findViewById(R.id.full_name_input)
        address1 = findViewById(R.id.address_line1_input)
        city = findViewById(R.id.city_input)
        state = findViewById(R.id.state_input)
        zip = findViewById(R.id.zip_input)
        phone = findViewById(R.id.phone_input)
        cardNumber = findViewById(R.id.card_number_input)
        expiry = findViewById(R.id.expiry_input)
        cvv = findViewById(R.id.cvv_input)
        checkoutBtn = findViewById(R.id.place_order_button)

        checkoutBtn.setOnClickListener {
            if (validateInputs()) {
                placeOrder()
            } else {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validateInputs(): Boolean {
        return fullName.text.isNotEmpty() &&
                address1.text.isNotEmpty() &&
                city.text.isNotEmpty() &&
                state.text.isNotEmpty() &&
                zip.text.isNotEmpty() &&
                phone.text.isNotEmpty() &&
                cardNumber.text.isNotEmpty() &&
                expiry.text.isNotEmpty() &&
                cvv.text.isNotEmpty()
    }

    private fun placeOrder() {
        val cartManager = CartManager(this)
        val cartItems = cartManager.getCartItems()

        // Get actual user email from session
        val userData = sessionManager.getUserDetails()
        val userEmail = userData[UserSessionManager.KEY_EMAIL] ?: "user@example.com"

        Log.d(TAG, "Placing order for user: $userEmail with ${cartItems.size} items")

        // Create ShippingAddress with field names matching the server expectations
        val shipping = ShippingAddress(
            fullName = fullName.text.toString(),
            addressLine1 = address1.text.toString(), // Don't change this variable name, just the field name
            city = city.text.toString(),
            state = state.text.toString(),
            zipCode = zip.text.toString(), // Don't change this variable name, just the field name
            phoneNumber = phone.text.toString() // Don't change this variable name, just the field name
        )

        val cardNum = cardNumber.text.toString()
        val payment = PaymentInfo(
            cardNumberLast4 = cardNum.takeLast(4),
            cardType = detectCardType(cardNum)
        )

        // Calculate total amount
        val total = cartItems.sumOf { it.productId.price * it.quantity }
        Log.d(TAG, "Order total: $total")

        // Create order with OrderProductItem objects instead of Product objects
        val order = Order(
            userEmail = userEmail,
            products = cartItems.map { cartItem ->
                // Transform Product into OrderProductItem with the structure server expects
                OrderProductItem(
                    productId = cartItem.productId._id ?: "", // MongoDB ID as string
                    name = cartItem.productId.name,
                    price = cartItem.productId.price,
                    quantity = cartItem.quantity
                )
            },
            totalAmount = total,
            shippingAddress = shipping,
            paymentInfo = payment,
            status = "Pending" // Capitalize to match server enum
        )

        // Add detailed logging to help debug
        Log.d(TAG, "Order structure: $order")
        Log.d(TAG, "First product: ${if (order.products.isNotEmpty()) order.products[0] else "none"}")

        lifecycleScope.launch {
            try {
                Toast.makeText(this@CheckoutActivity, "Processing your order...", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Sending order to server")

                val response = RetrofitClient.api.createOrder(order)

                if (response.isSuccessful) {
                    Log.d(TAG, "Order placed successfully!")
                    Toast.makeText(this@CheckoutActivity, "Order placed successfully!", Toast.LENGTH_LONG).show()
                    cartManager.clearCart()
                    finish()
                } else {
                    // Enhanced error logging
                    val errorBody = response.errorBody()?.string() ?: "No error body"
                    val errorMsg = "Failed to place order: ${response.code()} - ${response.message()}"
                    Log.e(TAG, "$errorMsg - Error response: $errorBody")
                    Toast.makeText(this@CheckoutActivity, errorMsg, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                val errorMsg = "Network error: ${e.message}"
                Log.e(TAG, errorMsg, e)
                Toast.makeText(this@CheckoutActivity, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun detectCardType(cardNumber: String): String {
        return when {
            cardNumber.startsWith("4") -> "Visa"
            cardNumber.startsWith("5") -> "MasterCard"
            cardNumber.startsWith("3") -> "American Express"
            else -> "Unknown"
        }
    }
}