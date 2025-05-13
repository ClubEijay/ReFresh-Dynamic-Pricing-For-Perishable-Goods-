package com.example.refresh

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast

class LogIn : Activity() {
    private var registeredEmail: String? = null
    private var registeredPassword: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        registeredEmail = intent.getStringExtra("email")
        registeredPassword = intent.getStringExtra("password")

        val emailInput = findViewById<EditText>(R.id.signinEmail)
        val passwordInput = findViewById<EditText>(R.id.signinPassword)
        val signInBtn = findViewById<ImageButton>(R.id.signinbtnofficial)
        var fullname = ""

        intent?.let{
            it.getStringExtra("email")?.let{ username ->
                emailInput.setText(username)
            }
            it.getStringExtra("password")?.let{ password ->
                passwordInput.setText(password)
            }
            it.getStringExtra("fullname")?.let{ fname ->
                fullname = fname
            }

        }
        signInBtn.setOnClickListener {
            val inputEmail = emailInput.text.toString().trim()
            val inputPassword = passwordInput.text.toString().trim()

            if (inputEmail == registeredEmail && inputPassword == registeredPassword) {
                Toast.makeText(this, "Welcome to your account", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LogIn, Dashboard::class.java).apply{
                    putExtra("username", emailInput.text.toString())
                    putExtra("password", passwordInput.text.toString())
                    putExtra("fullname", fullname)
                })

                finish()
            } else {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
            }
        }

        val signUpBtn = findViewById<ImageButton>(R.id.regSignUpBtn2)
        val signUpTxt = findViewById<TextView>(R.id.SignUptxtbtn)

        signUpBtn.setOnClickListener {
            startActivity(Intent(this@LogIn, SignUp::class.java))
        }

        signUpTxt.setOnClickListener {
            startActivity(Intent(this@LogIn, SignUp::class.java))
        }
    }
}
