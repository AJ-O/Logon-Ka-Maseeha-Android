//Check for permissions
//put things in onStart?
//Mail?
//implement onResume method, for freshly loading changes in itemlist -- ngo page and user page
package com.company.logon_ka_maseeha

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.company.logon_ka_maseeha.services.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.api.ServiceOrBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "DocSnippets"
        private const val RC_SIGN_IN = 9001
        private lateinit var auth: FirebaseAuth
        private lateinit var googleSignInClient: GoogleSignInClient
        private const val sharedPrefFile = "appSharedFile"
        val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        main_sign_up.setOnClickListener {
//            val intent = Intent(this, SignUp :: class.java)
//            startActivity(intent)
//        }

        button.setOnClickListener {
            callTest()
        }

        val user = FirebaseAuth.getInstance().currentUser
        if(user != null) {
            Log.i(TAG, "User Exists!")
            val email = user.email
            val photoUrl = user.photoUrl.toString()
            val name = user.displayName
            if (email != null) {
                if (name != null) {
                    setSharedPreferences(email, photoUrl, name)
                }
            }
            Toast.makeText(this, "User exists! Yay!", Toast.LENGTH_LONG).show()
//            val intent = Intent(this, UserPage::class.java)
//            startActivity(intent)
        } else {
            Log.i(TAG, "User does not exist!")
        }

        ngo_sign_in.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        gSignIn.setOnClickListener{
            Log.i(TAG, "Called Google sign in")
            signIn()
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()
    }

    private fun signIn(){
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG, "Data is: $data")
        if(requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Log.w(TAG, "G Sign in failed!", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.i(TAG, "Account: $acct.id")

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener (this){
                    task ->
                if(task.isSuccessful) {
                    Log.i(TAG, "Successful sign in: $task")
                    val user = auth.currentUser
                    val email = user?.email
                    val googleUserName = user?.displayName
                    val googlePhotoUrl = user?.photoUrl.toString()

                    if (email != null) {
                        if (googleUserName != null) {
                            setSharedPreferences(email, googlePhotoUrl, googleUserName)
                        }
                    }

                    val db = Firebase.firestore
                    if (email != null) {
                        val userDetails = hashMapOf(
                            "Name" to googleUserName,
                            "PhotoUrl" to googlePhotoUrl
                        )
                        db.collection("Users").document(email).set(userDetails).addOnSuccessListener {
                            val intent = Intent(this, UserPage::class.java)
                            startActivity(intent)
                        }.addOnFailureListener{
                            exception -> Log.w(TAG, "Error adding to database!", exception)
                        }
                    }
                } else {
                    Snackbar.make(activity_main, "Authentication failed", Snackbar.LENGTH_LONG).show()
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                }
            }
    }

    private fun setSharedPreferences(gEmail: String, gPhotoUrl: String, gName: String) {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("email", gEmail)
        editor.putString("photoUrl", gPhotoUrl)
        editor.putString("username", gName)
        editor.apply()
        editor.commit()
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        Log.i(TAG, "$currentUser")
        when {
            PermissionUtils.isAccessFineLocationGranted(this) -> {
                when {
                    PermissionUtils.isLocationEnabled(this) -> {
                        //setUpLocationListener()
                        Log.i(TAG,"Permission given")
                    } else ->  {
                    PermissionUtils.showGPSNotEnabledDialog(this)
                }
                }
            }   else -> {
                PermissionUtils.requestAccessFineLocationPermission(this, LOCATION_PERMISSION_REQUEST_CODE) //Change the requestId
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    when {
                        PermissionUtils.isLocationEnabled(this) -> run {
                            //setUpLocationListener()
                            Log.i(TAG, "Asked permissions and recd!")
                        } else -> {
                        PermissionUtils.showGPSNotEnabledDialog(this)
                    }
                    }
                } else {
                    Toast.makeText(this, "Location not granted", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
//testBuilder -- object, serviceBuilder
    //test - interface, DestinationService
    private fun callTest() {
//        val testService: test = testBuilder.buildService(test::class.java)
//        val requestCall: Call<List<testData>> = testService.testFun()
//
//        requestCall.enqueue(object: Callback<List<testData>> {
//            override fun onResponse(call: Call<List<testData>>, response: Response<List<testData>>) {
//                Log.i(TAG, "Kind of got response")
//                if(response.isSuccessful) {
//                    val retData: List<testData> = response.body()!!
//                    for(value in retData) {
//                        Log.i(TAG, value.name.toString())
//                        Log.i(TAG, value.pass.toString())
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<List<testData>>, t: Throwable) {
//                Log.i(TAG, "Failed response")
//                Toast.makeText(this@MainActivity, t.message, Toast.LENGTH_LONG).show()
//            }
//        })
            val ngoDistancesList: MutableList<Double> = mutableListOf(10.0, 12.0, 13.0)
            val mailService: ServerRequests = ServiceBuilder.buildService(ServerRequests::class.java)
            val requestCall: Call<String> = mailService.sendMail(ngoDistancesList)
            //TODO Check for values expected for return vs sending
            requestCall.enqueue(object: Callback<String>{
            override fun onResponse(call: Call<String>, response: Response<String>){
                if(response.isSuccessful) {
                    Log.i(TAG, "Sent Data!")
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.i(TAG, "${t.message}")
            }
        })
    }
}
