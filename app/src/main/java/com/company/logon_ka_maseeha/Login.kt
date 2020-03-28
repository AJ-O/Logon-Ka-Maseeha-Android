package com.company.logon_ka_maseeha

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import java.lang.StringBuilder
import java.security.MessageDigest

class Login : AppCompatActivity() {

    companion object {
        val db = Firebase.firestore
        private const val TAG = "DocSnippets"
        const val HEX_CHARS = "0123456789ABCDEF"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login.setOnClickListener {
            val ele1 = sign_in_emailId as EditText
            val email = ele1.text.toString()

            val ele2 = sign_in_password as EditText
            val pass = ele2.text.toString()

            if(email.isBlank() or pass.isBlank()) {
                Toast.makeText(this, "Kindly enter all the required data", Toast.LENGTH_LONG).show()
            } else {
                getData(email, pass)
            }
        }
    }

    private fun getData(email: String, pass: String) {

        Log.i(TAG, "$email $pass")

        val docRef = db.collection("Users").document(email)

        docRef.get()
            .addOnSuccessListener {
                    docs ->
                if (docs.exists()) {
                    val hashPassword = docs.get("Password")
                    val bytes = MessageDigest.getInstance("SHA-256").digest(pass.toByteArray())
                    val currHashedPassword = printHexBinary(bytes)

                    if(hashPassword == currHashedPassword) {
                            Log.i(TAG, "User exists!")
                            val intent = Intent(this, UserPage::class.java)
                            intent.putExtra("email", email)
                            Log.i(TAG, "${intent.extras} $email")
                            startActivity(intent)
                    } else {
                        Toast.makeText(this, "Kindly enter the right Password", Toast.LENGTH_LONG).show()
                    }

                } else {
                    Log.i(TAG, "User does not exists, kindly register first")
                    val intent = Intent(this, SignUp::class.java)
                    startActivity(intent)
                }
            }
    }

    private fun printHexBinary(data: ByteArray): String {
        val res = StringBuilder(data.size * 2)
        data.forEach { byte->
            val i = byte.toInt()
            res.append(HEX_CHARS[i shr 4 and 0xF])
            res.append(HEX_CHARS[i and 0xF])
        }
        return res.toString()
    }
}
