package com.example.refresh

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, GetStartedActivity::class.java)
            startActivity(intent)
            finish() // Optional: closes the splash screen so user can't go back to it
        }, 3000) // 3000ms = 3 seconds
    }
}
