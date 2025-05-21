package com.example.refresh

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.refresh.model.Product
import com.example.refresh.network.RetrofitClient
import com.example.refresh.util.CartManager
import com.example.refresh.util.UserSessionManager
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerDashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sessionManager: UserSessionManager
    private lateinit var cartManager: CartManager
    private val TAG = "CustomerDashboardActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_dashboard)

        sessionManager = UserSessionManager(this)
        cartManager = CartManager(this)

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LogIn::class.java))
            finish()
            return
        }

        drawerLayout = findViewById(R.id.drawerLayout)
        val navBtn: ImageButton = findViewById(R.id.navbtn)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val cartButton: ImageButton = findViewById(R.id.cart_btn)

        navBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        cartButton.setOnClickListener {
            val intent = Intent(this, CartActivity::class.java)
            startActivity(intent)
        }

        val headerView = navigationView.getHeaderView(0)
        val nameTextView: TextView = headerView.findViewById(R.id.profile_name)
        val emailTextView: TextView = headerView.findViewById(R.id.profile_email)
        val profileImageView: ImageView = headerView.findViewById(R.id.profile_image)

        val userData = sessionManager.getUserDetails()
        val name = userData[UserSessionManager.KEY_NAME]
        val email = userData[UserSessionManager.KEY_EMAIL]

        Log.d(TAG, "Setting profile data: $name, $email")

        nameTextView.text = name
        emailTextView.text = email
        profileImageView.setImageResource(R.drawable.profile_img)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                }
                R.id.nav_logout -> {
                    sessionManager.logoutUser()
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LogIn::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        fetchAndDisplayAllProducts()
    }

    private fun fetchAndDisplayAllProducts() {
        val aisle1 = findViewById<LinearLayout>(R.id.aisle1_container)
        val aisle2 = findViewById<LinearLayout>(R.id.aisle2_container)
        aisle1.removeAllViews()
        aisle2.removeAllViews()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getAllProductsDebug()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val debugResponse = response.body()
                        if (debugResponse != null && debugResponse.products != null) {
                            val products = debugResponse.products
                            if (products.isEmpty()) {
                                Toast.makeText(this@CustomerDashboard, "No products found", Toast.LENGTH_SHORT).show()
                            } else {
                                displayProducts(products)
                            }
                        } else {
                            Toast.makeText(this@CustomerDashboard, "No products data found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        fetchProductsWithEmptyEmail()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Error fetching products: ${e.message}", e)
                    fetchProductsWithEmptyEmail()
                }
            }
        }
    }

    private fun fetchProductsWithEmptyEmail() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getAllProducts("")
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val products = response.body() ?: emptyList()
                        displayProducts(products)
                    } else {
                        Toast.makeText(this@CustomerDashboard, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CustomerDashboard, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayProducts(products: List<Product>) {
        val aisle1 = findViewById<LinearLayout>(R.id.aisle1_container)
        val aisle2 = findViewById<LinearLayout>(R.id.aisle2_container)

        for (product in products) {
            val container = when (product.category) {
                "Aisle 1" -> aisle1
                "Aisle 2" -> aisle2
                else -> null
            }

            container?.let {
                val productCard = LinearLayout(this)
                productCard.orientation = LinearLayout.VERTICAL
                productCard.setBackgroundColor(Color.parseColor("#222222"))
                productCard.setPadding(0, 0, 0, 16)
                productCard.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 16, 0, 16) }

                // Product Image
                product.imageBase64?.let { base64 ->
                    try {
                        val imageView = ImageView(this)
                        val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        imageView.setImageBitmap(bitmap)
                        imageView.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            400
                        )
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        productCard.addView(imageView)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error displaying image: ${e.message}")
                    }
                }

                // Product Info
                val productInfo = LinearLayout(this)
                productInfo.orientation = LinearLayout.VERTICAL
                productInfo.setPadding(16, 16, 16, 16)

                // Fix: Display product name instead of toString()
                val nameTextView = TextView(this).apply {
                    text = product.name
                    textSize = 18f
                    setTextColor(Color.WHITE)
                }
                val priceTextView = TextView(this).apply {
                    // Format price correctly without scientific notation
                    text = "Price: $${String.format("%.2f", product.price)}"
                    textSize = 16f
                    setTextColor(Color.WHITE)
                }
                val descTextView = TextView(this).apply {
                    text = product.description
                    textSize = 14f
                    setTextColor(Color.LTGRAY)
                }
                val vendorTextView = TextView(this).apply {
                    text = "Vendor: ${product.userEmail}"
                    textSize = 12f
                    setTextColor(Color.GRAY)
                }

                val addToCartBtn = Button(this).apply {
                    text = "Add to Cart"
                    setBackgroundColor(Color.parseColor("#FF9800"))
                    setTextColor(Color.WHITE)
                    setOnClickListener {
                        addToCart(product)
                    }
                }

                productInfo.addView(nameTextView)
                productInfo.addView(priceTextView)
                productInfo.addView(descTextView)
                productInfo.addView(vendorTextView)
                productInfo.addView(addToCartBtn)

                productCard.addView(productInfo)
                it.addView(productCard)
            }
        }
    }

    private fun addToCart(product: Product) {
        // Add the product to cart using CartManager
        cartManager.addToCart(product)

        // Show a notification
        Toast.makeText(this, "${product.name} added to cart!", Toast.LENGTH_SHORT).show()

        // Optional: Update cart badge or counter if you have one
        updateCartBadge()
    }

    private fun updateCartBadge() {
        // If you have a cart badge/counter, update it here
        // For example:
        // val cartSize = cartManager.getCartItems().size
        // cartBadgeTextView.text = if (cartSize > 0) cartSize.toString() else ""
        // cartBadgeTextView.visibility = if (cartSize > 0) View.VISIBLE else View.GONE
    }

    override fun onResume() {
        super.onResume()
        fetchAndDisplayAllProducts()
    }
}