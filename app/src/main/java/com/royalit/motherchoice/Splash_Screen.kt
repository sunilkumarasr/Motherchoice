package com.royalit.motherchoice

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import com.erepairs.app.api.Api
import com.erepairs.app.models.AppMaintananceResponseModel
import com.royalit.motherchoice.api.APIClient
import com.royalit.motherchoice.models.CartListResponse
import com.royalit.motherchoice.roomdb.CartViewModel
import com.royalit.motherchoice.utils.NetWorkConection
import com.royalit.motherchoice.utils.Utilities
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Console
import java.lang.Exception

class   Splash_Screen : Activity() {
    lateinit var sharedPreferences: SharedPreferences
    private var isLogined: Boolean = false
    var isActive=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_screen)

       /* window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )*/

        sharedPreferences = getSharedPreferences("loginprefs", Context.MODE_PRIVATE)
        checkAppMaintanance()
        Log.d("Message ", sharedPreferences.toString())
       /* Handler(Looper.getMainLooper()).postDelayed({

            isLogined = sharedPreferences.getBoolean("islogin", false)
            Log.d("isLogined","isLogined $isLogined")
            if (isLogined) {
                val intent = Intent(this, HomeScreen::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, First_Screen::class.java)
                startActivity(intent)
                finish()
            }


        }, 3000)*/


//        Handler().postDelayed({
//
//            sharedPreferences = getSharedPreferences("loginprefs", Context.MODE_PRIVATE)

//            image.visibility = View.VISIBLE
//            val anim = ValueAnimator.ofFloat(2f, 2.5f)
//            anim.duration = 4000
//            anim.addUpdateListener { animation ->
//                image.scaleX = (animation.animatedValue as Float)
//                image.scaleY = (animation.animatedValue as Float)
//            }
//            anim.repeatCount = 1
//            anim.repeatMode = ValueAnimator.REVERSE
//            anim.start()
//            image.startAnimation(AnimationUtils.loadAnimation(this, R.anim.zoomin))

//            isLogined = sharedPreferences.getBoolean("islogin", false)
//            if (isLogined) {
//                val intent = Intent(this, HomeScreen::class.java)
//                startActivity(intent)
//                finish()
//            } else {
//                val intent = Intent(this, First_Screen::class.java)
//                startActivity(intent)
//                finish()
//            }
//
//
//        }, 3000)
    }

    private fun checkAppMaintanance() {

        if (NetWorkConection.isNEtworkConnected(this@Splash_Screen)) {

            val apiServices = APIClient.client.create(Api::class.java)
            var call=  apiServices.checkAppMaintanance(getString(R.string.api_key))


            call?.enqueue(object : Callback<AppMaintananceResponseModel> {
                @SuppressLint("WrongConstant")
                override fun onResponse(call: Call<AppMaintananceResponseModel>, response: Response<AppMaintananceResponseModel>) {
                    Log.e(ContentValues.TAG, response.toString())

                    if (response.isSuccessful) {
                        try {
                           val list= response.body()?.response
                            if(list!=null&&list.size>0)
                            {
                                if(list.get(0).message!="online")
                                {
                                    isActive=false
                                }
                            }

                            if(isActive)
                            {
                                isLogined = sharedPreferences.getBoolean("islogin", false)
                                Log.d("isLogined","isLogined $isLogined")
                                if (isLogined) {
                                    val intent = Intent(this@Splash_Screen, HomeScreen::class.java)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    val intent = Intent(this@Splash_Screen, First_Screen::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                            }else
                            {
                                val intent = Intent(this@Splash_Screen, AppMaintananceActivity::class.java)
                                startActivity(intent)
                                finish()
                            }


                        } catch (e: java.lang.NullPointerException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<AppMaintananceResponseModel>, t: Throwable) {
                    Log.e(ContentValues.TAG, t.toString())

                    isLogined = sharedPreferences.getBoolean("islogin", false)
                    Log.d("isLogined","isLogined $isLogined")
                    if (isLogined) {
                        val intent = Intent(this@Splash_Screen, HomeScreen::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val intent = Intent(this@Splash_Screen, First_Screen::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            })
        } else {
            Toast.makeText(
                this@Splash_Screen,
                "Please Check your internet",
                Toast.LENGTH_LONG
            ).show()
        }


    }
}