package com.company.logon_ka_maseeha

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import com.company.logon_ka_maseeha.services.ServerRequests
import com.company.logon_ka_maseeha.services.ServiceBuilder
import com.company.logon_ka_maseeha.services.MailData
import com.company.logon_ka_maseeha.services.MailSuccessResponse
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.android.synthetic.main.activity_donation.*
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response
import java.net.FileNameMap
import java.sql.Timestamp
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.properties.Delegates

class Donation : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var fileName: String
    private lateinit var filePath: Uri
    companion object {
        private const val TAG = "DocSnippets"
        private lateinit var fusedLocationClient: FusedLocationProviderClient
        lateinit var item: String
        //lateinit var filePath: Uri
        lateinit var imgView: ImageView
        lateinit var downloadUri: String
        //lateinit var fileName: String
        const val PICK_IMAGE_REQUEST = 111
        val storage = Firebase.storage
        val db = Firebase.firestore
        var userLat = 0.0
        var userLong = 0.0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation)
        when {
            PermissionUtils.isAccessFineLocationGranted(this) -> {
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                            //setUpLocationListener()
                        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this) //Works only if last location is given or else null
                        fusedLocationClient.lastLocation.addOnSuccessListener {
                            if (it == null) {
                                Toast.makeText(this, "Error fetching coordinates", Toast.LENGTH_LONG).show()
                                val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                                val locationRequest = LocationRequest().setInterval(2000).setFastestInterval(2000)
                                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                fusedLocationProviderClient.requestLocationUpdates(locationRequest, object: LocationCallback() {
                                    override fun onLocationResult(locationResult: LocationResult?) {
                                        super.onLocationResult(locationResult)

                                        if (locationResult != null) {
                                            for(location in locationResult.locations) {
                                                userLat = location.latitude
                                                userLong = location.longitude
                                                Log.i(TAG, "Lat: ${location.latitude}")
                                                Log.i(TAG, "Long: ${location.longitude}")
                                                //Looper.myLooper()?.quitSafely()
                                                break
                                            }
                                        }
                                    }
                                },
                                    Looper.myLooper()) // ---commented till here
                            }
                            else {
                                Log.i(TAG, "{${it.longitude}, {${it.latitude}}")
                                userLat = it.latitude
                                userLong = it.longitude
                            }
                        }.addOnFailureListener { exception ->
                            Log.i(TAG, exception.toString())
                        }
                            Log.i(TAG,"Permission given")
                    } else ->  {
                        PermissionUtils.showGPSNotEnabledDialog(this)
                    }
                }
            }  else -> {
                PermissionUtils.requestAccessFineLocationPermission(this,
                MainActivity.LOCATION_PERMISSION_REQUEST_CODE
                ) //Change the requestId
            }
        }
//        val mAuth = FirebaseAuth.getInstance()
//        val firebaseUser = mAuth.currentUser
//        Log.i(TAG, "User is: $firebaseUser")

        //To select an item from a drop down menu
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
            //TODO popup for confirmation
            if(::fileName.isInitialized) {
                uploadData(fileName)
            } else {
                Toast.makeText(this, "Kindly select an image before uploading item!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun uploadToFirebase(): String {
        val storageRef = storage.reference

        if(::filePath.isInitialized) {
            Log.i(TAG, "FP is $filePath") //filePath is simply the image data that will be received when onActivityResult is completed
            fileName = filePath.toString()
            fileName = fileName.substring(fileName.lastIndexOf("/") + 1)
            val imageRef: StorageReference? = storageRef.child(fileName)

            val uploadTask = imageRef?.putFile(filePath)

            uploadTask?.continueWithTask { task ->
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
                    Log.i(TAG, "Error getting url")
                }
            }

            if (uploadTask == null) {
                Toast.makeText(this, "Please select an image before uploading", Toast.LENGTH_LONG).show()
            }
            return fileName
        } else {
            Toast.makeText(this, "Please select an image before uploading", Toast.LENGTH_LONG).show()
            return  ""
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            filePath = data.data!!

            try{
                val bitMap = MediaStore.Images.Media.getBitmap(contentResolver, filePath)
                //TODO find alternative to deprecated method
                imgView.setImageBitmap(bitMap)
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        } //TODO a popup if the intent fails!
    }

    private fun uploadData(fileName: String) {

        if (fileName == "") {
            Toast.makeText(
                this,
                "Please select an image before donating the item!",
                Toast.LENGTH_LONG
            ).show()
        } else {
            val time = Timestamp(System.currentTimeMillis())
            //val email = intent.getStringExtra("email")
            val sharedPreferences: SharedPreferences =
                getSharedPreferences("appSharedFile", Context.MODE_PRIVATE)
            val email = sharedPreferences.getString("email", "")
            val mno = mobile_no as EditText
            val userAddress = user_address as EditText

            Log.i(TAG, "email is: $email")

            if (mno.text.toString() == "" || userAddress.text.toString() == "") {
                Toast.makeText(
                    this,
                    "Please fill out the details for the item before uploading",
                    Toast.LENGTH_LONG
                ).show()
            } else {

                val itemDetails = hashMapOf(
                    "Type" to item,
                    "Mobile_No" to mno.text.toString().toInt(),
                    "Address" to userAddress.text.toString(),
                    "ImageName" to fileName,
                    "DownloadUrl" to downloadUri,
                    "Timestamp" to time,
                    "Status" to "Awaiting Response",
                    "Uploaded By" to email
                )

                Log.i(TAG, "$itemDetails")
                Log.i(TAG, "$userLat, $userLong")
                val itemDonatedRef = db.collection("Items_Donated")//TODO Change " " with _ "

                val docRef =
                    email?.let { db.collection("Users").document(it).collection("Donated_Items") }
                docRef?.add(itemDetails)?.addOnSuccessListener { documentRef ->
                    Log.i(TAG, documentRef.id)
                    itemDonatedRef.document(documentRef.id).set(itemDetails)
                        .addOnSuccessListener {
                            val ngoDb = Firebase.firestore
                            //var ngoNameAndDistanceList: MutableList<MailData> = listOf(Pair)
                            val ngoEmailAndDistanceList: HashMap<String, Double> = hashMapOf()

                            val docRef =
                                ngoDb.collection("NGO").get().addOnSuccessListener { docs ->
                                    for (doc in docs) {
                                        val ngoCoordinates = doc.get("Coordinates") as ArrayList<*>
                                        val ngoLat = ngoCoordinates[0]
                                        val ngoLong = ngoCoordinates[1]
                                        val ngoEmail = doc.id
                                        val dist = calcDistanceBetweenUserAndNgo(
                                            ngoLat as Double,
                                            ngoLong as Double, userLat, userLong
                                        )
                                        ngoEmailAndDistanceList[ngoEmail] = dist
                                        Log.i(TAG, "NGO DETAils = $ngoEmail, $ngoLat, $dist")
                                    }
                                    Log.i(TAG, ngoEmailAndDistanceList.toString())

                                    //Initialize service
                                    val mailService: ServerRequests = ServiceBuilder.buildService(ServerRequests::class.java)
                                    val requestCall: Call<MailSuccessResponse> = mailService.sendMail(ngoEmailAndDistanceList)
                                    //TODO Check for values expected for return vs sending
                                    requestCall.enqueue(object : Callback<MailSuccessResponse> {
                                        override fun onResponse(
                                            call: Call<MailSuccessResponse>,
                                            response: Response<MailSuccessResponse>
                                        ) {
                                            if (response.isSuccessful) {
                                                Log.i(TAG, "Mails sent!!")
                                                Log.i(TAG, response.body()!!.toString())
                                                Toast.makeText(
                                                    this@Donation,
                                                    "Mails sent to Ngo",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                val intent = Intent(this@Donation, UserPage::class.java)
                                                startActivity(intent)
                                            }
                                        }

                                        override fun onFailure(
                                            call: Call<MailSuccessResponse>,
                                            t: Throwable
                                        ) {
                                            Log.i(TAG, "${t.message}")
                                        }
                                    })
                                }
//                val intent = Intent(this, UserPage::class.java)
//                //intent.putExtra("email", email)
//                startActivity(intent)
                        }.addOnFailureListener { exception ->
                            Log.i(TAG, "error inserting in Items donated collection: ", exception)
                        }
                }?.addOnFailureListener { exception ->
                    Log.i(TAG, "Error", exception)
                }
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        item = parent?.getItemAtPosition(position) as String
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this, "Kindly select the type of the item", Toast.LENGTH_LONG).show()
    }

    private fun calcDistanceBetweenUserAndNgo(ngoLat: Double, ngoLong: Double, userLat: Double, userLong: Double) : Double{
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