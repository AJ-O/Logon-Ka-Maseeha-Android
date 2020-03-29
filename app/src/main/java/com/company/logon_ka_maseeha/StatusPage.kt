package com.company.logon_ka_maseeha

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp
import java.util.*
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

        //val email = intent.getStringExtra("email")
        val docRef = db.collection("Users").document("ashishleiot@gmail.com").collection("Donated Items")
        val lists = ArrayList<List_Item>()

        docRef.get() //Check if no item is there or not
            .addOnSuccessListener { docs ->
                for(doc in docs) {
                    val ptype = doc.get("Type")
                    val status = doc.get("Status")
                    val mno = doc.get("Mobile_No")
                    val donatedTime = doc.get("Timestamp") //Convert timestamp as date

                    lists.add(List_Item(ptype as String, status as String, mno as Long,
                        donatedTime as Timestamp
                    ))
                }
                val adapter = CustomAdapter(lists)
                recyclerView.adapter = adapter
            }.addOnFailureListener {
                exception -> Log.i(TAG, "exception", exception)
            }
    }
}
