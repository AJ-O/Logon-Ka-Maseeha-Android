package com.company.logon_ka_maseeha

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_user_page.*

class UserPage : AppCompatActivity() {

    companion object {
        const val TAG = "DocSnippets"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_page)

        val email = intent.getStringExtra("email")
        Log.i(TAG, "email is $email")

        donate_item_btn.setOnClickListener {
            val intent = Intent(this, Donation::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }

        status_btn.setOnClickListener {
            val intent = Intent(this, StatusPage::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }
    }

}
