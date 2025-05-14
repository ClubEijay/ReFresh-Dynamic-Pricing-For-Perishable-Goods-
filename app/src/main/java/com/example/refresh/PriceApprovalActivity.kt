package com.example.refresh

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.refresh.model.Product
import com.example.refresh.network.RetrofitClient
import com.example.refresh.util.UserSessionManager
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PriceApprovalActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sessionManager: UserSessionManager
    private lateinit var pendingChangesRecyclerView: RecyclerView
    private lateinit var adapter: PriceApprovalAdapter
    private val TAG = "PriceApprovalActivity"
    private lateinit var loggedInUserEmail: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_price_approval)

        sessionManager = UserSessionManager(this)
        loggedInUserEmail = sessionManager.getUserDetails()[UserSessionManager.KEY_EMAIL] ?: "unknown@example.com"

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        drawerLayout = findViewById(R.id.drawerLayout)
        val navBtn: ImageButton = findViewById(R.id.navbtn)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        pendingChangesRecyclerView = findViewById(R.id.pendingChangesRecyclerView)
        val simulateNextDayButton: Button = findViewById(R.id.simulateNextDayButton)

        // Setup navigation drawer
        navBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        val headerView = navigationView.getHeaderView(0)
        val nameTextView: TextView = headerView.findViewById(R.id.profile_name)
        val emailTextView: TextView = headerView.findViewById(R.id.profile_email)
        val profileImageView: ImageView = headerView.findViewById(R.id.profile_image)

        val userData = sessionManager.getUserDetails()
        val name = userData[UserSessionManager.KEY_NAME]
        val email = userData[UserSessionManager.KEY_EMAIL]

        nameTextView.text = name
        emailTextView.text = email
        profileImageView.setImageResource(R.drawable.profile_img)

        // Setup recycler view
        adapter = PriceApprovalAdapter(mutableListOf(),
            onApprove = { product -> approvePrice(product) },
            onReject = { product -> rejectPrice(product) },
            onCustomPrice = { product, newPrice -> updateCustomPrice(product, newPrice) }
        )
        pendingChangesRecyclerView.layoutManager = LinearLayoutManager(this)
        pendingChangesRecyclerView.adapter = adapter

        // Setup simulate next day button
        simulateNextDayButton.setOnClickListener {
            simulateNextDay()
        }

        // Load pending price changes
        loadPendingPriceChanges()
    }

    private fun simulateNextDay() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.simulateNextDay()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@PriceApprovalActivity, "Next day simulated successfully", Toast.LENGTH_SHORT).show()
                        // Reload price changes
                        loadPendingPriceChanges()
                    } else {
                        Toast.makeText(this@PriceApprovalActivity, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error simulating next day: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PriceApprovalActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadPendingPriceChanges() {
        val encodedEmail = URLEncoder.encode(loggedInUserEmail, "UTF-8")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getPendingPriceChanges(encodedEmail)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val products = response.body() ?: emptyList()
                        adapter.updateProducts(products)
                        if (products.isEmpty()) {
                            Toast.makeText(this@PriceApprovalActivity, "No pending price changes", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@PriceApprovalActivity, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading pending price changes: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PriceApprovalActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun approvePrice(product: Product) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.approvePrice(product.id)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@PriceApprovalActivity, "Price approved!", Toast.LENGTH_SHORT).show()
                        // Remove from list
                        adapter.removeProduct(product)
                    } else {
                        Toast.makeText(this@PriceApprovalActivity, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error approving price: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PriceApprovalActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun rejectPrice(product: Product) {
        // Reset price to base price and approve
        val updatedProduct = product.copy(
            currentPrice = product.basePrice,
            isPriceApproved = true,
            daysOnShelf = 0, // Reset shelf time
            lastChecked = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
        )
        updateProduct(updatedProduct)
    }

    private fun updateCustomPrice(product: Product, newPrice: Double) {
        val updatedProduct = product.copy(
            currentPrice = newPrice,
            isPriceApproved = true,
            lastChecked = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).format(Date())
        )
        updateProduct(updatedProduct)
    }

    private fun updateProduct(product: Product) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.updateProduct(product.id, product)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@PriceApprovalActivity, "Product updated!", Toast.LENGTH_SHORT).show()
                        // Remove from list
                        adapter.removeProduct(product)
                    } else {
                        Toast.makeText(this@PriceApprovalActivity, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating product: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@PriceApprovalActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Adapter for the price approval RecyclerView
    inner class PriceApprovalAdapter(
        private val products: MutableList<Product>,
        private val onApprove: (Product) -> Unit,
        private val onReject: (Product) -> Unit,
        private val onCustomPrice: (Product, Double) -> Unit
    ) : RecyclerView.Adapter<PriceApprovalAdapter.ViewHolder>() {

        fun updateProducts(newProducts: List<Product>) {
            products.clear()
            products.addAll(newProducts)
            notifyDataSetChanged()
        }

        fun removeProduct(product: Product) {
            val position = products.indexOfFirst { it.id == product.id }
            if (position != -1) {
                products.removeAt(position)
                notifyItemRemoved(position)
            }
        }

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val productImageView: ImageView = itemView.findViewById(R.id.productImageView)
            val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)
            val productCategoryTextView: TextView = itemView.findViewById(R.id.productCategoryTextView)
            val productExpiryTextView: TextView = itemView.findViewById(R.id.productExpiryTextView)
            val daysOnShelfTextView: TextView = itemView.findViewById(R.id.daysOnShelfTextView)
            val previousPriceTextView: TextView = itemView.findViewById(R.id.previousPriceTextView)
            val newPriceTextView: TextView = itemView.findViewById(R.id.newPriceTextView)
            val btnApprove: Button = itemView.findViewById(R.id.btnApprove)
            val btnReject: Button = itemView.findViewById(R.id.btnReject)
            val customPriceEditText: EditText = itemView.findViewById(R.id.customPriceEditText)
            val btnSetCustomPrice: Button = itemView.findViewById(R.id.btnSetCustomPrice)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_price_approval, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val product = products[position]

            // Set product info
            holder.productNameTextView.text = product.name
            holder.productCategoryTextView.text = "Aisle: ${product.category}"
            holder.productExpiryTextView.text = product.description
            holder.daysOnShelfTextView.text = "Days on shelf: ${product.daysOnShelf}"
            holder.previousPriceTextView.text = "$${product.basePrice}"
            holder.newPriceTextView.text = "$${product.currentPrice}"

            // Set image if available
            product.imageBase64?.let { base64 ->
                try {
                    val imageBytes = Base64.decode(base64, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    holder.productImageView.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    Log.e(TAG, "Error displaying image: ${e.message}")
                    holder.productImageView.setImageResource(R.drawable.ic_launcher_foreground)
                }
            } ?: run {
                holder.productImageView.setImageResource(R.drawable.ic_launcher_foreground)
            }

            // Set button listeners
            holder.btnApprove.setOnClickListener {
                onApprove(product)
            }

            holder.btnReject.setOnClickListener {
                onReject(product)
            }

            holder.btnSetCustomPrice.setOnClickListener {
                val priceText = holder.customPriceEditText.text.toString()
                val customPrice = priceText.toDoubleOrNull()
                if (customPrice != null) {
                    onCustomPrice(product, customPrice)
                } else {
                    Toast.makeText(holder.itemView.context, "Please enter a valid price", Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun getItemCount(): Int = products.size
    }
} 