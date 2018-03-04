package com.example.kminghan.simplechatapp

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_msg_received.view.*
import kotlinx.android.synthetic.main.item_msg_sent.view.*
import kotlinx.android.synthetic.main.item_msg_today.view.*
import kotlinx.android.synthetic.main.item_msg_unread.view.*

/**
 * Created by KMingHan on 3/3/2018.
 */
class MessageAdapter (val messages: ArrayList<Message>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SENT: Int = 0;
    private val TYPE_RECEIVED: Int = 1;
    private val TYPE_RECEIVED_UNREAD: Int = 2;
    private val TYPE_RECEIVED_TODAY: Int = 3;


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(messages[position].type) {
            TYPE_SENT -> (holder as MessageSentHolder).bindMessage(messages[position])
            TYPE_RECEIVED -> (holder as MessageReceivedHolder).bindMessage(messages[position])
            TYPE_RECEIVED_UNREAD -> (holder as MessageUnreadHolder).bindMessage(messages[position])
            TYPE_RECEIVED_TODAY -> (holder as MessageTodayHolder)
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
                MessageTodayHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_msg_today, parent, false))
            }
        }
    }


    override fun getItemCount(): Int {
        return messages.size
    }

    override fun getItemViewType(position: Int): Int {
        return messages[position].type
    }

    class MessageSentHolder(v: View): RecyclerView.ViewHolder(v) {
        private var view: View = v

        fun bindMessage(message: Message) {
            view.msg_sent.text = message.text
        }
    }

    class MessageReceivedHolder(v: View): RecyclerView.ViewHolder(v) {
        private var view: View = v

        fun bindMessage(message: Message) {
            view.msg_received.text = message.text
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
    }
}