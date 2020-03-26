package com.company.logon_ka_maseeha

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*

class Login : AppCompatActivity() {

    companion object {
        val db = Firebase.firestore
        private const val TAG = "DocSnippets"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login.setOnClickListener {
            getData()
        }
    }

    private fun getData() {
        val email = emailId.text
        val pass = password.text
        Log.i(TAG, "$email $pass")

        val docRef = db.collection("Users").document(email.toString())

        docRef.get()
            .addOnSuccessListener {
                    docs ->
                if (docs.exists()) {
                    Log.i(TAG, "User exists!")
                    val intent = Intent(this, UserPage::class.java)
                    startActivity(intent)
                } else {
                    Log.i(TAG, "User does not exists, kindly register first")
                    val intent = Intent(this, SignUp::class.java)
                    startActivity(intent)
                }
            }
    }
}
