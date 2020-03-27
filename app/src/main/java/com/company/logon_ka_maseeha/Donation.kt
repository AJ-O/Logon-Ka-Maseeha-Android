package com.company.logon_ka_maseeha

import android.app.Activity
import android.content.Intent
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


        val mAuth = FirebaseAuth.getInstance()
        val firebaseUser = mAuth.currentUser

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

//        val docRef = db.collection("Users").document("ashishleiot@gmail.com").collection("Donated Items")
//        docRef.get()
//            .addOnSuccessListener {
//                    docs -> for(doc in docs) {
//                    Log.i(TAG, "${doc.id} => ${doc.data} ${doc.get("type")}")
//                }
//            }.addOnFailureListener {
//                exception -> Log.i(TAG, "Error: ", exception)
//            }

        selectImg.setOnClickListener{
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_PICK
            startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
        }

        uploadImg.setOnClickListener {
            uploadToFirebase()
        }

        final_donate.setOnClickListener {
            uploadData()
        }
    }

    private fun uploadToFirebase() {
        val storageRef = storage.reference
        fileName = filePath.toString()
        fileName = fileName.substring(fileName.lastIndexOf("/") + 1)
        val imageRef: StorageReference? = storageRef.child(fileName)
        //imageRef.name == that will give images name
        val metadata = storageMetadata {
            contentType = "images/jpeg"
        }

        val uploadTask = imageRef?.putFile(filePath, metadata)

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
        Log.i(TAG, "$urlTask")
//        uploadTask?.addOnSuccessListener {
//            Toast.makeText(this, "Image Uploaded", Toast.LENGTH_LONG).show()
//        }?.addOnFailureListener {
//            exception -> Log.e(TAG, "Error $exception")
//        }

        if (uploadTask == null) {
            Toast.makeText(this, "Please select an image before uploading", Toast.LENGTH_LONG).show()
        }
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

    private fun uploadData() {
        val time = Timestamp(System.currentTimeMillis())
        val email = intent.getStringExtra("email")

        val itemDetails = hashMapOf(
            "Type" to item,
            "Mobile_No" to mobile_no.toString(),
            "Address" to user_address.toString(),
            "ImageName" to fileName,
            "DownloadUrl" to filePath,
            "Timestamp" to time
        )

        Log.i(TAG, "$itemDetails")

        val docRef = db.collection("Users").document("ashishleiot@gmail.com").collection("Donated Items")
        docRef.add(itemDetails)
            .addOnSuccessListener {
                documentRef -> Log.i(TAG, documentRef.id)
            }
            .addOnFailureListener{
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


