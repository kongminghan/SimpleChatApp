package com.example.kminghan.simplechatapp

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_msg_received.view.*
import kotlinx.android.synthetic.main.item_msg_sent.view.*

/**
 * Created by KMingHan on 3/3/2018.
 */
class MessageAdapter (val messages: ArrayList<Message>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_SENT: Int = 0;
    private val TYPE_RECEIVED: Int = 1;

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        when(messages[position].type) {
            TYPE_SENT -> (holder as MessageSentHolder).bindMessage(messages[position])
            TYPE_RECEIVED -> (holder as MessageReceivedHolder).bindMessage(messages[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        when(viewType){
            TYPE_SENT -> {
                return MessageSentHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_msg_sent, parent, false))
            }
            TYPE_RECEIVED -> {
                return MessageReceivedHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_msg_received, parent, false))
            }
        }
        return null;
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
}