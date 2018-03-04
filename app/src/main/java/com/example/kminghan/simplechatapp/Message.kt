package com.example.kminghan.simplechatapp

/**
 * Created by KMingHan on 3/3/2018.
 */
data class Message(val text: String, val time: String, val type: Int, val unreadCount: Int, val channelType: Int, val senderName: String)