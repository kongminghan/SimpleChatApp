package com.example.kminghan.simplechatapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.facebook.AccessToken
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.toolbar_chat.*


class ChatActivity : AppCompatActivity() {
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        Thread(Runnable {
            userId = AccessToken.getCurrentAccessToken().userId.toString()
        })
        val image_url = intent.getStringExtra("profileUrl")
        val channel_url = intent.getStringExtra("channelUrl")
        val name = intent.getStringExtra("name")

        Glide.with(applicationContext)
                .load(image_url)
                .apply(RequestOptions().circleCrop())
                .into(conversation_contact_photo)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayUseLogoEnabled(true);

        action_bar_title_1.text = name
        action_bar_title_2.text = "Last seen at 11.11AM"


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
            when(item.itemId) {
                R.id.home -> {
                    val intent = Intent(applicationContext, ListActivity::class.java)
                    intent.putExtra("id", userId)
                    startActivity(intent)
                }

            }
        }

        return super.onOptionsItemSelected(item)
    }
}
