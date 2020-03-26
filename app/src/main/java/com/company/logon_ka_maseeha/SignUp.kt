package com.company.logon_ka_maseeha

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUp : AppCompatActivity() {

    companion object {
        val db = Firebase.firestore
        private const val TAG = "DocSnippets"
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        sign_up.setOnClickListener {
            getData()
        }
    }

    private fun getData() {
        val user = username.text
        val email = sign_in_emailId.text
        val password = sign_in_password.text

        val docRef = db.collection("Users").document(email.toString())

        docRef.get()
            .addOnSuccessListener {
                    docs ->
                if (docs.exists()) {
                    Log.i(TAG, "User exists, kindly sign in!")
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                } else {
                    Log.i(TAG, "User signed in")

                    val userData = hashMapOf (
                        "Name" to user.toString(),
                        "Email" to email.toString(),
                        "Password" to password.toString()
                    )

                    docRef.set(userData)
                        .addOnSuccessListener {
                            Log.i(TAG,"User Added!")
                            Toast.makeText(this@SignUp, "$user Signed Up", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, Login::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener {
                                exception -> Log.i(TAG, "Error: ", exception)
                        }
                }
            }
    }
}