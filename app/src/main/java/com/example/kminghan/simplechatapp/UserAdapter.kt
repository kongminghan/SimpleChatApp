package com.example.kminghan.simplechatapp

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.bumptech.glide.request.RequestOptions
import com.sendbird.android.User
import kotlinx.android.synthetic.main.list_item_user.view.*

class UserAdapter (private val users: ArrayList<User>): RecyclerView.Adapter<UserAdapter.UserHolder>() {
    var selectedIDs: ArrayList<String> = ArrayList()
    var positiveBtn: Button? = null

    override fun onBindViewHolder(holder: UserAdapter.UserHolder, position: Int) {
        holder.bindChannel(users[position])
    }

    override fun getItemCount(): Int {
        return users.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.list_item_user, parent, false)
        return UserHolder(inflatedView)
    }

    fun getSelectedIds(): ArrayList<String>{
        return selectedIDs
    }

    fun getSelectedIdCount(): Int{
        return selectedIDs.size
    }

    fun setButton(button: Button) {
        positiveBtn = button
    }

    inner class UserHolder(v: View) : RecyclerView.ViewHolder(v) {
        private var view: View = v

        fun bindChannel(user: User) {
            if(user.profileUrl != "")
                GlideApp.with(view.context).load(user.profileUrl).apply(RequestOptions().circleCrop()).into(view.ivUser)
            else
                GlideApp.with(view.context).load(R.drawable.ic_person).apply(RequestOptions().circleCrop()).into(view.ivUser)

            view.chkUser.text = user.nickname

            view.chkUser.setOnCheckedChangeListener() {
                buttonView, isChecked ->
                if(isChecked) {
                    selectedIDs.add(user.userId)
                    if(getSelectedIdCount() > 0){
                        positiveBtn!!.isEnabled = true
                    }
                }
                else {
                    selectedIDs.remove(user.userId)
                    if(getSelectedIdCount() == 0) {
                        positiveBtn!!.isEnabled = false
                    }
                }
            }
        }
    }
}