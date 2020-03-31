package com.company.logon_ka_maseeha

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.android.synthetic.main.activity_donation.*
import java.net.FileNameMap
import java.sql.Timestamp
import java.time.Instant
import java.time.format.DateTimeFormatter

class Donation : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    companion object {
        private const val TAG = "DocSnippets"
        lateinit var item: String
        const val PICK_IMAGE_REQUEST = 111
        val storage = Firebase.storage
        lateinit var filePath: Uri
        lateinit var imgView: ImageView
        lateinit var downloadUri: String
        lateinit var fileName: String
        val db = Firebase.firestore
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation)

//        val mAuth = FirebaseAuth.getInstance()
//        val firebaseUser = mAuth.currentUser
//        Log.i(TAG, "User is: $firebaseUser")

        val product: Spinner = product_type
        Log.i(TAG, "Test message!")
        ArrayAdapter.createFromResource(
            this,
            R.array.Product_Types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            product.adapter = adapter
        }
        product.onItemSelectedListener = this

        selectImg.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
        }

        uploadImg.setOnClickListener {
            fileName = uploadToFirebase()
        }

        final_donate.setOnClickListener {
            uploadData(fileName)
        }
    }

    private fun uploadToFirebase(): String{
        val storageRef = storage.reference
        Log.i(TAG, "FP is $filePath") //filePath is simply the image data that will be received when onActivityResult is completed
        fileName = filePath.toString()
        fileName = fileName.substring(fileName.lastIndexOf("/") + 1)
        val imageRef: StorageReference? = storageRef.child(fileName)

        val uploadTask = imageRef?.putFile(filePath)

        val urlTask = uploadTask?.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imageRef.downloadUrl

        }?.addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    downloadUri = task.result.toString()
                    Toast.makeText(this, "Image Uploaded", Toast.LENGTH_LONG).show()
                    Log.i(TAG, downloadUri)
                } else {
                    Log.i(TAG, "Error getting urls")
                }
        }

        if (uploadTask == null) {
            Toast.makeText(this, "Please select an image before uploading", Toast.LENGTH_LONG).show()
        }
        return fileName
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filePath = data.data!!

            try{
                val bitMap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                imgView.setImageBitmap(bitMap)
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
    }

    private fun uploadData(fileName: String) {
        val time = Timestamp(System.currentTimeMillis())
        //val email = intent.getStringExtra("email")
        val sharedPreferences: SharedPreferences = getSharedPreferences("appSharedFile", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")
        Log.i(TAG, "email is: $email")

        val mno = mobile_no as EditText
        val userAddress = user_address as EditText

        val itemDetails = hashMapOf(
            "Type" to item,
            "Mobile_No" to mno.text.toString().toInt(),
            "Address" to userAddress.text.toString(),
            "ImageName" to fileName,
            "DownloadUrl" to downloadUri,
            "Timestamp" to time,
            "Status" to "Awaiting Response"
        )

        Log.i(TAG, "$itemDetails")

        val itemDonatedRef = db.collection("Items Donated")
        itemDonatedRef.add(itemDetails).addOnSuccessListener {
            documentRef ->  Log.i(TAG, "Data added to items, id: " + documentRef.id)
        }.addOnFailureListener{
            exception -> Log.i(TAG, "Error adding to items donated--", exception)
        }

        val docRef = email?.let { db.collection("Users").document(it).collection("Donated Items") }
        docRef?.add(itemDetails)?.addOnSuccessListener { documentRef -> Log.i(TAG, documentRef.id)
            val intent = Intent(this, UserPage::class.java)
            intent.putExtra("email", email)
            startActivity(intent)
        }?.addOnFailureListener{
                exception -> Log.i(TAG, "Error", exception)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        item = parent?.getItemAtPosition(position) as String
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}