package com.example.kminghan.simplechatapp

/**
 * Created by KMingHan on 3/2/2018.
 */
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.list_item_row.view.*

class ChannelAdapter(val chats: ArrayList<Channel>): RecyclerView.Adapter<ChannelAdapter.ChannelHolder>(){

    val TYPE_PRIVATE: Int = 1;
    val TYPE_GROUP: Int = 2;

    override fun onBindViewHolder(holder: ChannelAdapter.ChannelHolder, position: Int) {
        holder.bindChannel(chats[position])
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_row, parent, false)
        return ChannelHolder(inflatedView)
    }

    inner class ChannelHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v

        fun bindChannel(chat: Channel) {
            if(chat.img_url != "")
                GlideApp.with(view.context).load(chat.img_url).apply(RequestOptions().circleCrop()).into(view.img)
            else {
                if(chat.type == TYPE_PRIVATE) {
                    GlideApp.with(view.context).load(R.drawable.ic_person).apply(RequestOptions().circleCrop()).into(view.img)
                }
                else if(chat.type == TYPE_GROUP) {
                    GlideApp.with(view.context).load(R.drawable.ic_group).apply(RequestOptions().circleCrop()).into(view.img)
                }
            }

            view.name.text = chat.name
            view.last_msg.text = chat.lastMsg

            view.setOnClickListener {
                if(chat.type == TYPE_PRIVATE){
                    val intent = Intent(view.context, ChatActivity::class.java)
                    intent.putExtra("channelUrl", chat.channelUrl)
                    intent.putExtra("profileUrl", chat.img_url)
                    intent.putExtra("name", chat.name)
                    intent.putExtra("lastSeen", chat.lastSeenAt)
                    view.context.startActivity(intent)
                }
                else if(chat.type == TYPE_GROUP){
                    val intent = Intent(view.context, ChatActivity::class.java)
                    intent.putExtra("channelUrl", chat.channelUrl)
                    intent.putExtra("profileUrl", chat.img_url)
                    intent.putExtra("name", chat.name)
                    intent.putExtra("lastSeen", chat.lastSeenAt)
                    intent.putExtra("type", chat.type)
                    view.context.startActivity(intent)
                }
            }
        }
    }
}