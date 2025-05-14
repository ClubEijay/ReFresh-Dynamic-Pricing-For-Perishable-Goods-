package com.example.refresh

import android.app.Activity
import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.refresh.util.UserSessionManager

class EditProfile : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val sessionManager = UserSessionManager(this)
        val userData = sessionManager.getUserDetails()

        val nameEditText: EditText = findViewById(R.id.edit_profile_name)
        val emailEditText: EditText = findViewById(R.id.edit_gmail_name)

        nameEditText.setText(userData[UserSessionManager.KEY_NAME])
        emailEditText.setText(userData[UserSessionManager.KEY_EMAIL])
        }
    }
