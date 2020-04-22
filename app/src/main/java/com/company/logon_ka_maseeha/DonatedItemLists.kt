package com.company.logon_ka_maseeha

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class DonatedItemLists : AppCompatActivity() {

    companion object{
        const val TAG = "DocSnippets"
        val db = Firebase.firestore
        val storage = Firebase.storage
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donated_item_lists)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewItems)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val storageRef = storage.reference

        val sharedPreferences: SharedPreferences = getSharedPreferences("appSharedFile", Context.MODE_PRIVATE)
        val ngoEmail = sharedPreferences.getString("ngoEmail", "")

        val docRef = db.collection("Items_Donated")
        val itemList = ArrayList<NgoItemDisplayData>()
        val oneMb: Long = 1024 * 1024

//        item_status_btn.setOnClickListener {
//            Log.i(TAG, accepted_item_address.toString())
//            Log.i(TAG, accepted_item_donated_date.toString())
//            Log.i(TAG, accepted_item_mno.toString())
//            Log.i(TAG, accepted_item_status.toString())
//        }

        docRef.get()
            .addOnSuccessListener {
                docs ->

                if (docs == null) {
                    Log.i(TAG, "No items available!")
                    Toast.makeText(this, "No items available", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, NgoPage::class.java)
                    startActivity(intent)
                } else {
                    for (doc in docs) {
                        val productType = doc.get("Type")
                        val status = doc.get("Status")

                        if (status == "Awaiting Response") {

                            val mno = doc.get("Mobile_No")
                            val donatedTimeString =
                                doc.get("Timestamp") as com.google.firebase.Timestamp
                            val imageName = doc.get("ImageName")
                            val userAddress = doc.get("Address")
                            val userEmail = doc.get("Uploaded By")

                            val donatedDate = donatedTimeString.toDate()
                            val imageRef = storageRef.child(imageName as String)
                            imageRef.getBytes(oneMb).addOnSuccessListener {
                                val bmp = BitmapFactory.decodeByteArray(it, 0, it.size)
                                itemList.add(
                                    NgoItemDisplayData(
                                        productType as String,
                                        userAddress as String,
                                        status as String,
                                        mno as Long,
                                        donatedDate,
                                        bmp,
                                        doc.id,
                                        ngoEmail?:"",
                                        userEmail as String,
                                        imageName
                                    )
                                )
                                val adapter = DonatedItemCustomAdapter(itemList)
                                recyclerView.adapter = adapter
                            }.addOnFailureListener { exception ->
                                Log.w(TAG, "Error!", exception)
                            }
                        }
                    }
                }
            }.addOnFailureListener{
                exception -> Log.w(TAG, "Error, ", exception)
            }
        val adapter = DonatedItemCustomAdapter(itemList)
        recyclerView.adapter = adapter
    }
}
