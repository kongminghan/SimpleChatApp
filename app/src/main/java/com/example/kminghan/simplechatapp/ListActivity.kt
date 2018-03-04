package com.example.kminghan.simplechatapp

import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_list.*
import android.os.Bundle
import android.app.AlertDialog
import android.content.Intent
import android.opengl.Visibility
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.facebook.login.LoginManager
import com.google.firebase.iid.FirebaseInstanceId
import com.sendbird.android.*
import com.sendbird.android.User
import com.sendbird.android.UserListQuery
import com.sendbird.android.SendBird

class ListActivity : AppCompatActivity() {
    private var linearLayoutManager: LinearLayoutManager? = null
    private var userLinearLayoutManager: LinearLayoutManager? = null
    private var adapter: ChannelAdapter? = null
    private var userAdapter: UserAdapter? = null
    private var userId: String = ""
    private var chatList: ArrayList<Channel> = ArrayList()
    private var userList: ArrayList<User> = ArrayList()

    private val TYPE_PRIVATE: Int = 1;
    private val TYPE_GROUP: Int = 2;
    private val CHANNEL_HANDLER_ID = "CHAT_CHANNEL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        setSupportActionBar(toolbar)

        linearLayoutManager = LinearLayoutManager(applicationContext, LinearLayout.VERTICAL, false)
        rv.layoutManager = linearLayoutManager
        adapter = ChannelAdapter(chatList)
        rv.adapter = adapter

        userId = intent.getStringExtra("id");

        SendBird.init(getString(R.string.app_id), applicationContext)
        SendBird.connect(userId, SendBird.ConnectHandler { _, e ->
            if (e != null) {
                Toast.makeText(applicationContext,  "Failed to connect to SendBird", Toast.LENGTH_SHORT).show()
                return@ConnectHandler
            }

            SendBird.registerPushTokenForCurrentUser(FirebaseInstanceId.getInstance().getToken(),
                    SendBird.RegisterPushTokenWithStatusHandler {
                        _, e ->
                        if (e != null) {
                            e.printStackTrace()
                            return@RegisterPushTokenWithStatusHandler
                        }
                    })

            swipeContainer.isRefreshing = true
            rv.visibility = View.GONE
            //refresh()
        })

        fab.setOnClickListener {
            setupDialog()
        }

        swipeContainer.setOnRefreshListener {
            refresh()
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item!!.itemId
        if(id == R.id.miSignOut) {
            LoginManager.getInstance().logOut()
            SendBird.disconnect {  }
            Thread(Runnable {
                startActivity(Intent(applicationContext, LoginActivity::class.java))
            }).start()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupDialog() {
        val layoutInflaterAndroid = LayoutInflater.from(this@ListActivity)
        val mView = layoutInflaterAndroid.inflate(R.layout.dialog_create_channel, null)
        val rv_user = mView.findViewById<RecyclerView>(R.id.rv_user)

        userLinearLayoutManager = LinearLayoutManager(applicationContext, LinearLayout.VERTICAL, false)
        rv_user.layoutManager = userLinearLayoutManager
        userAdapter = UserAdapter(userList)
        rv_user.adapter = userAdapter
        userAdapter!!.notifyDataSetChanged()

        val alertDialogBuilderUserInput = AlertDialog.Builder(this@ListActivity)
        alertDialogBuilderUserInput.setView(mView)

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Create", {
                    _, _ ->
                    if(userAdapter!!.getSelectedIdCount() > 0) {
                        val channelType = if(userAdapter!!.getSelectedIdCount() == 2)
                            "private"
                        else
                            "Group Channel"

                        GroupChannel.createChannelWithUserIds(userAdapter!!.getSelectedIds(), true, channelType, null, null, GroupChannel.GroupChannelCreateHandler { groupChannel, e ->
                            if (e != null) {
                                e.printStackTrace()
                                return@GroupChannelCreateHandler
                            }

                            var isAdded: Boolean = false
                            var chn: Channel? = null

                            val filteredList: Channel? = chatList.find { it.channelUrl == groupChannel.url }

                            if(filteredList == null) {
                                for(c in chatList){
                                    if(c.channelUrl != groupChannel.url){
                                        val members = groupChannel.members

                                        if(groupChannel.memberCount > 2) {
                                            chn = Channel(groupChannel.name, "", groupChannel.coverUrl, groupChannel.url,"", TYPE_GROUP)
                                            isAdded = true
                                        }else {
                                            for(member in members){
                                                if(member.userId != userId)
                                                    chn = Channel(member.nickname, "", member.profileUrl, groupChannel.url,"", TYPE_PRIVATE)
                                                isAdded = true
                                            }
                                        }
                                        break
                                    }
                                }
                                chatList.add(chn!!)
                                checkEmptyState()
                                adapter!!.notifyDataSetChanged()
                            }
                            else
                                Toast.makeText(this@ListActivity, "Duplicate channel is found!", Toast.LENGTH_SHORT).show()
                        })
                    }
                })

                .setNegativeButton("Cancel", {
                    dialogBox, _ ->
                    dialogBox.cancel()
                })

        val alertDialogAndroid = alertDialogBuilderUserInput.create()
        alertDialogAndroid.setOnShowListener {
            dialogInterface ->
            val positiveBtn = (dialogInterface as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)

            userAdapter!!.setButton(positiveBtn)
            positiveBtn.isEnabled = false
        }
        alertDialogAndroid.show()
    }

    override fun onResume() {
        super.onResume()

        SendBird.connect(userId, SendBird.ConnectHandler { _, e ->
            if (e != null) {
                Toast.makeText(applicationContext, "Failed to connect to SendBird", Toast.LENGTH_SHORT).show()
                return@ConnectHandler
            }
            refresh()
            initUsers()
        })
    }

    private fun checkEmptyState() {
        if(chatList.size > 0){
            emptyLayout.visibility = View.GONE
            rv.visibility = View.VISIBLE
        }
        else {
            emptyLayout.visibility = View.VISIBLE
            rv.visibility = View.GONE
        }
    }

    private fun refresh(){
        swipeContainer.isRefreshing = true
        chatList.clear()
        val channelList = GroupChannel.createMyGroupChannelListQuery()
        channelList.setLimit(50)
        channelList.setIncludeEmpty(true);
        channelList.next { groupChannelList: List<GroupChannel>, sendBirdException ->
            if(sendBirdException!=null){
                Toast.makeText(applicationContext, sendBirdException.toString(), Toast.LENGTH_LONG).show()
                return@next;
            }

            for(myChannel in groupChannelList)
            {
                if(myChannel.memberCount == 2){
                    myChannel.members
                            .filterNot { it.userId == userId }
                            .forEach {
                                val lastSeenAt: String = when {
                                    it.connectionStatus == User.ConnectionStatus.ONLINE -> "Online"
                                    else -> it.lastSeenAt.toString()
                                }

                                val lastMsg: String = when{
                                    (myChannel.lastMessage != null) -> (myChannel.lastMessage as UserMessage).message
                                    else -> ""
                                }

                                chatList.add(Channel(it.nickname, lastMsg, it.profileUrl, myChannel.url,
                                        lastSeenAt, TYPE_PRIVATE)) }
                }
                else if (myChannel.memberCount > 2){
                    val lastMsg: String = if(myChannel.lastMessage != null)
                        (myChannel.lastMessage as UserMessage).message else ""

                    chatList.add(Channel(myChannel.name, lastMsg, myChannel.coverUrl, myChannel.url,
                            "", TYPE_GROUP))
                }
            }
            checkEmptyState()
            adapter!!.notifyDataSetChanged()
            swipeContainer.isRefreshing = false
        }
    }

    private fun initUsers(){
        val mUserListQuery = SendBird.createUserListQuery()
        mUserListQuery.next(UserListQuery.UserListQueryResultHandler { list, e ->
            if (e != null) {
                e.printStackTrace()
                return@UserListQueryResultHandler
            }

            userList = list.filterNot { it.userId == userId } as ArrayList<User>
        })
    }
}