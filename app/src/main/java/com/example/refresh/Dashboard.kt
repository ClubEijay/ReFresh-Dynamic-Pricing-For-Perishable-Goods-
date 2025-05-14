package com.example.refresh

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.widget.TextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import com.example.refresh.util.UserSessionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.refresh.model.Product
import com.example.refresh.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import java.io.ByteArrayOutputStream
import android.graphics.BitmapFactory
import androidx.activity.result.ActivityResultLauncher



class Dashboard : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sessionManager: UserSessionManager
    private val TAG = "DashboardActivity"
    private var selectedImageUri: Uri? = null
    private lateinit var loggedInUserEmail: String
    private lateinit var imagePickerLauncher: ActivityResultLauncher<String>
    private var currentImagePreview: ImageView? = null
    private var processedImageBase64: String? = null // Keep track of the processed image

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize the image picker launcher early
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                // Update the image preview if available
                currentImagePreview?.setImageURI(uri)

                // Process the image to Base64 right after selection
                processSelectedImage(uri)
            }
        }

        sessionManager = UserSessionManager(this)
        loggedInUserEmail = sessionManager.getUserDetails()[UserSessionManager.KEY_EMAIL] ?: "unknown@example.com"

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
                R.id.nav_sales_report -> {
                    Toast.makeText(this, "Sales Report clicked", Toast.LENGTH_SHORT).show()
                }

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

        val fabAddProduct = findViewById<FloatingActionButton>(R.id.fab_add_product)

        fabAddProduct.setOnClickListener {
            selectedImageUri = null
            processedImageBase64 = null // Reset the processed image

            val dialogView = layoutInflater.inflate(R.layout.dialog_add_product, null)
            val dialog = android.app.AlertDialog.Builder(this).setView(dialogView).create()

            val productName = dialogView.findViewById<EditText>(R.id.edit_product_name)
            val price = dialogView.findViewById<EditText>(R.id.edit_price)
            val expiryDate = dialogView.findViewById<EditText>(R.id.edit_expiry_date)
            val aisleSelector = dialogView.findViewById<RadioGroup>(R.id.aisle_selector)
            val addBtn = dialogView.findViewById<Button>(R.id.btn_add_product)
            val imagePreview = dialogView.findViewById<ImageView>(R.id.product_image_preview)
            val selectImageBtn = dialogView.findViewById<Button>(R.id.btn_select_image)

            // Set the current image preview reference
            currentImagePreview = imagePreview

            expiryDate.setOnClickListener {
                val calendar = Calendar.getInstance()
                DatePickerDialog(
                    this,
                    { _, year, month, dayOfMonth ->
                        val formattedDate = "${month + 1}/$dayOfMonth/$year"
                        expiryDate.setText(formattedDate)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            selectImageBtn.setOnClickListener {
                imagePickerLauncher.launch("image/*")
            }

            addBtn.setOnClickListener {
                val name = productName.text.toString()
                val priceVal = price.text.toString()
                val date = expiryDate.text.toString()

                if (name.isBlank() || priceVal.isBlank() || date.isBlank() || aisleSelector.checkedRadioButtonId == -1) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val selectedAisle = when (aisleSelector.checkedRadioButtonId) {
                    R.id.aisle1 -> "Aisle 1"
                    R.id.aisle2 -> "Aisle 2"
                    else -> "Unknown"
                }

                val priceDouble = priceVal.toDoubleOrNull()
                if (priceDouble == null) {
                    Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                if (selectedImageUri == null) {
                    Toast.makeText(this, "Please select an image for the product", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Create a product with all the data
                val product = Product(
                    name = name,
                    price = priceDouble,
                    description = "Exp: $date",
                    category = selectedAisle,
                    userEmail = loggedInUserEmail,
                    imageBase64 = processedImageBase64
                )

                Log.d(TAG, "Product created: ${product.name}, has image: ${product.imageBase64 != null}")

                // Save the product to database
                saveProductToDatabase(product)

                // Also display it locally
                addProductToLocalDisplay(product, selectedImageUri!!)

                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun processSelectedImage(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        val byteArray = outputStream.toByteArray()

        processedImageBase64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
        Log.d("ImageBase64", "Encoded image length: ${processedImageBase64?.length}")
    }


    private fun addProductToLocalDisplay(product: Product, imageUri: Uri) {
        val container = when (product.category) {
            "Aisle 1" -> findViewById<LinearLayout>(R.id.aisle1_container)
            "Aisle 2" -> findViewById<LinearLayout>(R.id.aisle2_container)
            else -> null
        }
        container?.let {
            // Add the image view
            val imageView = ImageView(this)
            imageView.setImageURI(imageUri)
            imageView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                400
            ).apply { setMargins(0, 8, 0, 8) }
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            it.addView(imageView)

            // Add the text view
            val productView = TextView(this)
            productView.text = "${product.name}\nPrice: $${product.price}\n${product.description}"
            productView.setPadding(16, 16, 16, 16)
            productView.setTextColor(Color.WHITE)
            productView.setBackgroundColor(Color.parseColor("#2C2C2C"))
            productView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 8, 0, 8) }
            it.addView(productView)
        }
    }

    private fun saveProductToDatabase(product: Product) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Saving product to database: ${product.name}, image: ${product.imageBase64?.take(20)}...")
                val response = RetrofitClient.api.addProduct(product)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@Dashboard, "Product saved!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@Dashboard, "Failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving product: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Dashboard, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAndDisplayUserProducts()
    }

    private fun fetchAndDisplayUserProducts() {
        val aisle1 = findViewById<LinearLayout>(R.id.aisle1_container)
        val aisle2 = findViewById<LinearLayout>(R.id.aisle2_container)
        aisle1.removeAllViews()
        aisle2.removeAllViews()

        val encodedEmail = java.net.URLEncoder.encode(loggedInUserEmail, "UTF-8")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.api.getUserProducts(encodedEmail)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val products = response.body() ?: emptyList()
                        if (products.isEmpty()) {
                            Toast.makeText(this@Dashboard, "No products found", Toast.LENGTH_SHORT).show()
                        } else {
                            displayProducts(products)
                            Toast.makeText(this@Dashboard, "${products.size} products loaded", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@Dashboard, "Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@Dashboard, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
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
                        ).apply { setMargins(0, 8, 0, 8) }
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        it.addView(imageView)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error displaying image: ${e.message}")
                    }
                }

                // Add the text view
                val textView = TextView(this)
                textView.text = "${product.name}\nPrice: $${product.price}\n${product.description}"
                textView.setPadding(16, 16, 16, 16)
                textView.setTextColor(Color.WHITE)
                textView.setBackgroundColor(Color.parseColor("#2C2C2C"))
                textView.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 8, 0, 8) }

                it.addView(textView)
            }
        }
    }

}