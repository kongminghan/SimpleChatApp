package com.example.kminghan.simplechatapp

import android.util.Log
import android.widget.Toast
import com.sendbird.android.SendBird
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService



/**
 * Created by KMingHan on 3/4/2018.
 */
class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {

    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
        Log.d(TAG, "Refreshed token: " + refreshedToken!!)

        sendRegistrationToServer(refreshedToken)
    }

    private fun sendRegistrationToServer(token: String?) {
        SendBird.registerPushTokenForCurrentUser(token, SendBird.RegisterPushTokenWithStatusHandler { pushTokenRegistrationStatus, e ->
            if (e != null) {
                Toast.makeText(this@MyFirebaseInstanceIDService, "" + e.code + ":" + e.message, Toast.LENGTH_SHORT).show()
                return@RegisterPushTokenWithStatusHandler
            }

            if (pushTokenRegistrationStatus == SendBird.PushTokenRegistrationStatus.PENDING) {
                Toast.makeText(this@MyFirebaseInstanceIDService, "Connection required to register push token.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    companion object {
        private val TAG = "MyFirebaseIIDService"
    }
}