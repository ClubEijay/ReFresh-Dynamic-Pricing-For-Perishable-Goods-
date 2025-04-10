package com.example.refresh

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

class SignUp : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        val name = findViewById<EditText>(R.id.signupName)
        val email = findViewById<EditText>(R.id.signupEmail)
        val password = findViewById<EditText>(R.id.signupPassword)
        val confirmPass = findViewById<EditText>(R.id.signupConfirmPass)
        val registerBtn = findViewById<ImageButton>(R.id.signupRegbtn)

        registerBtn.setOnClickListener {
            val nameText = name.text.toString().trim()
            val emailText = email.text.toString().trim()
            val passText = password.text.toString().trim()
            val confirmText = confirmPass.text.toString().trim()

            // Basic validation
            if (nameText.isEmpty()) {
                name.error = "Name is required"
                return@setOnClickListener
            }

            if (emailText.isEmpty()) {
                email.error = "Email is required"
                return@setOnClickListener
            }

            if (passText.isEmpty()) {
                password.error = "Password is required"
                return@setOnClickListener
            }

            if (confirmText.isEmpty()) {
                confirmPass.error = "Please confirm your password"
                return@setOnClickListener
            }

            if (passText != confirmText) {
                confirmPass.error = "Passwords do not match"
                return@setOnClickListener
            }

            // Success!
            Toast.makeText(this, "Your account is being registered", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@SignUp, LogIn::class.java)
            intent.putExtra("email", emailText)
            intent.putExtra("password", passText)
            startActivity(intent)
            finish()
        }

        val logInBtn = findViewById<ImageButton>(R.id.regSignBtn2)
        logInBtn.setOnClickListener {
            startActivity(Intent(this@SignUp, LogIn::class.java))
        }
    }
}
