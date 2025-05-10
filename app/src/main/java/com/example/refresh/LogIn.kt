package com.example.refresh

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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
    private lateinit var sessionManager: UserSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        // Initialize session manager
        sessionManager = UserSessionManager(this)

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            // User is already logged in, go to Dashboard
            startActivity(Intent(this, Dashboard::class.java))
            finish()
            return
        }

        val emailInput = findViewById<EditText>(R.id.signinEmail)
        val passwordInput = findViewById<EditText>(R.id.signinPassword)
        val signInBtn = findViewById<ImageButton>(R.id.signinbtnofficial)

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000/") // Emulator access to localhost
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        signInBtn.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signInBtn.isEnabled = false

            val loginRequest = LoginRequest(email, password)

            Log.d(TAG, "Attempting login with email: $email")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = apiService.login(loginRequest)
                    Log.d(TAG, "Response code: ${response.code()}")

                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val loginResponse = response.body()
                            Log.d(TAG, "Response body: $loginResponse")

                            if (loginResponse?.success == true) {
                                // Save session data
                                loginResponse.name?.let { name ->
                                    sessionManager.createLoginSession(name, email)
                                }

                                Toast.makeText(this@LogIn, "Login successful!", Toast.LENGTH_SHORT).show()

                                // Go to Dashboard - no need to pass data through Intent anymore
                                startActivity(Intent(this@LogIn, Dashboard::class.java))
                                finish()
                            } else {
                                Toast.makeText(this@LogIn, loginResponse?.message ?: "Unknown error", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val errorMsg = response.errorBody()?.string() ?: "Invalid email or password"
                            Log.e(TAG, "Login failed: $errorMsg")
                            Toast.makeText(this@LogIn, "Login failed: Invalid email or password", Toast.LENGTH_SHORT).show()
                        }
                        signInBtn.isEnabled = true
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Login failed with exception", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LogIn, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                        signInBtn.isEnabled = true
                    }
                }
            }
        }

        findViewById<ImageButton>(R.id.regSignUpBtn2).setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        findViewById<TextView>(R.id.SignUptxtbtn).setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }
    }
}