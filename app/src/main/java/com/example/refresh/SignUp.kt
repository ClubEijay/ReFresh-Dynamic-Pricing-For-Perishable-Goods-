package com.example.refresh

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.*
import com.example.refresh.model.User
import com.example.refresh.network.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class SignUp : Activity() {

    private val TAG = "SignUpActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val name = findViewById<EditText>(R.id.signupName)
        val email = findViewById<EditText>(R.id.signupEmail)
        val password = findViewById<EditText>(R.id.signupPassword)
        val confirmPass = findViewById<EditText>(R.id.signupConfirmPass)
        val registerBtn = findViewById<ImageButton>(R.id.signupRegbtn)
        val showPassword = findViewById<CheckBox>(R.id.signupShowPassword)
        val showConfirmPassword = findViewById<CheckBox>(R.id.signupShowConfirmPassword)
        val passwordStrengthText = findViewById<TextView>(R.id.passwordStrengthText)

        showPassword.setOnCheckedChangeListener { _, isChecked ->
            password.transformationMethod = if (isChecked) {
                HideReturnsTransformationMethod.getInstance()
            } else {
                PasswordTransformationMethod.getInstance()
            }
            password.setSelection(password.text.length)
        }

        showConfirmPassword.setOnCheckedChangeListener { _, isChecked ->
            confirmPass.transformationMethod = if (isChecked) {
                HideReturnsTransformationMethod.getInstance()
            } else {
                PasswordTransformationMethod.getInstance()
            }
            confirmPass.setSelection(confirmPass.text.length)
        }

        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000/") // Emulator localhost
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(ApiService::class.java)

        registerBtn.setOnClickListener {
            val nameText = name.text.toString().trim()
            val emailText = email.text.toString().trim()
            val passText = password.text.toString().trim()
            val confirmText = confirmPass.text.toString().trim()

            if (nameText.isEmpty() || emailText.isEmpty() || passText.isEmpty() || confirmText.isEmpty()) {
                Toast.makeText(this, "Please complete all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passText != confirmText) {
                confirmPass.error = "Passwords do not match"
                return@setOnClickListener
            }

            registerBtn.isEnabled = false

            val user = User(nameText, emailText, passText)

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = apiService.signUp(user)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@SignUp, "Registration successful!", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this@SignUp, BeforeRegister::class.java).apply {
                                putExtra("email", emailText)
                                putExtra("password", passText)
                            }
                            startActivity(intent)
                            finish()
                        } else {
                            val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                            Toast.makeText(this@SignUp, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                            registerBtn.isEnabled = true
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Sign-up failed", e)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@SignUp, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                        registerBtn.isEnabled = true
                    }
                }
            }
        }

        findViewById<ImageButton>(R.id.regSignBtn2).setOnClickListener {
            startActivity(Intent(this, LogIn::class.java))
        }
    }
}
