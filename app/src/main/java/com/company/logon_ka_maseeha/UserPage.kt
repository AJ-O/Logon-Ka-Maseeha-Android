package com.company.logon_ka_maseeha

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_page.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

class UserPage : AppCompatActivity() {

    companion object {
        const val TAG = "DocSnippets"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_page)

        donate_item_btn.setOnClickListener {
            val intent = Intent(this, Donation::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Confirmation")
            builder.setMessage("Do you want to sign out?")
            builder.setPositiveButton("Yes"){
                    _: DialogInterface?, _: Int ->
                FirebaseAuth.getInstance().signOut()

                val sharedPreferences: SharedPreferences = getSharedPreferences("appSharedFile", Context.MODE_PRIVATE)
                sharedPreferences.edit().clear().apply()

                Toast.makeText(this, "User has successfully signed out", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }
            builder.setNegativeButton("No"){ _, _ -> }
            val alertDialog: AlertDialog = builder.create()
            // Set other dialog properties
            alertDialog.setCancelable(true)
            alertDialog.show()
        }
        displayUserItems()
    }

    override fun onResume() {
        super.onResume()
        displayUserItems()
    }

    private fun displayUserItems() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("appSharedFile", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")
        val photoUrl = sharedPreferences.getString("photoUrl", "")
        val googleUserName = sharedPreferences.getString("username", "")

        Picasso.get().load(photoUrl).into(userDP)
        val userNameEle = userName as TextView
        userNameEle.text = googleUserName
        Log.i(TAG, "shared pref: $email")

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val storage = Firebase.storage
        val storageRef = storage.reference
        val db = Firebase.firestore
        val lists = ArrayList<ListItem>()
        val oneMb: Long = 1024 * 1024 //Max size of image

        Log.i(TAG, "$email")

        //TODO order by
        val docRef = email?.let { db.collection("Users").document(it).collection("Donated_Items") }
        docRef?.get()?.addOnSuccessListener { docs ->
            if (docs == null) {
                Log.i(StatusPage.TAG, "No items donated!")
                Toast.makeText(this, "No items donated", Toast.LENGTH_LONG).show()
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Message")
                builder.setMessage("No items have been donated yet")
                builder.setNeutralButton("Ok"){ _, _ -> }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(true)
                alertDialog.show()
            } else {
                for (doc in docs) {
                    val productType = doc.get("Type")
                    val status = doc.get("Status")
                    val mno = doc.get("Mobile_No")
                    val donatedTimeString = doc.get("Timestamp") as com.google.firebase.Timestamp
                    val imageName = doc.get("ImageName")
                    val donatedDate = donatedTimeString.toDate()
                    val imageRef = storageRef.child(imageName as String)

                    imageRef.getBytes(oneMb).addOnSuccessListener {
                        val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                        lists.add(
                            ListItem(
                                productType as String,
                                status as String,
                                mno as Long,
                                donatedDate,
                                bmp,
                                doc.id,
                                email,
                                imageName
                            )
                        )
                        val adapter = CustomAdapter(lists)
                        recyclerView.adapter = adapter
                    }.addOnFailureListener { exception ->
                        Log.i(StatusPage.TAG, "Error: ", exception)
                    }
                }
            }
        }?.addOnFailureListener {
                exception -> Log.i(StatusPage.TAG, "exception", exception)
        }
    }

    private fun calcDistanceBetweenUserAndNgo(ngoLat: Double, ngoLong: Double, userLat: Double, userLong: Double): Double{
        val theta: Double = ngoLong - userLong
        var dist = (sin(deg2rad(ngoLat))
                * sin(deg2rad(userLat))
                + (cos(deg2rad(ngoLat))
                * cos(deg2rad(userLat))
                * cos(deg2rad(theta))))
        dist = acos(dist)
        dist = rad2deg(dist)
        dist *= 60 * 1.1515
        return dist
    }

    private fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    private fun rad2deg(rad: Double): Double {
        return rad * 180.0 / Math.PI
    }
}
