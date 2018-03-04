package com.example.kminghan.simplechatapp

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_msg_received.view.*
import kotlinx.android.synthetic.main.item_msg_sent.view.*
import kotlinx.android.synthetic.main.item_msg_unread.view.*
import kotlinx.android.synthetic.main.item_msg_date.view.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by KMingHan on 3/3/2018.
 */
class MessageAdapter (val messages: ArrayList<Message>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SENT: Int = 0;
    private val TYPE_RECEIVED: Int = 1;
    private val TYPE_RECEIVED_UNREAD: Int = 2;
    private val TYPE_RECEIVED_DATE: Int = 3;
    private val TYPE_PRIVATE: Int = 1;
    private val TYPE_GROUP: Int = 2;

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(messages[position].type) {
            TYPE_SENT -> (holder as MessageSentHolder).bindMessage(messages[position])
            TYPE_RECEIVED -> (holder as MessageReceivedHolder).bindMessage(messages[position])
            TYPE_RECEIVED_UNREAD -> (holder as MessageUnreadHolder).bindMessage(messages[position])
            TYPE_RECEIVED_DATE -> (holder as MessageTodayHolder).bindMessage(messages[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            TYPE_SENT -> {
                MessageSentHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_msg_sent, parent, false))
            }
            TYPE_RECEIVED -> {
                MessageReceivedHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_msg_received, parent, false))
            }
            TYPE_RECEIVED_UNREAD -> {
                MessageUnreadHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_msg_unread, parent, false))
            }
            else -> {
                MessageTodayHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_msg_date, parent, false))
            }
        }
    }

    private fun getMessageTime(s: String): String? {
        return try {
            val newDate = Date(s.toLong())
            var sdf = SimpleDateFormat("hh:mm a")
            sdf.timeZone = TimeZone.getTimeZone("GMT+8")
            sdf.format(newDate)
        } catch (e: Exception) {
            e.toString()
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        return messages[position].type
    }

    inner class MessageSentHolder(v: View): RecyclerView.ViewHolder(v) {
        private var view: View = v

        fun bindMessage(message: Message) {
            view.msg_sent.text = message.text
            view.time_sent.text = getMessageTime(message.time)
        }
    }

    inner class MessageReceivedHolder(v: View): RecyclerView.ViewHolder(v) {
        private var view: View = v

        fun bindMessage(message: Message) {
            view.msg_received.text = message.text
            view.time_received.text = getMessageTime(message.time)

            if(message.channelType == TYPE_GROUP){
                view.tv_name.text = message.senderName
                view.tv_name.visibility = View.VISIBLE
            }
        }
    }

    class MessageUnreadHolder(v: View): RecyclerView.ViewHolder(v) {
        private var view: View = v

        fun bindMessage(message: Message) {
            val strUnreadCount = message.unreadCount.toString() + if(message.unreadCount > 1) " UNREAD MESSAGES" else " UNREAD MESSAGE"
            view.tvUnreadMessage.text = strUnreadCount
        }
    }

    class MessageTodayHolder(v: View): RecyclerView.ViewHolder(v) {
        private var view: View = v

        fun bindMessage(message: Message) {
            view.tv_date.text = message.text
        }
    }
}