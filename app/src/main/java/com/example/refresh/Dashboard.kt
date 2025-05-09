package com.example.refresh

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import android.widget.TextView
import android.widget.ImageView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.bumptech.glide.Glide

class Dashboard : Activity() {
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        //Push Test Push Test!
        drawerLayout = findViewById(R.id.drawerLayout)
        val navBtn: ImageButton = findViewById(R.id.navbtn)
        val navigationView: NavigationView = findViewById(R.id.nav_view)

        // 👉 Set up Navigation Drawer open behavior
        navBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // 👉 Populate the header with Google account info
        val headerView = navigationView.getHeaderView(0)
        val nameTextView: TextView = headerView.findViewById(R.id.profile_name)
        val emailTextView: TextView = headerView.findViewById(R.id.profile_email)
        val profileImageView: ImageView = headerView.findViewById(R.id.profile_image)

        val account: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            nameTextView.text = account.displayName
            emailTextView.text = account.email

            val photoUrl = account.photoUrl
            if (photoUrl != null) {
                Glide.with(this)
                    .load(photoUrl)
                    .into(profileImageView)
            }
        }

        // 👉 Handle Navigation item clicks
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_sales_report -> {
                    Toast.makeText(this, "Sales Report clicked", Toast.LENGTH_SHORT).show()
                    // startActivity(Intent(this, SalesReportActivity::class.java))
                }

                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show()
                }

                R.id.nav_settings -> {
                    Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show()
                    val intent = Intent (this, SettingsActivity::class.java)
                    startActivity(intent)
                }

                R.id.nav_logout -> {
                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }
}
