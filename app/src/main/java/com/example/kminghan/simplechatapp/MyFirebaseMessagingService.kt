package com.example.kminghan.simplechatapp

import android.app.Notification
import android.content.Context.NOTIFICATION_SERVICE
import android.app.NotificationManager
import android.media.RingtoneManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sendbird.android.BaseChannel
import org.json.JSONException
import org.json.JSONObject



/**
 * Created by KMingHan on 3/4/2018.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TYPE_PRIVATE: Int = 1;
    private val TYPE_GROUP: Int = 2;

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        var channelUrl: String? = null
        var channelName: String? = null
        var channelType: Int? = null
        var profileUrl: String? = null

        try {
            val sendBird = JSONObject(remoteMessage.getData().get("sendbird"))
            val channel = sendBird.get("channel") as JSONObject
            val sender = sendBird.get("recipient") as JSONObject
            val type = sendBird.get("channel_type") as String

            if(type == "group_messaging"){
                channelType = TYPE_PRIVATE
                profileUrl = ""
            }else {
                channelType = TYPE_GROUP
                profileUrl = sender.get("profile_url") as String
            }
            channelUrl = channel.get("channel_url") as String
            channelName = channel.get("name") as String
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        sendNotification(this, remoteMessage.data["message"]!!, channelUrl, channelType!!, channelName!!, profileUrl!!)
    }

    private fun sendNotification(context: Context, messageBody: String, channelUrl: String?, channelType: Int, channelName: String, profileUrl: String) {
        val intent = Intent(context, ChatActivity::class.java)
        intent.putExtra("channelUrl", channelUrl)
        intent.putExtra("name", channelName)
        intent.putExtra("profileUrl", profileUrl)
        intent.putExtra("type", channelType)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(Notification.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)

        notificationBuilder.setContentText(messageBody)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(0, notificationBuilder.build())
    }
}