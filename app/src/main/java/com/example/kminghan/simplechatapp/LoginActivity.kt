package com.example.kminghan.simplechatapp

import android.app.ProgressDialog
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import java.util.*
import android.content.Intent
import android.util.Log
import com.facebook.*
import com.sendbird.android.SendBird.ConnectHandler
import com.facebook.GraphResponse
import org.json.JSONObject
import com.facebook.GraphRequest
import java.net.MalformedURLException
import java.net.URL
import android.provider.SyncStateContract.Helpers.update
import android.content.pm.PackageManager
import android.content.pm.PackageInfo
import android.view.ContextThemeWrapper
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.iid.FirebaseInstanceId
import com.sendbird.android.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class LoginActivity : AppCompatActivity() {

    private var callbackManager: CallbackManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(applicationContext)

        val accessToken = AccessToken.getCurrentAccessToken()
        if(accessToken != null) {
            val request = GraphRequest.newMeRequest(accessToken) {
                jsonObject, _ ->
                val facebookData = getFacebookData(jsonObject)
                goToNextActivity(facebookData!!)
                finish()
            }
            request.executeAsync()
        }
        else
        {
            setContentView(R.layout.activity_login)
            callbackManager = CallbackManager.Factory.create()

            val loginButton = findViewById<View>(R.id.login_button) as LoginButton
            loginButton.setReadPermissions(Arrays.asList("public_profile","email"));

            loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {

                    // make request to get facebook user info
                    val request = GraphRequest.newMeRequest(loginResult.accessToken) {
                        jsonObject, _ ->
                        // Getting FB User Data
                        val facebookData = getFacebookData(jsonObject)

                        SendBird.init(getString(R.string.app_id), applicationContext)
                        SendBird.connect(loginResult.accessToken.userId, SendBird.ConnectHandler { _, e ->
                            if (e != null) {
                                Toast.makeText(applicationContext, "Failed to connect to SendBird", Toast.LENGTH_SHORT).show()
                                return@ConnectHandler
                            }

                            if (FirebaseInstanceId.getInstance().token == null) return@ConnectHandler;

                            SendBird.registerPushTokenForCurrentUser(FirebaseInstanceId.getInstance().getToken(),
                                    SendBird.RegisterPushTokenWithStatusHandler {
                                        status, e ->
                                        if (e != null) {
                                            e.printStackTrace()
                                            return@RegisterPushTokenWithStatusHandler
                                        }
                                    })

                            SendBird.updateCurrentUserInfo(facebookData!!["name"].toString(), facebookData["profile_pic"].toString(), SendBird.UserInfoUpdateHandler { e ->
                                if (e != null) {
                                    Toast.makeText(applicationContext, "" + e.code + ":" + e.message, Toast.LENGTH_SHORT).show()
                                    return@UserInfoUpdateHandler
                                }
                                goToNextActivity(facebookData!!);
                                finish()
                            })
                        })
                    }
                    request.executeAsync()
                }
                override fun onCancel() {}

                override fun onError(exception: FacebookException) {
                    Toast.makeText(applicationContext,  "Error", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        callbackManager!!.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun goToNextActivity(facebookData: Bundle) {
        val intent = Intent(applicationContext, ListActivity::class.java)
        intent.putExtra("id", facebookData!!["idFacebook"].toString())
        intent.putExtra("profile_pic", facebookData["profile_pic"].toString())
        intent.putExtra("name", if (facebookData["name"] != null) facebookData["name"].toString() else "")
        startActivity(intent)
    }

    private fun getFacebookData(`object`: JSONObject): Bundle? {
        val bundle = Bundle()

        try {
            val id = `object`.getString("id")
            val profile_pic: URL
            try {
                profile_pic = URL("https://graph.facebook.com/$id/picture?type=large")
                bundle.putString("profile_pic", profile_pic.toString())
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                return null
            }

            bundle.putString("idFacebook", id)
            if (`object`.has("name"))
                bundle.putString("name", `object`.getString("name"))

//            prefUtil.saveFacebookUserInfo(`object`.getString("first_name"),
//                    `object`.getString("last_name"), `object`.getString("email"),
//                    `object`.getString("gender"), profile_pic.toString())

        } catch (e: Exception) {
            Log.d("TAG", "BUNDLE Exception : " + e.toString())
        }

        return bundle
    }
}
