package com.company.logon_ka_maseeha

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_user_page.*

class UserPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_page)

        donate_item_btn.setOnClickListener {
            val intent = Intent(this, Donation::class.java)
            val email = intent.getStringExtra("email")
            intent.putExtra("email", email)
            startActivity(intent)
        }

        status_btn.setOnClickListener {
            val intent = Intent(this, StatusPage::class.java)
            val email = intent.getStringExtra("email")
            intent.putExtra("email", email)
            startActivity(intent)
        }
    }

}
