package com.company.logon_ka_maseeha

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlin.collections.ArrayList

class StatusPage : AppCompatActivity() {

    companion object{
        const val TAG = "DocSnippets"
        val db = Firebase.firestore
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_status_page)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val storage = Firebase.storage
        val storageRef = storage.reference

        val email = intent.getStringExtra("email")?: ""
        Log.i(TAG, email)
        val docRef = db.collection("Users").document("ashishleiot@gmail.com").collection("Donated Items")
        val lists = ArrayList<ListItem>()
        val one_mb: Long = 1024 * 1024 //Max size of image
        docRef.get()
            .addOnSuccessListener { docs ->
                if(docs == null) {
                    Log.i(TAG, "No items donated!")
                    Toast.makeText(this, "No items donated", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, UserPage::class.java)
                    startActivity(intent)
                } else {
                    for (doc in docs) {
                        val productType = doc.get("Type")
                        val status = doc.get("Status")
                        val mno = doc.get("Mobile_No")
                        val donatedTime = doc.get("Timestamp") as com.google.firebase.Timestamp
                        val imageName = doc.get("ImageName")
                        val timeString = donatedTime.toDate()
                        val imageRef = storageRef.child(imageName as String)

                        imageRef.getBytes(one_mb).addOnSuccessListener {
                            val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                            lists.add(
                                ListItem(
                                    productType as String,
                                    status as String,
                                    mno as Long,
                                    timeString,
                                    bmp
                                )
                            )
                            val adapter = CustomAdapter(lists)
                            recyclerView.adapter = adapter
                        }.addOnFailureListener { exception ->
                            Log.i(TAG, "Error: ", exception)
                        }

                    }
                }
            }.addOnFailureListener {
                exception -> Log.i(TAG, "exception", exception)
            }
    }
}
