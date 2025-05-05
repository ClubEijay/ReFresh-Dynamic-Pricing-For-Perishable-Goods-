package com.example.refresh

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.*
import android.util.Log
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.*
import com.google.firebase.auth.GoogleAuthProvider.getCredential

class SignUp : Activity() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth

    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

       firebaseAuth = FirebaseAuth.getInstance()


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        googleSignInClient.signOut()
        val googleLayout = findViewById<LinearLayout>(R.id.googleSignUpLayout)
        googleLayout.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

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


            val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString("email", emailText)
            editor.putString("password", passText)
            editor.putString("name", nameText)
            editor.apply()

            Toast.makeText(this, "Your account is being registered", Toast.LENGTH_SHORT).show()

            val intent = Intent(this@SignUp, LogIn::class.java)
            startActivity(intent)
            finish()
        }

        val logInBtn = findViewById<ImageButton>(R.id.regSignBtn2)
        logInBtn.setOnClickListener {
            startActivity(Intent(this@SignUp, LogIn::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("GOOGLE_SIGN_IN", "Google sign in failed", e)
                Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        // Sign in with the Google credential
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser

                    if (task.result?.additionalUserInfo?.isNewUser == true) {

                        Toast.makeText(this, "Welcome, ${user?.displayName}!", Toast.LENGTH_SHORT)
                            .show()
                    } else {

                        Toast.makeText(
                            this,
                            "Welcome back, ${user?.displayName}!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }


                    val intent = Intent(this, Dashboard::class.java)
                    intent.putExtra("USERNAME", user?.displayName)
                    intent.putExtra("EMAIL", user?.email)
                    startActivity(intent)
                    finish()
                } else {

                    Toast.makeText(
                        this,
                        "Authentication failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
