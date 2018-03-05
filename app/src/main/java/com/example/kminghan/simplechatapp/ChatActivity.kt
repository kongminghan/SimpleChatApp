package com.example.kminghan.simplechatapp

import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.NavUtils
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.AccessToken
import com.sendbird.android.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.toolbar_chat.*
import kotlinx.android.synthetic.main.dialog_change_group_name.*
import com.sendbird.android.OpenChannel
import com.sendbird.android.User
import com.sendbird.android.GroupChannel
import com.sendbird.android.BaseChannel
import com.sendbird.android.BaseMessage
import com.sendbird.android.SendBird
import kotlinx.android.synthetic.main.dialog_change_group_name.view.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

class ChatActivity : AppCompatActivity() {
    private var userId: String = ""
    private var groupChannel: GroupChannel? = null
    private val TYPE_SENT: Int = 0
    private val TYPE_RECEIVED: Int = 1
    private val TYPE_RECEIVED_UNREAD: Int = 2
    private val TYPE_RECEIVED_DATE: Int = 3
    private val TYPE_PRIVATE: Int = 1
    private val TYPE_GROUP: Int = 2

    private val CHANNEL_HANDLER_ID = "CHAT_CHANNEL"
    private var channel_url: String = ""
    private var messageList: ArrayList<Message> = ArrayList()
    private var currDate: Date? = null
    private var messageAdapter: MessageAdapter? = null
    private var linearLayoutManager: LinearLayoutManager? = null
    private var type: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        userId = AccessToken.getCurrentAccessToken().userId.toString()
        currDate =getStartOfDay(Date())
        val image_url = intent.getStringExtra("profileUrl")
        val name = intent.getStringExtra("name")
        channel_url = intent.getStringExtra("channelUrl")
        type = intent.getIntExtra("type", -1)
        val lastSeen: String = intent.getStringExtra("lastSeen")

        when (lastSeen) {
            "Online" -> action_bar_title_2.text = lastSeen
            "" -> action_bar_title_2.visibility = View.GONE
            else -> action_bar_title_2.text = getLastSeen(lastSeen)
        }

        if(type == TYPE_PRIVATE) {
            if(image_url == ""){
                GlideApp.with(applicationContext)
                        .load(resources.getDrawable(R.drawable.ic_person))
                        .apply(RequestOptions().circleCrop())
                        .into(conversation_contact_photo)
            }
        }
        else {
            if(image_url == ""){
                GlideApp.with(applicationContext)
                        .load(resources.getDrawable(R.drawable.ic_group))
                        .apply(RequestOptions().circleCrop())
                        .into(conversation_contact_photo)
            }
        }

        GlideApp.with(applicationContext)
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

        et_chat.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                btn_send.isEnabled = p0.toString().isNotEmpty()
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(type == TYPE_PRIVATE)
            menuInflater.inflate(R.menu.menu_chat, menu)
        else
            menuInflater.inflate(R.menu.menu_group_chat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item != null) {
            val id = item.itemId
            if(id == R.id.miLeaveChannel) {
                leaveChannel(groupChannel!!)
                onBackPressed()
                return true
            }else if(id == R.id.miChangeName){
                changeGroupNameDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun changeGroupNameDialog(){
        var positiveBtn: Button? = null
        val layoutInflaterAndroid = LayoutInflater.from(this@ChatActivity)
        val mView = layoutInflaterAndroid.inflate(R.layout.dialog_change_group_name, null)
        val alertDialogBuilderUserInput = AlertDialog.Builder(this@ChatActivity)

        mView.etName.addTextChangedListener(object: TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                positiveBtn!!.isEnabled = p0.toString().isNotEmpty()
            }
        })

        alertDialogBuilderUserInput.setView(mView)
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Change", {
                    _, _ ->
                    groupChannel!!.updateChannel(true, mView.etName.text.toString(), null, null,
                            GroupChannel.GroupChannelUpdateHandler{
                                _, e ->
                                if(e!=null){
                                    e.printStackTrace()
                                }
                                action_bar_title_1.text = mView.etName.text.toString()
                            })
                })
                .setNegativeButton("Cancel", {
                    dialogBox, _ ->
                    dialogBox.cancel()
                })
        val alertDialogAndroid = alertDialogBuilderUserInput.create()
        alertDialogAndroid.setOnShowListener {
            dialogInterface ->
            positiveBtn = (dialogInterface as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
            positiveBtn!!.isEnabled = false
        }
        alertDialogAndroid.show()
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
                sendBirdException.printStackTrace()
                return@SendUserMessageHandler
            }
            messageList.add(Message(userMessage.message, userMessage.createdAt.toString(), TYPE_SENT, groupChannel!!.unreadMessageCount, type!!, ""))
            messageAdapter!!.notifyItemInserted(messageList.size -1)
            et_chat.text.clear()
        })
    }

    private fun retrieveMessage(){
        groupChannel!!.getPreviousMessagesByTimestamp(Long.MAX_VALUE, true, 50, false,
                BaseChannel.MessageTypeFilter.USER, null, BaseChannel.GetMessagesHandler{
            list: List<BaseMessage>, e ->
            if (e != null) {
                e.printStackTrace()
                return@GetMessagesHandler;
            }

            val unreadMessageCount = groupChannel!!.unreadMessageCount
            var isDateCreated = false
            var isTodayCreated = false
            var currMessageDate: Date = Date()
            var sdf = SimpleDateFormat("dd MMMM yyyy")

            list.map { (it as UserMessage) }.forEach {
                val newDate = Date(it.createdAt + 28800)

                if(!isDateCreated) {
                    currMessageDate = Date((it.createdAt + 28800))

                    //if first date is today
                    if(!currMessageDate.before(currDate)) {
                        messageList.add(Message("TODAY", "", TYPE_RECEIVED_DATE, -1, type!!, ""))
                        isTodayCreated = true
                    }
                    else {
                        messageList.add(Message(sdf.format(currMessageDate), "", TYPE_RECEIVED_DATE, -1, type!!, ""))
                    }
                    isDateCreated = true
                }

                when {
                    //if today
                    (!isTodayCreated && !newDate.before(currDate)) -> {
                        messageList.add(Message("TODAY", "", TYPE_RECEIVED_DATE, -1, type!!, ""))
                        isTodayCreated = true
                    }
                    //for each date
                    newDate.day != currMessageDate.day -> {
                        messageList.add(Message(sdf.format(newDate), "", TYPE_RECEIVED_DATE, -1, type!!, ""))
                        currMessageDate = newDate
                    }
                }

                if(it.sender.userId == userId)
                    messageList.add(Message(it.message, it.createdAt.toString(), TYPE_SENT, -1, type!!, it.sender.nickname))
                else
                    messageList.add(Message(it.message, it.createdAt.toString(), TYPE_RECEIVED, -1, type!!, it.sender.nickname))
            }

            if(unreadMessageCount > 0){
                messageList.add(messageList.size - unreadMessageCount, Message("", "",
                        TYPE_RECEIVED_UNREAD, unreadMessageCount, type!!, ""))
            }

            messageAdapter!!.notifyDataSetChanged()
            rv_chat.scrollToPosition(messageList.size - 1)
            groupChannel!!.markAsRead()
        })
    }

    private fun channelHandler(){
        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, object : SendBird.ChannelHandler() {
            override fun onMessageReceived(baseChannel: BaseChannel, baseMessage: BaseMessage) {
                if (baseChannel.url == channel_url) {
                    val userMessage = (baseMessage as UserMessage)

                    messageList.add(Message(userMessage.message, userMessage.createdAt.toString(), TYPE_RECEIVED,
                            groupChannel!!.unreadMessageCount, type!!,  userMessage.sender.nickname))
                    messageAdapter!!.notifyItemInserted(messageList.size - 1)
                    rv_chat.scrollToPosition(messageList.size - 1)
                }
            }

//            override fun onChannelChanged(channel: BaseChannel?) {
//
//            }
        })
    }

    public override fun onPause() {
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID)
        super.onPause()
    }

    private fun getLastSeen(s: String): String? {
        return try {
            val newDate = Date(s.toLong())
            var sdf = SimpleDateFormat("hh:mm a")
            sdf.timeZone = TimeZone.getTimeZone("GMT+8")

            if(!newDate.before(currDate)){
                "Last seen today "+ sdf.format(newDate)
            } else{
                sdf.applyPattern("dd/MM/yyyy HH:mm")
                "Last seen at " + sdf.format(newDate)
            }
        } catch (e: Exception) {
            e.toString()
        }
    }

    private fun getStartOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }
}
