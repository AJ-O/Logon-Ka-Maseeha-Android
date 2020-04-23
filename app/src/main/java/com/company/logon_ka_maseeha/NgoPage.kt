package com.company.logon_ka_maseeha

import android.app.AlertDialog
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_ngo_page.*

class NgoPage : AppCompatActivity() {

    companion object{
        const val TAG = "DocSnippets"
        val db = Firebase.firestore
        val storage = Firebase.storage
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ngo_page)

        collections.setOnClickListener{
            val intent = Intent(this, DonatedItemLists::class.java)
            startActivity(intent)
        }

        displayItems()

        ngoLogout.setOnClickListener{
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Sign out")
            builder.setMessage("Do you want to sign out?")
            builder.setPositiveButton("Yes"){ _, _  ->
                FirebaseAuth.getInstance().signOut()
            }
            builder.setNegativeButton("No"){ _, _ -> }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(true)
            alertDialog.show()
        }
    }

    override fun onResume() {
        super.onResume()
        displayItems()
    }

    private fun displayItems() {
        val sharedPreferences: SharedPreferences = getSharedPreferences("appSharedFile", Context.MODE_PRIVATE)
        val globalNgoName = sharedPreferences.getString("ngoName", "")

        ngoName.text = globalNgoName

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewNgo)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val storageRef = storage.reference

        //val sharedPreferences: SharedPreferences = getSharedPreferences("appSharedFile", Context.MODE_PRIVATE)
        val ngoEmail = sharedPreferences.getString("ngoEmail", "")?:""

        val docRef = db.collection("NGO").document(ngoEmail).collection("Selected_Items")
        val itemList = ArrayList<NgoItemDisplayData>()
        val oneMb: Long = 1024 * 1024

        docRef.get()
            .addOnSuccessListener {
                    docs ->

                if (docs == null) {
                    Log.i(TAG, "No items donated")
                    Toast.makeText(this, "No items donated yet", Toast.LENGTH_LONG).show()
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Message")
                    builder.setMessage("No items to have been accepted yet")
                    builder.setNeutralButton("Ok"){ _, _ -> }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(true)
                    alertDialog.show()
                }

                else {
                    for (doc in docs) {

                        val status = doc.get("Status")

                        if (status != "Awaiting Response") {

                            val productType = doc.get("Type")
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
                                        ngoEmail,
                                        userEmail as String,
                                        imageName
                                    )
                                )

                                val adapter = NgoItemCustomAdapter(itemList)
                                recyclerView.adapter = adapter
                            }.addOnFailureListener { exception ->
                                Log.i(TAG, exception.toString())
                            }
                        }
                    }
                }
            }.addOnFailureListener{
                    exception -> Log.i(TAG, "Error: ", exception)
            }
        val adapter = NgoItemCustomAdapter(itemList)
        recyclerView.adapter = adapter
    }
}
