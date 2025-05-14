package com.example.refresh

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.refresh.model.Product
import com.example.refresh.network.RetrofitClient
import com.example.refresh.util.UserSessionManager
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CustomerDashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sessionManager: UserSessionManager
    private val TAG = "CustomerDashboardActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_dashboard)

        sessionManager = UserSessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LogIn::class.java))
            finish()
            return
        }

        drawerLayout = findViewById(R.id.drawerLayout)
        val navBtn: ImageButton = findViewById(R.id.navbtn)
        val navigationView: NavigationView = findViewById(R.id.nav_view)

        navBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Set up navigation drawer header
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

        // Set up navigation drawer item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
                }
                R.id.nav_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    startActivity(intent)
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

        // Fetch all products to display
        fetchAndDisplayAllProducts()
    }

    private fun fetchAndDisplayAllProducts() {
        val aisle1 = findViewById<LinearLayout>(R.id.aisle1_container)
        val aisle2 = findViewById<LinearLayout>(R.id.aisle2_container)
        aisle1.removeAllViews()
        aisle2.removeAllViews()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use the debug endpoint which requires no parameters
                val response = RetrofitClient.api.getAllProductsDebug()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        // Handle successful response
                        val debugResponse = response.body()
                        if (debugResponse != null && debugResponse.products != null) {
                            val products = debugResponse.products
                            if (products.isEmpty()) {
                                Toast.makeText(this@CustomerDashboard, "No products found", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                displayProducts(products)
                                Toast.makeText(
                                    this@CustomerDashboard,
                                    "${products.size} products loaded",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@CustomerDashboard,
                                "No products data found in response",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // Handle error response
                        Toast.makeText(
                            this@CustomerDashboard,
                            "Error: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Fallback to get all products with empty email parameter
                        fetchProductsWithEmptyEmail()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e(TAG, "Error fetching products: ${e.message}", e)
                    Toast.makeText(
                        this@CustomerDashboard,
                        "Network error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Try fallback method
                    fetchProductsWithEmptyEmail()
                }
            }
        }
    }

    private fun fetchProductsWithEmptyEmail() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Try the query parameter method with empty email
                val response = RetrofitClient.api.getAllProducts("")

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val products = response.body() ?: emptyList()
                        if (products.isEmpty()) {
                            Toast.makeText(this@CustomerDashboard, "No products found", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            displayProducts(products)
                            Toast.makeText(
                                this@CustomerDashboard,
                                "${products.size} products loaded",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@CustomerDashboard,
                            "Fallback also failed: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@CustomerDashboard,
                        "Fallback network error: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun displayProducts(products: List<Product>) {
        val aisle1 = findViewById<LinearLayout>(R.id.aisle1_container)
        val aisle2 = findViewById<LinearLayout>(R.id.aisle2_container)

        // Loop through each product and display them
        for (product in products) {
            val container = when (product.category) {
                "Aisle 1" -> aisle1
                "Aisle 2" -> aisle2
                else -> null
            }

            container?.let {
                // Create a product card layout
                val productCard = LinearLayout(this)
                productCard.orientation = LinearLayout.VERTICAL
                productCard.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 16, 0, 16) }
                productCard.setBackgroundColor(Color.parseColor("#222222"))
                productCard.setPadding(0, 0, 0, 16)

                // Add the image view if there's an image available
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

                // Add the product details
                val productInfo = LinearLayout(this)
                productInfo.orientation = LinearLayout.VERTICAL
                productInfo.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                productInfo.setPadding(16, 16, 16, 16)

                // Product name
                val nameTextView = TextView(this)
                nameTextView.text = product.name
                nameTextView.textSize = 18f
                nameTextView.setTextColor(Color.WHITE)
                nameTextView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 8) }
                productInfo.addView(nameTextView)

                // Product price
                val priceTextView = TextView(this)
                priceTextView.text = "Price: $${product.price}"
                priceTextView.textSize = 16f
                priceTextView.setTextColor(Color.WHITE)
                priceTextView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, 8) }
                productInfo.addView(priceTextView)

                // Product description (expiry date)
                val descTextView = TextView(this)
                descTextView.text = product.description
                descTextView.textSize = 14f
                descTextView.setTextColor(Color.LTGRAY)
                descTextView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                productInfo.addView(descTextView)

                // Add "Added by Vendor" info
                val vendorTextView = TextView(this)
                vendorTextView.text = "Vendor: ${product.userEmail}"
                vendorTextView.textSize = 12f
                vendorTextView.setTextColor(Color.GRAY)
                vendorTextView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 8, 0, 0) }
                productInfo.addView(vendorTextView)

                // Add all views to the product card
                productCard.addView(productInfo)

                // Add the product card to the container
                it.addView(productCard)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAndDisplayAllProducts()
    }
}