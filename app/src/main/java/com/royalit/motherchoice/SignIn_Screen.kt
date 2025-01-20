package com.royalit.motherchoice

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import com.erepairs.app.api.Api
import com.google.firebase.iid.FirebaseInstanceId
import com.royalit.motherchoice.api.APIClient
import com.royalit.motherchoice.databinding.LoginScreenBinding
import com.royalit.motherchoice.models.LoginList_Response
import com.royalit.motherchoice.utils.NetWorkConection
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 *  Created by Sucharitha Peddinti on 27/11/21.
 */
class SignIn_Screen : Activity() {
    private lateinit var binding: LoginScreenBinding
    lateinit var usermobile_stng: String
    lateinit var password_stng: String
    lateinit var sharedPreferences: SharedPreferences
    lateinit var token: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences =
            getSharedPreferences(
                "loginprefs",
                Context.MODE_PRIVATE
            )
        binding.loginBtn.setOnClickListener {

            val intent = Intent(
                this@SignIn_Screen,
                HomeScreen::class.java
            )
            startActivity(intent)
            finish()
        }
        binding.createacc.setOnClickListener {

            val intent = Intent(
                this@SignIn_Screen,
                Signup_Screen::class.java
            )
            startActivity(intent)
            finish()
        }
        binding.forgotBtn.setOnClickListener {

//            val intent = Intent(
//                this@SignIn_Screen,
//                ForgotPassward_Screen::class.java
//            )
//            startActivity(intent)
//            finish()

            showDialog()

        }

        token = FirebaseInstanceId.getInstance().token.toString()
        binding.loginBtn.setOnClickListener {

            usermobile_stng = binding.usernameSignin.text.toString().trim()
            password_stng = binding.passwordSignin.text.toString().trim()
            if (usermobile_stng.isEmpty()) {
                binding.usernameSignin.error = "Please Enter UserName"
                binding.usernameSignin.requestFocus()

            } else if (password_stng.isEmpty()) {
                binding.passwordSignin.error = "Please Enter Password"

            } else {
                postuser_Login()
            }


        }


        //Get the token
        //Use the token only for received a push notification this device

        Log.d(ContentValues.TAG, "Token : $token")

    }

    private fun showDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.forgotpasswo_screen)
        val phonenum = dialog.findViewById(R.id.phone_text) as TextView
        val ok_btn = dialog.findViewById(R.id.ok_btn) as TextView

        phonenum.setOnClickListener {
            val dialIntent = Intent(Intent.ACTION_DIAL)
            dialIntent.data = Uri.parse("tel:" + "9154102137")
            startActivity(dialIntent)
        }
        ok_btn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()

    }

    private fun postuser_Login() {

        if (NetWorkConection.isNEtworkConnected(this)) {

            //Set the Adapter to the RecyclerView//
            binding.progressBarlogin.visibility = View.VISIBLE


            var apiServices = APIClient.client.create(Api::class.java)

            if(token==null||token.isEmpty())
                token = FirebaseInstanceId.getInstance().token.toString()

            val call =
                apiServices.postlogin(
                    getString(R.string.api_key),
                    usermobile_stng,
                    password_stng, token
                )

            call.enqueue(object : Callback<LoginList_Response> {
                override fun onResponse(
                    call: Call<LoginList_Response>,
                    response: Response<LoginList_Response>
                ) {

                    binding.progressBarlogin.visibility = View.GONE
                    Log.e(ContentValues.TAG, response.toString())

                    try {
                        if (response.isSuccessful) {

                            //Set the Adapter to the RecyclerView//
if(response.body()?.status == false)
{
    Toast.makeText(applicationContext,"User InActive / ${response.body()?.message}",Toast.LENGTH_LONG).show()
    return
}

                            val intent =
                                Intent(this@SignIn_Screen, HomeScreen::class.java)
                            startActivity(intent)
                            finish()

                            Log.e("login", "" + response.message())
                            //postOtp()

                            val editor = sharedPreferences.edit()
                            editor.putString(
                                "userid",
                                response.body()?.response!!.customer_id.toString()
                            )
                            editor.putString(
                                "address",
                                response.body()?.response!!.address.toString()
                            )
                            editor.putString(
                                "username",
                                response.body()?.response!!.full_name.toString()
                            )
                            editor.putBoolean("islogin", true)

                            editor.putString(
                                "mobilenumber",
                                response.body()!!.response.mobile_number
                            )
                            editor.putInt(
                                "customer_category",
                                response.body()!!.response.customer_category
                            )
                            editor.commit()
                        } else {
                            Toast.makeText(
                                this@SignIn_Screen,
                                "Invalid Mobile Number",
                                Toast.LENGTH_LONG
                            ).show()

                        }
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    } catch (e: TypeCastException) {
                        e.printStackTrace()
                    }


                }


                override fun onFailure(call: Call<LoginList_Response>, t: Throwable) {
                    binding.progressBarlogin.visibility = View.GONE
                    Log.e(ContentValues.TAG, t.toString())
                    Toast.makeText(
                        this@SignIn_Screen,
                        "Invalid Credentials..",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })


        } else {
            Toast.makeText(this, "Please Check your internet", Toast.LENGTH_LONG).show()

        }

    }


}