package com.example.refresh

import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.refresh.util.UserSessionManager
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class ChangePassword : AppCompatActivity() {

    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        val sessionManager = UserSessionManager(this)
        val userData = sessionManager.getUserDetails()
        val email = userData[UserSessionManager.KEY_EMAIL]

        val passwordEditText: EditText = findViewById(R.id.edit_password)
        val toggleVisibilityBtn: ImageButton = findViewById(R.id.toggle_password_visibility)
        val saveButton: ImageButton = findViewById(R.id.save_button)

        toggleVisibilityBtn.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                toggleVisibilityBtn.setImageResource(R.drawable.ic_visibility_on)
            } else {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                toggleVisibilityBtn.setImageResource(R.drawable.ic_visibility_off)
            }
            passwordEditText.setSelection(passwordEditText.text.length) // keep cursor at end
        }

        saveButton.setOnClickListener {
            val newPassword = passwordEditText.text.toString().trim()
            if (newPassword.isEmpty()) {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val client = OkHttpClient()
            val url = "http://10.0.2.2:5000/change-password"

            val json = JSONObject()
            json.put("email", email)
            json.put("password", newPassword)

            val requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                json.toString()
            )

            val request = Request.Builder()
                .url(url)
                .put(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Network error", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        if (response.isSuccessful) {
                            Toast.makeText(applicationContext, "Password updated successfully", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(applicationContext, "Failed to update password", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        }
    }
}
