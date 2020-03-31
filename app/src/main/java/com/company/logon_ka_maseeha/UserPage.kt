package com.company.logon_ka_maseeha

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_page.*

class UserPage : AppCompatActivity() {

    companion object {
        const val TAG = "DocSnippets"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_page)

        val sharedPreferences: SharedPreferences = getSharedPreferences("appSharedFile", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")
        val photoUrl = sharedPreferences.getString("photoUrl", "")
        val googleUserName = sharedPreferences.getString("username", "")

        Picasso.get().load(photoUrl).into(userDP)
        val userNameEle = userName as TextView
        userNameEle.text = googleUserName
        Log.i(TAG, "shared pref: $email")

        //val email = intent.getStringExtra("email")
        //Log.i(TAG, "email is $email")

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
