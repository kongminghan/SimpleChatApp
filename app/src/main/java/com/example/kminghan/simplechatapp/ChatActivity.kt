package com.example.kminghan.simplechatapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.AccessToken
import com.sendbird.android.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.toolbar_chat.*
import com.sendbird.android.OpenChannel
import com.sendbird.android.User
import com.sendbird.android.GroupChannel
import com.sendbird.android.BaseChannel
import com.sendbird.android.BaseMessage
import com.sendbird.android.SendBird
import java.text.SimpleDateFormat
import java.util.*


class ChatActivity : AppCompatActivity() {
    private var userId: String = ""
    private var groupChannel: GroupChannel? = null
    private val TYPE_SENT: Int = 0;
    private val TYPE_RECEIVED: Int = 1;
    private val CHANNEL_HANDLER_ID = "CHAT_CHANNEL"
    private var channel_url: String = ""
    private var messageList: ArrayList<Message> = ArrayList()
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        Thread(Runnable {
            userId = AccessToken.getCurrentAccessToken().userId.toString()
        }).start()

        val image_url = intent.getStringExtra("profileUrl")
        val name = intent.getStringExtra("name")
        channel_url = intent.getStringExtra("channelUrl")
        val lastSeen = intent.getStringExtra("lastSeen")

        Glide.with(applicationContext)
                .load(image_url)
                .apply(RequestOptions().circleCrop())
                .into(conversation_contact_photo)

        setSupportActionBar(toolbar_chat)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayUseLogoEnabled(true);

        linearLayoutManager = LinearLayoutManager(applicationContext, LinearLayout.VERTICAL, false)
        rv_chat.layoutManager = linearLayoutManager
        messageAdapter = MessageAdapter(messageList)
        rv_chat.adapter = messageAdapter

        action_bar_title_1.text = name
        action_bar_title_2.text = getLastSeen(lastSeen)

        GroupChannel.getChannel(channel_url, GroupChannel.GroupChannelGetHandler{
            channel, e ->
            if(e != null){
                e.printStackTrace()
                return@GroupChannelGetHandler
            }
            groupChannel = channel
            retrieveMessage()
            channelHandler()
        })

        btn_send.setOnClickListener {
            sendMessage(et_chat.text.toString())
        }


//        Glide.with(applicationContext)
//                .asBitmap()
//                .load(image_url)
//                .apply(RequestOptions().circleCrop().override(120, 120))
//                .into(object : SimpleTarget<Bitmap>() {
//                    override fun onResourceReady(bitmap: Bitmap, transition: Transition<in Bitmap>?) {
//                        supportActionBar!!.setIcon(BitmapDrawable(resources, bitmap))
//                    }
//                })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            val id = item.itemId
            if(id == R.id.miLeaveChannel) {
                leaveChannel(groupChannel!!)
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun leaveChannel(channel: GroupChannel) {
        channel.leave(GroupChannel.GroupChannelLeaveHandler { e ->
            if (e != null) {
                return@GroupChannelLeaveHandler
            }
        })
    }

    private fun sendMessage(text: String) {
        groupChannel!!.sendUserMessage(text, BaseChannel.SendUserMessageHandler{
            userMessage, sendBirdException ->
            if (sendBirdException != null) {
                sendBirdException.printStackTrace();
                return@SendUserMessageHandler;
            }
            messageList.add(Message(userMessage.message, userMessage.createdAt.toString(), TYPE_SENT))
            messageAdapter.notifyItemInserted(messageList.size -1)
            et_chat.text.clear()
        })
    }

    private fun retrieveMessage(){
        groupChannel!!.getPreviousMessagesByTimestamp(Long.MAX_VALUE, true, 30, false,
                BaseChannel.MessageTypeFilter.USER, null, BaseChannel.GetMessagesHandler{
            list: List<BaseMessage>, e ->
            if (e != null) {
                e.printStackTrace();
                return@GetMessagesHandler;
            }

            list.map { (it as UserMessage) }.forEach {
                if(it.sender.userId == userId)
                    messageList.add(Message(it.message, it.createdAt.toString(), TYPE_SENT))
                else
                    messageList.add(Message(it.message, it.createdAt.toString(), TYPE_RECEIVED))
            }

            messageAdapter.notifyDataSetChanged()
            rv_chat.scrollToPosition(messageList.size - 1)
        })
    }

    private fun channelHandler(){
        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, object : SendBird.ChannelHandler() {
            override fun onMessageReceived(baseChannel: BaseChannel, baseMessage: BaseMessage) {
                if (baseChannel.url == channel_url) {
                    val userMessage = (baseMessage as UserMessage)
                    messageList.add(Message(userMessage.message, userMessage.createdAt.toString(), TYPE_RECEIVED))
                    messageAdapter.notifyItemInserted(messageList.size - 1)
                    rv_chat.scrollToPosition(messageList.size - 1)
                }
            }
        })
    }

    public override fun onPause() {
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID)
        super.onPause()
    }

    private fun getLastSeen(s: String): String? {
        return try {
            val newDate = Date(s.toLong())
            val curr = Calendar.getInstance().time;
            var sdf = SimpleDateFormat("hh:mm a")

            if(newDate.day == curr.day){
                "Last seen at "+ sdf.format(newDate)
            } else{
                sdf = SimpleDateFormat("dd/MM/yyyy HH:mm")
                "Last seen at" + sdf.format(newDate)
            }
        } catch (e: Exception) {
            e.toString()
        }
    }
}
