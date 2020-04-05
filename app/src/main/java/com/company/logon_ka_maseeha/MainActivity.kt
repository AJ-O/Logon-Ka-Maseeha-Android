package com.company.logon_ka_maseeha

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "DocSnippets"
        private const val RC_SIGN_IN = 9001
        private lateinit var auth: FirebaseAuth
        private lateinit var googleSignInClient: GoogleSignInClient
        private const val sharedPrefFile = "appSharedFile"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        main_sign_up.setOnClickListener {
//            val intent = Intent(this, SignUp :: class.java)
//            startActivity(intent)
//        }
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
            val intent = Intent(this, UserPage::class.java)
            startActivity(intent)
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
            .requestIdToken(getString(R.string.default_web_client_id))//(getString(R.string.default_web_client_id))
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

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        Log.i(TAG, "$currentUser")
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
        editor.putString("PhotoUrl", gPhotoUrl)
        editor.putString("Name", gName)
        editor.apply()
        editor.commit()
    }
}
