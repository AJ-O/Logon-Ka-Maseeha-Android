package com.company.logon_ka_maseeha

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_sign_up.*
import java.security.MessageDigest
import java.util.ArrayList


class SignUp : AppCompatActivity() {

    companion object {
        val db = Firebase.firestore
        private const val TAG = "DocSnippets"
        val HEX_CHARS = "0123456789ABCDEF".toCharArray()
        private lateinit var auth: FirebaseAuth
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        sign_up.setOnClickListener {

            val userEle = sign_up_username as EditText
            val ngoName = userEle.text.toString()

            val emailEle = sign_up_emailId as EditText
            val email = emailEle.text.toString()

            val passEle = sign_up_password as EditText
            val userPassword = passEle.text.toString()

            val ngoCoords = ngoCoordinates as EditText
            val ngoLocation = ngoCoords.text.toString()

            val coords = ngoLocation.split(" ")
            val lat = coords[0].toDouble()
            val long = coords[1].toDouble()

            Log.i(TAG, "$ngoName, $email, $userPassword, $lat, $long")
            if(ngoName.isBlank() or email.isBlank() or userPassword.isBlank() or ngoLocation.isBlank()){
                Toast.makeText(this, "Kindly enter the require data", Toast.LENGTH_LONG).show()
            } else {
                Log.i(TAG, "$ngoName, $email, $userPassword, $lat, $long")
                createUser(ngoName, email, userPassword, lat, long)
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

    private fun addUserToDatabase(email: String, pass: String, ngoName: String, lat: Double, long: Double){
        val db = Firebase.firestore

        val bytes = MessageDigest.getInstance("SHA-256").digest(pass.toByteArray())
        val hashedPassword = printHexBinary(bytes)
        val coords = arrayListOf(lat, long)

        val data = hashMapOf(
            "Name" to ngoName,
            "Password" to hashedPassword,
            "Coordinates" to coords
        )

        db.collection("NGO").document(email).set(data).addOnSuccessListener {
            Log.i(TAG, "User added successfully!")
            Toast.makeText(this, "Ngo added successfully!", Toast.LENGTH_LONG).show()
        }.addOnFailureListener {
            Log.i(TAG, "Failed to add to database!", it)
        }
    }

    private fun createUser(ngoName: String, email: String, pass: String, lat: Double, long: Double) {

        auth = FirebaseAuth.getInstance()
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener{ task ->
            if(task.isSuccessful){
                Log.i(TAG, "User created!")
                addUserToDatabase(email, pass, ngoName, lat, long)
            } else {
                Log.i(TAG, "Error: ", task.exception)
            }
        }
    }
}