package com.example.refresh

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.refresh.model.Product
import com.example.refresh.network.RetrofitClient
import com.example.refresh.util.UserSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Calendar

class EditProductActivity : AppCompatActivity() {

    private val TAG = "EditProductActivity"
    private lateinit var productId: String
    private lateinit var productName: EditText
    private lateinit var productPrice: EditText
    private lateinit var expiryDate: EditText
    private lateinit var aisleGroup: RadioGroup
    private lateinit var aisle1: RadioButton
    private lateinit var aisle2: RadioButton
    private lateinit var productImagePreview: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var updateButton: Button
    private lateinit var sessionManager: UserSessionManager

    private var selectedImageUri: Uri? = null
    private var originalImageBase64: String? = null
    private var newImageBase64: String? = null

    // Register for activity result to handle image picking
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            productImagePreview.setImageURI(uri)
            processSelectedImage(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_product)

        sessionManager = UserSessionManager(this)

        // Initialize views
        productId = intent.getStringExtra("PRODUCT_ID") ?: ""
        productName = findViewById(R.id.edit_product_name)
        productPrice = findViewById(R.id.edit_price)
        expiryDate = findViewById(R.id.edit_expiry_date)
        productImagePreview = findViewById(R.id.product_image_preview)
        selectImageButton = findViewById(R.id.btn_select_image)
        updateButton = findViewById(R.id.btn_update_product)
        aisleGroup = findViewById(R.id.aisle_selector)
        aisle1 = findViewById(R.id.aisle1)
        aisle2 = findViewById(R.id.aisle2)

        // Populate fields with data from intent
        populateProductData()

        // Setup date picker for expiry date
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

        selectImageButton.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        updateButton.setOnClickListener {
            updateProductData()
        }
    }

    private fun populateProductData() {
        productName.setText(intent.getStringExtra("PRODUCT_NAME") ?: "")
        productPrice.setText(intent.getStringExtra("PRODUCT_PRICE") ?: "0.0")
        expiryDate.setText(intent.getStringExtra("PRODUCT_DESCRIPTION") ?: "")

        // Set the correct aisle radio button
        when (intent.getStringExtra("PRODUCT_CATEGORY")) {
            "Aisle 1" -> aisle1.isChecked = true
            "Aisle 2" -> aisle2.isChecked = true
        }

        // Load image if available
        originalImageBase64 = intent.getStringExtra("PRODUCT_IMAGE")
        if (!originalImageBase64.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(originalImageBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                productImagePreview.setImageBitmap(bitmap)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading product image: ${e.message}")
            }
        }
    }

    private fun processSelectedImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val outputStream = ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
            val byteArray = outputStream.toByteArray()

            newImageBase64 = Base64.encodeToString(byteArray, Base64.NO_WRAP)
            Log.d(TAG, "New image encoded, length: ${newImageBase64?.length}")
        } catch (e: Exception) {
            Log.e(TAG, "Error processing image: ${e.message}")
            Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProductData() {
        if (productName.text.isBlank() || productPrice.text.isBlank() || expiryDate.text.isBlank()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedAisle = when (aisleGroup.checkedRadioButtonId) {
            R.id.aisle1 -> "Aisle 1"
            R.id.aisle2 -> "Aisle 2"
            else -> "Unknown"
        }

        val priceDouble = productPrice.text.toString().toDoubleOrNull()
        if (priceDouble == null) {
            Toast.makeText(this, "Invalid price format", Toast.LENGTH_SHORT).show()
            return
        }

        // Use the user email from session manager
        val userEmail = sessionManager.getUserDetails()[UserSessionManager.KEY_EMAIL] ?: "unknown@example.com"

        // Create updated product object
        val updatedProduct = Product(
            _id = productId, // Make sure to include the original ID
            name = productName.text.toString(),
            price = priceDouble,
            description = expiryDate.text.toString(),
            category = selectedAisle,
            userEmail = userEmail,
            imageBase64 = newImageBase64 ?: originalImageBase64 // Use new image if available, otherwise keep original
        )

        // Send update to server
        updateProductOnServer(updatedProduct)
    }

    private fun updateProductOnServer(product: Product) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Updating product with ID: ${product._id}")
                val response = RetrofitClient.api.updateProduct(product._id ?: "", product)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@EditProductActivity, "Product updated successfully", Toast.LENGTH_SHORT).show()
                        // Return to Dashboard
                        finish()
                    } else {
                        Toast.makeText(this@EditProductActivity, "Update failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating product: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditProductActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}