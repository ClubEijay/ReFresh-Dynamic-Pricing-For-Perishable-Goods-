package com.example.refresh

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
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
//import com.google.android.gms.auth.api.signin.GoogleSignIn
//import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.bumptech.glide.Glide
import com.example.refresh.util.UserSessionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.Calendar
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity


class   Dashboard : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var sessionManager: UserSessionManager
    private val TAG = "DashboardActivity"
    private var selectedImageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Initialize session manager
        sessionManager = UserSessionManager(this)

        // Check if user is logged in, if not redirect to login
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LogIn::class.java))
            finish()
            return
        }


        //Push Test Push Test!

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

        // Get user data from session
        val userData = sessionManager.getUserDetails()
        val name = userData[UserSessionManager.KEY_NAME]
        val email = userData[UserSessionManager.KEY_EMAIL]

        Log.d(TAG, "Setting profile data: $name, $email")

        // Update UI with user data from session
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
                    Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, SettingsActivity::class.java)


                    startActivity(intent)
                }
                R.id.nav_logout -> {
                    // Clear session data
                    sessionManager.logoutUser()

                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

                    // Return to login screen
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

        val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
            }
        }

        fabAddProduct.setOnClickListener {
            selectedImageUri = null // Reset any previously selected image

            val dialogView = layoutInflater.inflate(R.layout.dialog_add_product, null)
            val dialog = android.app.AlertDialog.Builder(this).setView(dialogView).create()

            val productName = dialogView.findViewById<EditText>(R.id.edit_product_name)
            val price = dialogView.findViewById<EditText>(R.id.edit_price)
            val expiryDate = dialogView.findViewById<EditText>(R.id.edit_expiry_date)
            val aisleSelector = dialogView.findViewById<RadioGroup>(R.id.aisle_selector)
            val addBtn = dialogView.findViewById<Button>(R.id.btn_add_product)
            val imagePreview = dialogView.findViewById<ImageView>(R.id.product_image_preview)
            val selectImageBtn = dialogView.findViewById<Button>(R.id.btn_select_image)

            // Expiry date picker
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

            // Select image logic
            selectImageBtn.setOnClickListener {
                imagePickerLauncher.launch("image/*")
            }

            // Add product logic
            addBtn.setOnClickListener {
                val name = productName.text.toString()
                val priceVal = price.text.toString()
                val date = expiryDate.text.toString()

                if (name.isBlank() || priceVal.isBlank() || date.isBlank() || aisleSelector.checkedRadioButtonId == -1) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val container = when (aisleSelector.checkedRadioButtonId) {
                    R.id.aisle1 -> findViewById<LinearLayout>(R.id.aisle1_container)
                    R.id.aisle2 -> findViewById<LinearLayout>(R.id.aisle2_container)
                    else -> null
                }

                container?.let {
                    // Add image if selected
                    selectedImageUri?.let { uri ->
                        val imageView = ImageView(this)
                        imageView.setImageURI(uri)
                        imageView.layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            400
                        ).apply { setMargins(0, 8, 0, 8) }
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        it.addView(imageView)
                    }

                    // Add product details
                    val productView = TextView(this)
                    productView.text = "$name\nPrice: $$priceVal\nExp: $date"
                    productView.setPadding(16, 16, 16, 16)
                    productView.setTextColor(Color.WHITE)
                    productView.setBackgroundColor(Color.parseColor("#2C2C2C"))
                    productView.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { setMargins(0, 8, 0, 8) }

                    it.addView(productView)
                }

                dialog.dismiss()
            }

            dialog.show()
        }



    }
}

