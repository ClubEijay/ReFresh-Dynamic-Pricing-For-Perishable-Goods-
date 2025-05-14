package com.example.refresh

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.refresh.model.LoginRequest
import com.example.refresh.network.ApiService
import com.example.refresh.util.UserSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



class LogIn : Activity() {

    private val TAG = "LogInActivity"
    // Hardcoded credentials for quick login
    private val HARDCODED_EMAIL = "test@example.com"
    private val HARDCODED_PASSWORD = "password123"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)


        val emailEditText = findViewById<EditText>(R.id.signinEmail)
        val passwordEditText = findViewById<EditText>(R.id.signinPassword)
        val loginBtn = findViewById<ImageButton>(R.id.signinbtnofficial)
        val goToSignUpBtn = findViewById<ImageButton>(R.id.regSignUpBtn2)

        // Create session manager
        val sessionManager = UserSessionManager(this)

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            // User is already logged in, redirect to Dashboard
            val intent = Intent(this@LogIn, Dashboard::class.java)
            startActivity(intent)
            finish()
        }

        // Receive data from SignUp if passed
        val emailFromSignup = intent.getStringExtra("email")
        val passFromSignup = intent.getStringExtra("password")

        if (!emailFromSignup.isNullOrEmpty() && !passFromSignup.isNullOrEmpty()) {
            emailEditText.setText(emailFromSignup)
            passwordEditText.setText(passFromSignup)
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        loginBtn.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val loginRequest = LoginRequest(email, password)
                    val response = apiService.login(loginRequest)

                    withContext(Dispatchers.Main) {
                        val responseBody = response.body()
                        Log.d(TAG, "Login response: $responseBody")

                        if (response.isSuccessful && responseBody != null && responseBody.success == true) {
                            // Save user data in session manager
                            sessionManager.createLoginSession(
                                responseBody.name ?: "User",
                                email
                            )

                            Toast.makeText(this@LogIn, "Login successful!", Toast.LENGTH_SHORT).show()

                            // Create explicit intent to Dashboard
                            val intent = Intent(this@LogIn, Dashboard::class.java)
                            startActivity(intent)
                            finish() // This will close the Login activity
                        }
                        else {
                            handleHardcodedLogin(email)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Login error", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@LogIn,
                            "Network error: ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        // Make sure this is OUTSIDE the loginBtn click listener
        goToSignUpBtn.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

    }
    private fun handleHardcodedLogin(email: String) {
        // Create session for the hardcoded user
        val sessionManager = UserSessionManager(this)
        sessionManager.createLoginSession("Test User", email)

        Toast.makeText(this, "Test login successful!", Toast.LENGTH_SHORT).show()

        // Redirect to Customer Dashboard instead of regular Dashboard
        val intent = Intent(this, CustomerDashboard::class.java)
        startActivity(intent)
        finish()
    }
}