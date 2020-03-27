package com.company.logon_ka_maseeha

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.security.MessageDigest
import java.util.*


class SignUp : AppCompatActivity() {

    companion object {
        val db = Firebase.firestore
        private const val TAG = "DocSnippets"
        val HEX_CHARS = "0123456789ABCDEF".toCharArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        sign_up.setOnClickListener {

            val user = username.toString()
            val email = sign_in_emailId.toString()
            val pass = password.toString()

            if(user.isBlank() or email.isBlank() or pass.isBlank()){
                Toast.makeText(this, "Kindly enter the require data", Toast.LENGTH_LONG).show()
            } else {
                getData(user, email, pass)
            }
        }
    }

    private fun printHexBinary(data: ByteArray): String {
        val r = StringBuilder(data.size * 2)
        data.forEach { b ->
            val i = b.toInt()
            r.append(HEX_CHARS[i shr 4 and 0xF])
            r.append(HEX_CHARS[i and 0xF])
        }
        return r.toString()
    }

    private fun getData(user: String, email: String, pass: String) {

        val docRef = db.collection("Users").document(email)

        docRef.get()
            .addOnSuccessListener {
                    docs ->
                if (docs.exists()) {
                    Log.i(TAG, "User exists, kindly sign in!")
                    val intent = Intent(this, Login::class.java)
                    startActivity(intent)
                } else {
                    val bytes = MessageDigest.getInstance("SHA-256").digest(pass.toByteArray())
                    val hashedPassword = printHexBinary(bytes)

                    Log.i(TAG, hashedPassword)

                    val userData = hashMapOf (
                        "Name" to user,
                        "Email" to email,
                        "Password" to hashedPassword
                    )

                    docRef.set(userData)
                        .addOnSuccessListener {
                            Log.i(TAG,"User Added!")
                            Toast.makeText(this@SignUp, "$user Signed Up", Toast.LENGTH_LONG).show()
                            val intent = Intent(this, Login::class.java)
                            intent.putExtra("email", email)
                            startActivity(intent)
                        }
                        .addOnFailureListener {
                                exception -> Log.i(TAG, "Error: ", exception)
                        }
                }
            }
    }
}