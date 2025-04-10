package com.example.refresh

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton

class GetStartedActivity2 : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started2)

        val signInBtn = findViewById<ImageButton>(R.id.GsignInBtn)
        val signUpBtn = findViewById<ImageButton>(R.id.GsignUpBtn)

        signInBtn.setOnClickListener {
            val intent = Intent(this@GetStartedActivity2, LogIn::class.java)
            startActivity(intent)
        }

        signUpBtn.setOnClickListener {
            val intent = Intent(this@GetStartedActivity2, SignUp::class.java)
            startActivity(intent)
        }
    }
}
