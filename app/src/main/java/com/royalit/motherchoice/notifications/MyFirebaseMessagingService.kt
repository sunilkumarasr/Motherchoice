package com.royalit.motherchoice.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.royalit.motherchoice.HomeScreen
import com.royalit.motherchoice.R

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.*
import java.lang.System.currentTimeMillis
import java.net.URL
import javax.net.ssl.HttpsURLConnection


/**
 *  Created by Sucharitha Peddinti on 17/10/21.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        lateinit var sharedPreferences: SharedPreferences

        private val TAG = "MyFirebaseToken"
        private lateinit var notificationManager: NotificationManager
        private lateinit var title: String
        private lateinit var notification_body: String
        private lateinit var userid: String
        var token: String? = null
        var body: String? = null
        var id: String? = null
        lateinit var context: Context


        fun sendMessage(title: String, content: String, topic: String) {
            GlobalScope.launch {
                val endpoint = "https://fcm.googleapis.com/fcm/send"
                try {
                    val url = URL(endpoint)
                    val httpsURLConnection: HttpsURLConnection =
                        url.openConnection() as HttpsURLConnection
                    httpsURLConnection.readTimeout = 10000
                    httpsURLConnection.connectTimeout = 15000
                    httpsURLConnection.requestMethod = "POST"
                    httpsURLConnection.doInput = true
                    httpsURLConnection.doOutput = true

                    // Adding the necessary headers
                    httpsURLConnection.setRequestProperty(
                        "authorization",
                        "key=${R.string.fcm_key}"
                    )
                    httpsURLConnection.setRequestProperty("Content-Type", "application/json")

                    // Creating the JSON with post params
                    val body = JSONObject()

                    val data = JSONObject()
                    data.put("title", title)
                    data.put("body", content)
                    // body.put("body", data)

                    body.put("to", "/topics/$topic")

                    val outputStream: OutputStream =
                        BufferedOutputStream(httpsURLConnection.outputStream)
                    val writer = BufferedWriter(OutputStreamWriter(outputStream, "utf-8"))
                    writer.write(body.toString())
                    writer.flush()
                    writer.close()
                    outputStream.close()
                    val responseCode: Int = httpsURLConnection.responseCode
                    val responseMessage: String = httpsURLConnection.responseMessage
                    Log.d("Response:", "$responseCode $responseMessage")
                    var result = String()
                    var inputStream: InputStream? = null

                    inputStream = if (responseCode in 400..499) {
                        httpsURLConnection.errorStream
                    } else {
                        httpsURLConnection.inputStream
                    }

                    if (responseCode == 200) {
                        Log.e("Success:", "notification sent $title \n $content")
                        // The details of the user can be obtained from the result variable in JSON format


                    } else {
                        Log.e("Error", "Error Response")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
//        fun sendMessage(title: String, content: String, topic: String) {
//            if (NetWorkConection.isNEtworkConnected(context)) {
//
//                //Set the Adapter to the RecyclerView//
//
//
//                var apiServices = APIClient.client.create(Api::class.java)
//
//                val call =
//                    apiServices.test_notification(userid)
//
  //             call.enqueue(object : Callback<NotificationListResponse> {
//                    @SuppressLint("StringFormatInvalid")
//                    override fun onResponse(
//                        call: Call<NotificationListResponse>,
//                        response: Response<NotificationListResponse>
//                    ) {
//
//                        Log.e(ContentValues.TAG, response.toString())
//
//                        if (response.isSuccessful) {
//
//                            val body = JSONObject()
////
//                            val data = JSONObject()
//                            data.put("title", response.body()!!.response.title)
//                            body.put("body", response.body()!!.response.body)
//
//                        }
//
////
//
//                    }
//
//
//                    override fun onFailure(call: Call<NotificationListResponse>, t: Throwable) {
//                        Log.e(ContentValues.TAG, t.toString())
//
//                    }
//
//                })
//
//
//            } else {
//
//                Toast.makeText(
//                    context,
//                    "Please Check your internet",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
    }


    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        Log.e("onMessageReceived: ", p0.data.toString())


        if (p0.data.isNotEmpty()) {
            try {
                val params: Map<String?, String?> = p0.data
                val json = JSONObject(params)
                val message = json.getString("message")
                var obj = JSONObject(message)
                val jsonString = message.substring(1, message.length - 1).replace("\\", "")
                val jsonObject = JSONTokener(message).nextValue() as JSONObject

                id = jsonObject.getString("id")
                title = jsonObject.getString("title")
                body = jsonObject.getString("body")
//
//                if ((id.equals("1"))){
//                    startActivity(Intent(context,Home_Screen::class.java))
//                }

                Log.e(TAG, "onMessageReceived: $json")
                Log.e("jsonString", "" + jsonString)
                Log.e("id", "" + id)
                //  handleDataMessage(json)
            } catch (e: JSONException) {
                Log.e(TAG, "Exception: " + e.message)
            }
        }


        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

//        Log.e("tilte", "" + title)
        val intent = Intent(this, HomeScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intent.putExtra("id", id)

       /* var pendingIntent = PendingIntent.getActivity(
            applicationContext, 0,
            intent,
            PendingIntent.FLAG_MUTABLE
        )*/
        val pendingIntent: PendingIntent
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                this,
                0, intent,  PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            checkNotificationChannel("1")
        }


        val notification = NotificationCompat.Builder(applicationContext, "1")
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val color = ContextCompat.getColor(this, R.color.red)
            notification.setColor(
                Color.RED

            )
//            notification.setColor(ContextCompat.getColor(this, R.color.red))


        } else {
            val color = ContextCompat.getColor(this, R.color.red)
            notification.setColor(
                Color.RED


            )
//            notification.setColor(ContextCompat.getColor(this, R.color.red))
        }
            .setSmallIcon(R.drawable.logo)
            .setContentTitle("MotherChoice")

            .setContentTitle(title)
            .setContentText(body)
            .setStyle(
                NotificationCompat.MessagingStyle(title!!)
                    .setGroupConversation(false)
                    .addMessage(body, currentTimeMillis(), title)
            )
            //.setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(defaultSound)

        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification.build())

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkNotificationChannel(CHANNEL_ID: String) {
        val notificationChannel = NotificationChannel(
            CHANNEL_ID,
            "Mother Choice",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationChannel.description = body
        notificationChannel.enableLights(true)

        notificationChannel.enableVibration(true)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(notificationChannel)
    }

    override fun onNewToken(p0: String) {
        token = p0
        super.onNewToken(p0)
    }
}