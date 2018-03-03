package com.example.kminghan.simplechatapp

/**
 * Created by KMingHan on 3/2/2018.
 */
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.list_item_row.view.*

class RecyclerAdapter (val chats: ArrayList<Channel>): RecyclerView.Adapter<RecyclerAdapter.ChannelHolder>(){

    override fun onBindViewHolder(holder: RecyclerAdapter.ChannelHolder, position: Int) {
        holder.bindChannel(chats[position])
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_row, parent, false)
        return ChannelHolder(inflatedView)
    }

    class ChannelHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v

        fun bindChannel(chat: Channel) {
            if(chat.img_url != "")
                Glide.with(view.context).load(chat.img_url).apply(RequestOptions().circleCrop()).into(view.img)
            else
                Glide.with(view.context).load(R.drawable.ic_person).apply(RequestOptions().circleCrop()).into(view.img)

            view.name.text = chat.name
            view.last_msg.text = chat.lastMsg

            view.setOnClickListener{
                val intent = Intent(view.context, ChatActivity::class.java)
                intent.putExtra("channelUrl", chat.channelUrl)
                intent.putExtra("profileUrl", chat.img_url)
                intent.putExtra("name", chat.name)
                intent.putExtra("lastSeen", chat.lastSeenAt)
                view.context.startActivity(intent)
            }
        }
    }
}