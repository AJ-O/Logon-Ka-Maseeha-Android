package com.company.logon_ka_maseeha

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService(){

//    override fun onBind(intent: Intent): IBinder {
//        TODO("Return the communication channel to the service.")
//    }

    override fun onMessageReceived(p0: RemoteMessage) {
        val TAG = "DocSnippets"
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${p0.from}")

        // Check if message contains a data payload.
        if (p0.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + p0.data)

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                //scheduleJob()
                Log.i(TAG, "Random if!")
                val data: Map<String, String> = p0.data
                //TODO display message to user when using the app
            } else {
                // Handle message within 10 seconds
                //handleNow()
                Log.i(TAG, "Random else!")
            }
        }

        // Check if message contains a notification payload.
        p0.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

    }
        override fun onNewToken(token: String) {
            Log.i("DocSnippets", "This is the new generated token $token")
            //Updating the new registration token in the server of the user
            val sharedPreferences: SharedPreferences = getSharedPreferences("appSharedFile", Context.MODE_PRIVATE)
            val email = sharedPreferences.getString("email", "")
            val db = Firebase.firestore
            if (email != null && email != "") {
                val docRef = db.collection("Users").document(email)
                docRef.update("registrationToken", token).addOnCompleteListener{
                    Log.i("DocSnippets", "Registration token updated")
                }.addOnFailureListener{
                    exception ->  Log.i("DocSnippets", "Registration token not updated!", exception)
                }
            }
    }
}
