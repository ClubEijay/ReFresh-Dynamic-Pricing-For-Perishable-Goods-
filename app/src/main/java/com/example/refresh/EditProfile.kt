package com.example.refresh

import android.app.Activity
import android.app.VoiceInteractor
import android.os.Bundle
import android.view.WindowInsetsAnimation
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.refresh.util.UserSessionManager
import okhttp3.Call
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException
import okhttp3.Response
import okhttp3.Request
import okhttp3.Callback



class EditProfile : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val sessionManager = UserSessionManager(this)
        val userData = sessionManager.getUserDetails()

        val nameEditText: EditText = findViewById(R.id.edit_profile_name)
        val emailEditText: EditText = findViewById(R.id.edit_gmail_name)
        val saveButton: ImageButton = findViewById(R.id.save_button)

        val originalEmail = userData[UserSessionManager.KEY_EMAIL]

        nameEditText.setText(userData[UserSessionManager.KEY_NAME])
        emailEditText.setText(originalEmail)

        saveButton.setOnClickListener {
            val updatedName = nameEditText.text.toString().trim()
            val updatedEmail = emailEditText.text.toString().trim()

            if (updatedName.isEmpty() || updatedEmail.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val client = OkHttpClient()
            val url = "http://10.0.2.2:5000/update-profile"

            val json = JSONObject()
            json.put("name", updatedName)
            json.put("email", updatedEmail)
            json.put("originalEmail", originalEmail)

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
                        Toast.makeText(
                            applicationContext,
                            "Network error occurred",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        if (response.isSuccessful) {
                            // Optionally update session data if email or name changed
                            sessionManager.createLoginSession(updatedName, updatedEmail)

                            Toast.makeText(
                                applicationContext,
                                "Profile updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                applicationContext,
                                "Failed to update profile",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            })
        }
    }
}