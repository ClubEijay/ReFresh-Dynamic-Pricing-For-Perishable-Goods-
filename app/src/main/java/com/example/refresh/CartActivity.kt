package com.example.refresh

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.refresh.model.Product
import com.example.refresh.util.CartManager
import java.text.DecimalFormat

class CartActivity : AppCompatActivity() {

    private lateinit var cartManager: CartManager
    private lateinit var cartItemsContainer: LinearLayout
    private lateinit var subtotalValue: TextView
    private lateinit var taxValue: TextView
    private lateinit var totalValue: TextView
    private lateinit var checkoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        cartManager = CartManager(this)

        // Initialize views
        cartItemsContainer = findViewById(R.id.cart_items_container)
        subtotalValue = findViewById(R.id.subtotal_value)
        taxValue = findViewById(R.id.tax_value)
        totalValue = findViewById(R.id.total_value)
        checkoutButton = findViewById(R.id.checkout_button)

        // Set up back button
        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            finish()
        }

        // Set up clear cart button
        val clearCartButton: ImageButton = findViewById(R.id.clear_cart_btn)
        clearCartButton.setOnClickListener {
            showClearCartConfirmation()
        }

        // Set up checkout button
        checkoutButton.setOnClickListener {
            if (cartManager.getCartItems().isEmpty()) {
                Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Start checkout process
            val intent = Intent(this, CheckoutActivity::class.java)
            startActivity(intent)
        }

        // Load cart items
        loadCartItems()

        // Update summary
        updateSummary()
    }

    private fun showClearCartConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Clear Cart")
            .setMessage("Are you sure you want to remove all items from your cart?")
            .setPositiveButton("Yes") { _, _ ->
                cartManager.clearCart()
                loadCartItems()
                updateSummary()
                Toast.makeText(this, "Cart cleared", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun loadCartItems() {
        // Clear existing items
        cartItemsContainer.removeAllViews()

        // Get cart items
        val cartItems = cartManager.getCartItems()

        if (cartItems.isEmpty()) {
            // Show empty cart message
            val emptyCartMessage = TextView(this)
            emptyCartMessage.text = "Your cart is empty"
            emptyCartMessage.textSize = 18f
            emptyCartMessage.setTextColor(Color.WHITE)
            emptyCartMessage.gravity = android.view.Gravity.CENTER
            emptyCartMessage.setPadding(0, 100, 0, 100)
            cartItemsContainer.addView(emptyCartMessage)

            // Disable checkout button
            checkoutButton.isEnabled = false
            checkoutButton.alpha = 0.5f
        } else {
            // Enable checkout button
            checkoutButton.isEnabled = true
            checkoutButton.alpha = 1.0f

            // Display cart items
            for (cartItem in cartItems) {
                val product = cartItem.productId   // extract the Product from CartItem
                val cartItemView = createCartItemView(product)
                cartItemsContainer.addView(cartItemView)
            }

        }
    }

    private fun createCartItemView(product: Product): View {
        // Create a cart item view
        val cartItemLayout = LinearLayout(this)
        cartItemLayout.orientation = LinearLayout.HORIZONTAL
        cartItemLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 0, 0, 16) }
        cartItemLayout.setBackgroundColor(Color.parseColor("#222222"))
        cartItemLayout.setPadding(16, 16, 16, 16)

        // Create image container with fixed dimensions
        val imageContainer = LinearLayout(this)
        imageContainer.layoutParams = LinearLayout.LayoutParams(120, 120)

        // Product image
        product.imageBase64?.let { base64 ->
            try {
                val imageView = ImageView(this)
                val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                imageView.setImageBitmap(bitmap)
                imageView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageContainer.addView(imageView)
            } catch (e: Exception) {
                // If image can't be loaded, use placeholder
                val placeholderView = View(this)
                placeholderView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
                )
                placeholderView.setBackgroundColor(Color.GRAY)
                imageContainer.addView(placeholderView)
            }
        } ?: run {
            // If no image, use placeholder
            val placeholderView = View(this)
            placeholderView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            placeholderView.setBackgroundColor(Color.GRAY)
            imageContainer.addView(placeholderView)
        }

        cartItemLayout.addView(imageContainer)

        // Product details
        val detailsLayout = LinearLayout(this)
        detailsLayout.orientation = LinearLayout.VERTICAL
        detailsLayout.layoutParams = LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        ).apply { setMargins(16, 0, 16, 0) }

        // Product name
        val nameTextView = TextView(this)
        nameTextView.text = product.name
        nameTextView.textSize = 16f
        nameTextView.setTextColor(Color.WHITE)
        detailsLayout.addView(nameTextView)

        // Product price
        val priceTextView = TextView(this)
        priceTextView.text = "Price: $${product.price}"
        priceTextView.textSize = 14f
        priceTextView.setTextColor(Color.LTGRAY)
        detailsLayout.addView(priceTextView)

        // Product description
        val descTextView = TextView(this)
        descTextView.text = product.description
        descTextView.textSize = 12f
        descTextView.setTextColor(Color.GRAY)
        // Limit description to 2 lines
        descTextView.maxLines = 2
        descTextView.ellipsize = android.text.TextUtils.TruncateAt.END
        detailsLayout.addView(descTextView)

        cartItemLayout.addView(detailsLayout)

        // Remove button container for proper sizing
        val buttonContainer = LinearLayout(this)
        buttonContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        buttonContainer.gravity = android.view.Gravity.CENTER

        // Remove button
        val removeButton = ImageButton(this)
        removeButton.setImageResource(R.drawable.trash)
        // Set a fixed size for the button
        val buttonSize = 40 // in dp
        val density = resources.displayMetrics.density
        val buttonSizePx = (buttonSize * density).toInt()

        removeButton.layoutParams = LinearLayout.LayoutParams(
            buttonSizePx,
            buttonSizePx
        )
        removeButton.background = null
        removeButton.scaleType = ImageView.ScaleType.FIT_CENTER
        // Add padding to reduce the icon size within the button
        removeButton.setPadding(8, 8, 8, 8)

        removeButton.setOnClickListener {
            product._id?.let { id -> cartManager.removeFromCart(id) }
            loadCartItems()
            updateSummary()
            Toast.makeText(this, "${product.name} removed from cart", Toast.LENGTH_SHORT).show()
        }

        buttonContainer.addView(removeButton)
        cartItemLayout.addView(buttonContainer)

        return cartItemLayout
    }

    private fun updateSummary() {
        val formatter = DecimalFormat("$#,##0.00")
        val subtotal = cartManager.getCartTotal()
        val tax = subtotal * 0.07
        val total = subtotal + tax

        subtotalValue.text = formatter.format(subtotal)
        taxValue.text = formatter.format(tax)
        totalValue.text = formatter.format(total)
    }

    override fun onResume() {
        super.onResume()
        loadCartItems()
        updateSummary()
    }
}