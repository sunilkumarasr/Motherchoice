package com.royalit.motherchoice.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.erepairs.app.api.Api
import com.royalit.motherchoice.R
import com.royalit.motherchoice.api.APIClient
import com.royalit.motherchoice.databinding.MyprofileScreenBinding
import com.royalit.motherchoice.models.LoginList_Response
import com.royalit.motherchoice.utils.NetWorkConection
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyProfile_Fragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: MyprofileScreenBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var customerid: String
    lateinit var sharedPreferences: SharedPreferences
    lateinit var root: View
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        _binding = MyprofileScreenBinding.inflate(inflater, container, false)
        root = binding.root
        sharedPreferences =
            requireContext().getSharedPreferences(
                "loginprefs",
                Context.MODE_PRIVATE
            )

        customerid = sharedPreferences.getString("userid", "").toString()

        Log.e("customid", "" + customerid)
        getProfile()
        return root
    }

    private fun getProfile() {
        try {
            if (NetWorkConection.isNEtworkConnected(activity as Activity)) {
                val apiServices = APIClient.client.create(Api::class.java)
                val call = apiServices.getprofile(getString(R.string.api_key), customerid)

                binding.progressProfile.visibility = View.VISIBLE

                call.enqueue(object : Callback<LoginList_Response> {
                    @SuppressLint("WrongConstant")
                    override fun onResponse(
                        call: Call<LoginList_Response>,
                        response: Response<LoginList_Response>
                    ) {
                        Log.e(ContentValues.TAG, response.toString())
                        binding.progressProfile.visibility = View.GONE

                        if (response.isSuccessful) {
                            try {
                                val listOfcategories = response.body()?.response

                                // Ensure binding is not null before accessing its properties
                                binding?.apply {
                                    nameProfile.text = "" + listOfcategories?.full_name
                                    emailProfile.text = "" + listOfcategories?.email_id
                                    mobileProfile.text = "" + listOfcategories?.mobile_number
                                    addressProfile.text = "" + listOfcategories?.address
                                }
                            } catch (e: java.lang.NullPointerException) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call<LoginList_Response>, t: Throwable) {
                        Log.e(ContentValues.TAG, t.toString())
                        binding?.progressProfile?.visibility = View.GONE
                    }
                })
            } else {
                Toast.makeText(
                    activity as Activity,
                    "Please Check your internet",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}