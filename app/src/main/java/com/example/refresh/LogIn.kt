package com.example.refresh

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.refresh.model.LoginRequest
import com.example.refresh.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



class LogIn : Activity() {

    private val TAG = "LogInActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        val emailEditText = findViewById<EditText>(R.id.signinEmail)
        val passwordEditText = findViewById<EditText>(R.id.signinPassword)
        val loginBtn = findViewById<ImageButton>(R.id.signinbtnofficial)
        val goToSignUpBtn = findViewById<ImageButton>(R.id.regSignUpBtn2)

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
                        if (response.isSuccessful) {
                            Toast.makeText(this@LogIn, "Login successful!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@LogIn, Dashboard::class.java))
                        } else {
                            Toast.makeText(this@LogIn, "Login failed: Invalid credentials", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Login failed", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LogIn, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        goToSignUpBtn.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }
    }
}
