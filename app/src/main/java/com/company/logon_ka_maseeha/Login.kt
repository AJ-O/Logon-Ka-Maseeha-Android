package com.company.logon_ka_maseeha

import android.content.Context
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
import android.content.SharedPreferences

class Login : AppCompatActivity() {

    companion object {
        val db = Firebase.firestore
        private const val TAG = "DocSnippets"
        const val HEX_CHARS = "0123456789ABCDEF"
        private const val sharedPrefFile = "appSharedFile"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login.setOnClickListener {
            val ele1 = sign_in_emailId as EditText
            val ngoEmail = ele1.text.toString()

            val ele2 = sign_in_password as EditText
            val pass = ele2.text.toString()

            if(ngoEmail.isBlank() or pass.isBlank()) {
                Toast.makeText(this, "Kindly enter all the required data", Toast.LENGTH_LONG).show()
            } else {
                getData(ngoEmail, pass)
            }
        }
    }

    private fun getData(ngoEmail: String, pass: String) {

        Log.i(TAG, "$ngoEmail $pass")

        val docRef = db.collection("NGO").document("testNgoArea@gmail.com") //Replace with ngo email

        docRef.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val hashPassword = doc.get("Password")
//                    val bytes = MessageDigest.getInstance("SHA-256").digest(pass.toByteArray())
//                    val currHashedPassword = printHexBinary(bytes)

                    //if(hashPassword == currHashedPassword) {
                    if (hashPassword == "testHash") {
                        Log.i(TAG, "User exists!")

                        val ngoName = doc.get("Name") as String

                        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
                        val editor:SharedPreferences.Editor = sharedPreferences.edit()

                        editor.putString("ngoEmail", ngoEmail)
                        editor.putString("ngoName", ngoName)
                        editor.putString("photoUrl", "https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.spacetelescope.org%2Fimages%2Fheic1808a%2F&psig=AOvVaw3wRutboX88FRSahHazed3S&ust=1585767636711000&source=images&cd=vfe&ved=0CAIQjRxqFwoTCICu4vyyxegCFQAAAAAdAAAAABAQ")

                        editor.apply()
                        editor.commit()

                            //Change intent
                        val intent = Intent(this, NgoPage::class.java)
                        //intent.putExtra("email", email)
                        //Log.i(TAG, "${intent.extras} $email")
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Kindly enter the right Password", Toast.LENGTH_LONG).show()
                    }


                } else {
                    Toast.makeText(this, "NGO does not exist, kindly contact to get your credentials", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, MainActivity::class.java)
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
