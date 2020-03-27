package com.company.logon_ka_maseeha

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.view.get
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.android.synthetic.main.activity_donation.*

class Donation : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    companion object {
        private const val TAG = "DocSnippets"
        lateinit var item: String
        const val PICK_IMAGE_REQUEST = 111
        val storage = Firebase.storage
        lateinit var filePath: Uri
        lateinit var imgView: ImageView
        lateinit var downloadUri: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation)

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
            uploadToFirebase()
        }

        final_donate.setOnClickListener {
            displayData()
        }
    }

    private fun uploadToFirebase() {
        val storageRef = storage.reference
        var fileName = filePath.toString()
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

    private fun displayData() {
        val mno = mobile_no.text
        val userAddress = user_address.text
        Log.i(TAG, "$mno $userAddress $item")
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        item = parent?.getItemAtPosition(position) as String
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}


