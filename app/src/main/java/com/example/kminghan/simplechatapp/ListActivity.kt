package com.example.kminghan.simplechatapp

import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_list.*
import kotlinx.android.synthetic.main.dialog_create_channel.*
import android.os.Bundle
import android.app.AlertDialog
import android.content.Intent
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.facebook.login.LoginManager

import com.sendbird.android.OpenChannel
import com.sendbird.android.SendBird
import com.sendbird.android.SendBirdException
import com.sendbird.android.OpenChannelListQuery
import com.sendbird.android.GroupChannel
import com.sendbird.android.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener

class ListActivity : AppCompatActivity() {
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RecyclerAdapter
    private lateinit var myRef: DatabaseReference
    private lateinit var userId: String
    private var chatList: ArrayList<Channel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        setSupportActionBar(toolbar)

        linearLayoutManager = LinearLayoutManager(applicationContext, LinearLayout.VERTICAL, false)
//        linearLayoutManager.reverseLayout = true
        rv.layoutManager = linearLayoutManager
        adapter = RecyclerAdapter(chatList)
        rv.adapter = adapter

        userId = intent.getStringExtra("id");
        //val name = intent.getStringExtra("name")

        SendBird.init(getString(R.string.app_id), applicationContext)
        SendBird.connect(userId, SendBird.ConnectHandler { user, e ->
            if (e != null) {
                Toast.makeText(applicationContext,  "Failed to connect to SendBird", Toast.LENGTH_SHORT).show()
                return@ConnectHandler
            }

            //            SendBird.updateCurrentUserInfo(name, facebookData["profile_pic"].toString(), SendBird.UserInfoUpdateHandler { e ->
            //                if (e != null) {
            //                    // Error!
            //                    Toast.makeText(applicationContext, "" + e.code + ":" + e.message, Toast.LENGTH_SHORT).show()
            //                    return@UserInfoUpdateHandler
            //                }
            //            })
        })

        val database = FirebaseDatabase.getInstance()
        myRef = database.getReference(userId)

        myRef.child("channel").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                chatList.clear()
                dataSnapshot.children.mapNotNullTo(chatList){
                    it.getValue<Channel>(Channel::class.java)
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })

//        myRef.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                for (postSnapshot in dataSnapshot.children) {
//                    val channel = dataSnapshot.getValue(String::class.java)
//
//                    val iDs: MutableList<String> = mutableListOf(userId, "123")
//
//                    GroupChannel.createChannelWithUserIds(iDs, true, GroupChannel.GroupChannelCreateHandler { groupChannel, e ->
//                        if (e != null) {
//                            Toast.makeText(applicationContext, e.toString(), Toast.LENGTH_LONG).show()
//                            return@GroupChannelCreateHandler
//                        }
//                        val members = groupChannel.getMembers()
//                        for(member in members){
//                            chatList.add(Channel(member.nickname, "last msg", member.profileUrl))
//                        }
//                        adapter.notifyDataSetChanged()
//                    })
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {}
//        })

        fab.setOnClickListener {
            setupDialog()
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
            Thread(Runnable {
                startActivity(Intent(applicationContext, LoginActivity::class.java))
            }).start()
        }
        return super.onOptionsItemSelected(item)
    }

    fun setupDialog() {
        val layoutInflaterAndroid = LayoutInflater.from(this@ListActivity)
        val mView = layoutInflaterAndroid.inflate(R.layout.dialog_create_channel, null)
        val input = mView.findViewById<EditText>(R.id.userInputDialog)
        val alertDialogBuilderUserInput = AlertDialog.Builder(this@ListActivity)
        alertDialogBuilderUserInput.setView(mView);

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Send", {
                    dialogBox, id ->

                    val iDs: MutableList<String> = mutableListOf(userId, input.text.toString())
                    GroupChannel.createChannelWithUserIds(iDs, true, GroupChannel.GroupChannelCreateHandler { groupChannel, e ->
                        if (e != null) {
                            Toast.makeText(this@ListActivity, e.toString(), Toast.LENGTH_LONG).show()
                            return@GroupChannelCreateHandler
                        }

                        val members = groupChannel.getMembers()
                        for(member in members){
                            if(!member.userId.equals(userId)) {
                                val chn = Channel(member.nickname, "last msg", member.profileUrl, groupChannel.url)
                                chatList.add(chn)
                                myRef.child("channel").push().setValue(chn)
                            }
                        }
                        adapter.notifyDataSetChanged()
                    })
                })

                .setNegativeButton("Cancel", {
                    dialogBox, id ->
                    dialogBox.cancel();
                })

        val alertDialogAndroid = alertDialogBuilderUserInput.create();
        alertDialogAndroid.show();
    }
}