package com.company.logon_ka_maseeha

import android.content.Context
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
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_page.*
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import com.google.firebase.messaging.FirebaseMessagingService

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

        donate_item_btn.setOnClickListener {
            val intent = Intent(this, Donation::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }

        logoutButton.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "User has successfully signed out", Toast.LENGTH_LONG).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val storage = Firebase.storage
        val storageRef = storage.reference
        val db = Firebase.firestore
        val lists = ArrayList<ListItem>()
        val oneMb: Long = 1024 * 1024 //Max size of image

        Log.i(TAG, "$email")

        val docRef = email?.let { db.collection("Users").document(it).collection("Donated_Items") }
        docRef?.get()?.addOnSuccessListener { docs ->
            if (docs == null) {
                Log.i(StatusPage.TAG, "No items donated!")
                Toast.makeText(this, "No items donated", Toast.LENGTH_LONG).show()
//                val intent = Intent(this, UserPage::class.java)
//                startActivity(intent)
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
                                email
                            )
                        )
                        val adapter = CustomAdapter(lists)
                        recyclerView.adapter = adapter
                    }.addOnFailureListener { exception ->
                        Log.i(StatusPage.TAG, "Error: ", exception)
                    }

                }
            }
        }?.addOnFailureListener { exception -> Log.i(StatusPage.TAG, "exception", exception)
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
