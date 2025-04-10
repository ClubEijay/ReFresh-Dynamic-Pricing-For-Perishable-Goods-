package com.example.refresh

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton

class GetStartedActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_started)

        val getStartedBtn = findViewById<ImageButton>(R.id.getstartedbtn)

        getStartedBtn.setOnClickListener {
            val intent = Intent(this@GetStartedActivity, GetStartedActivity2::class.java)
            startActivity(intent)
        }
    }
}
