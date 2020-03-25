package com.company.logon_ka_maseeha

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
//import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    fun onClick(v: View) {
        when (v.id) {
            R.id.sign_in_button -> signIn()
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 9001
        fun getLaunchIntent(from: Context) = Intent(from,
        MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("721444271113-fselfk74l3sfrdehdr1hnpf4m0vqhhni.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()

        val account = GoogleSignIn.getLastSignedInAccount(this)
        print(account)

        sign_in_button.setOnClickListener {
            signIn()
        }
        //sign_in_button.setOnClickListener(this)
        //setupUI()
    }

    private fun setupUI() {
        sign_in_button.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
        print("YAY!")
    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        print(currentUser)

        if (currentUser != null) {
            startActivity(MainActivity.getLaunchIntent(this))
            finish()
        }
        updateUI(currentUser)
    }

    private fun updateUI(user: FirebaseUser?) {

        if (user != null) {
            sign_in_button.visibility = View.GONE
        } else {
            print("God knows some error")
        }
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed!", e)
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(TAG, "firebaseAuthWithGoogle:"+ acct.id!!)

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) {
                task ->
                run {
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithCredential: success")
                        val user = auth.currentUser
                        print(user)
                        startActivity(MainActivity.getLaunchIntent(this))
                        //updateUI(user)
                    } else {
                        Log.w(TAG, "signInWithCredential: failure", task.exception)
                        //updateUI(null)
                    }
                }
            }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
    }

}
