package com.company.logon_ka_maseeha

import android.R
import android.R.attr.bitmap
import android.R.style
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class MyFirebaseMessagingService : FirebaseMessagingService(){

    private val TAG = "MyFirebaseMessagingServ"

    override fun onMessageReceived(p0: RemoteMessage) {

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${p0.from}")
        // Check if message contains a data payload.
        if (p0.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: " + p0.data)
            // For long-running tasks (10 seconds or more) use WorkManager.
            //scheduleJob()
            setNotificationData(p0, Notification())
        }

        // Check if message contains a notification payload.
        p0.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }
    }

    private fun setNotificationData(remoteMessage: RemoteMessage, notification: Notification){
        val data: Map<String, String> = remoteMessage.data
        notification.title = data["title"].toString()
        notification.content = data["content"].toString()
        //notification.imageUrl = data["imageUrl"].toString()

        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent =
            PendingIntent.getActivity(applicationContext, 0, intent, 0)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val NOTIFICATION_CHANNEL_ID = "101"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant") val notificationChannel =
                NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "Notification",
                    NotificationManager.IMPORTANCE_MAX
                )

            //Configure Notification Channel
            notificationChannel.description = "Donation Notifications"
            notificationChannel.enableLights(true)
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.sym_def_app_icon)
                .setContentTitle(notification.title)
                .setAutoCancel(true)
                //.setSound(defaultSound)
                .setContentText(notification.content)
                .setContentIntent(pendingIntent)
                //.setStyle(style)
                .setWhen(System.currentTimeMillis())
                .setPriority(2) //MAX
        notificationManager.notify(1, notificationBuilder.build()) }

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
            }.addOnFailureListener{ exception ->
                Log.i("DocSnippets", "Registration token not updated!", exception)
            }
        }
    }
}
