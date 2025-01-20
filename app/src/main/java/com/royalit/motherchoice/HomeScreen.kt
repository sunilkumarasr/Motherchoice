package com.royalit.motherchoice

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.MenuItemCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.erepairs.app.api.Api
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import com.royalit.motherchoice.api.APIClient
import com.royalit.motherchoice.databinding.ActionbarTitleBinding
import com.royalit.motherchoice.databinding.ActivityHomeScreenBinding
import com.royalit.motherchoice.models.CartListResponse
import com.royalit.motherchoice.models.LoginList_Response
import com.royalit.motherchoice.roomdb.AppConstants
import com.royalit.motherchoice.roomdb.CartViewModel
import com.royalit.motherchoice.utils.NetWorkConection
import com.royalit.motherchoice.utils.Utilities
import com.royalit.motherchoice.utils.Utilities.customerid
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeScreen : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener,
    PaymentResultWithDataListener {
    lateinit var navBottomView: BottomNavigationView
    lateinit var navView: NavigationView
    lateinit var profilepic: ImageView
    lateinit var profile_name: TextView
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityHomeScreenBinding
    lateinit var navController: NavController
    lateinit var drawerLayout: DrawerLayout
    lateinit var customerid: String
    lateinit var sharedPreferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarHomeScreen.toolbar)

        supportActionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar!!.setCustomView(R.layout.actionbar_title)
        drawerLayout = binding.drawerLayout
        navView = binding.navView
        navBottomView = findViewById(R.id.bottom_navigation_view)
        sharedPreferences =
            getSharedPreferences(
                "loginprefs",
                Context.MODE_PRIVATE
            )

        customerid = sharedPreferences.getString("userid", "")!!
        Utilities.customer_category = sharedPreferences.getInt("customer_category", 1)!!
        Utilities.customerid=customerid
        navController = findNavController(R.id.nav_host_fragment_content_home_screen)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.navigation_viewallproducts, R.id.nav_contactus
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navBottomView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(this)
        val headerView: View = binding.navView.getHeaderView(0)


        profile_name = headerView.findViewById(R.id.text_profile)
        profilepic = headerView.findViewById(R.id.imageView_home)
        getProfile()
        getCart()

    }


    fun onPhoneIconClick(view: View) {
        val phoneNumber = "9154102137" // Replace with your desired phone number
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        startActivity(intent)
    }
    fun onPhonenumbersclick(view: View) {
        val phoneNumber = "9154102137" // Replace with your desired phone number
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        startActivity(intent)
    }
 var  textCartItemCount:TextView?=null
 var  bagred_image:View?=null
 var  actionView:View?=null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home_screen, menu)
        val menuItem = menu.findItem(R.id.action_cart)
        try {
            MenuItemCompat.setActionView(menuItem, R.layout.custom_badgecount_layout)
             actionView = menuItem.actionView!!
             textCartItemCount = actionView?.findViewById<TextView>(R.id.cart_badge_count)
             bagred_image = actionView?.findViewById<ImageView>(R.id.bagred_image)
           /* val viewModel = CartViewModel(this)
            viewModel.cartCount()*/
            /*viewModel.cartCount.observe(this) { cartItems ->
                if (cartItems.size > 0) {
                    textCartItemCount.visibility = View.VISIBLE
                    bagred_image.visibility = View.VISIBLE
                    textCartItemCount.text = "" + cartItems.size
                } else {
                    textCartItemCount.visibility = View.GONE
                    bagred_image.visibility = View.GONE
                }
            }*/
            actionView?.setOnClickListener { onOptionsItemSelected(menuItem) }
        } catch (e: java.lang.NullPointerException) {
            e.printStackTrace()
        }
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_cart -> {

                navController.navigate(R.id.navigation_cart)


                return true
            }

            R.id.nav_search -> {

                navController.navigate(R.id.nav_search)


                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        item.isChecked = true
        drawerLayout.closeDrawers()

        val id = item.itemId

        when (id) {


            R.id.nav_contactus ->

                navController.navigate(R.id.nav_contactus)
            R.id.nav_home ->

                navController.navigate(R.id.nav_home)
            R.id.navigation_products ->

                navController.navigate(R.id.navigation_viewallproducts)
            R.id.navigation_cart ->

                navController.navigate(R.id.navigation_cart)

            R.id.navigation_account ->
                navController.navigate(R.id.navigation_account)
            R.id.nav_orderhis ->
                navController.navigate(R.id.navigation_orderhis)
            R.id.nav_logout ->
                logoutfun()


        }

        return true

    }

    fun logoutfun() {

        val editor=sharedPreferences.edit()
        editor.clear()
        editor.commit()
        //sharedPreferences.edit().clear().commit()
       // sharedPreferences.edit().clear().apply()

        intent = Intent(applicationContext, First_Screen::class.java)
        startActivity(intent)
        finish()

    }

    private fun getProfile() {
        try {


            if (NetWorkConection.isNEtworkConnected(this)) {

                //Set the Adapter to the RecyclerView//


                var apiServices = APIClient.client.create(Api::class.java)

                val call =
                    apiServices.getprofile(getString(R.string.api_key), customerid)

                call.enqueue(object : Callback<LoginList_Response> {
                    @SuppressLint("WrongConstant")
                    override fun onResponse(
                        call: Call<LoginList_Response>,
                        response: Response<LoginList_Response>
                    ) {

                        Log.e(ContentValues.TAG, response.toString())

                        if (response.isSuccessful) {

                            try {
//
//                                Glide.with(this@HomeScreen)
////                                    .load(response.body()?.response!!.profile_pic)
//                                    .placeholder(R.drawable.logo)
//                                    .apply(RequestOptions().centerCrop())
//                                    .into(profilepic)


                                profile_name.text = "" + response.body()!!.response.full_name

//                            val editor = sharedPreferences.edit()
//                            editor.putString("selectcatid", response.body()!!.response.category_ids)
//                            editor.putString(
//                                "selectsubcatid",
//                                response.body()!!.response.subcategory_ids
//                            )
//                            editor.commit()

                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            } catch (e: TypeCastException) {
                                e.printStackTrace()
                            } catch (e: IllegalArgumentException) {
                                e.printStackTrace()
                            } catch (e: IllegalStateException) {
                                e.printStackTrace()
                            }

                        }


                    }

                    override fun onFailure(call: Call<LoginList_Response>, t: Throwable) {
                        Log.e(ContentValues.TAG, t.toString())

                    }

                })


            } else {

                Toast.makeText(
                    this,
                    "Please Check your internet",
                    Toast.LENGTH_LONG
                ).show()
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_home_screen)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun getCart() {

        if (NetWorkConection.isNEtworkConnected(this@HomeScreen)) {

                val apiServices = APIClient.client.create(Api::class.java)
                var call=  apiServices.getCartList(getString(R.string.api_key),customer_id = Utilities.customerid)

                Log.e("cat", " customerid " + Utilities.customerid)

                call?.enqueue(object : Callback<CartListResponse> {
                    @SuppressLint("WrongConstant")
                    override fun onResponse(call: Call<CartListResponse>, response: Response<CartListResponse>) {
                        Log.e(ContentValues.TAG, response.toString())

                        if (response.isSuccessful) {
                            try {


                                if (response.body()?.cartList?.size!!  > 0) {
                                    textCartItemCount?.visibility = View.VISIBLE
                                    bagred_image?.visibility = View.VISIBLE
                                    textCartItemCount?.text  = "" + response.body()?.cartList?.size!!
                                } else {
                                    textCartItemCount?.visibility = View.GONE
                                    bagred_image?.visibility = View.GONE
                                }

                                if (response.body()?.cartList?.size!! > 0) {

                                      } else {

                                    }






                            } catch (e: java.lang.NullPointerException) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call<CartListResponse>, t: Throwable) {
                        Log.e(ContentValues.TAG, t.toString())
                    }
                })
            } else {
            Toast.makeText(
                applicationContext,
                "Please Check your internet",
                Toast.LENGTH_LONG
            ).show()
        }


    }
    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {

        val concatenatedValues = listOf(p1?.orderId, p1?.paymentId, p1?.signature).joinToString(",")


            p1?.paymentId?.let { NotificationCenter.postNotification("1"+concatenatedValues) }



    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        Toast.makeText(this, "Payment Failed", Toast.LENGTH_LONG).show()
    }
    fun updateCartCount()
    {
        getCart()
    }
}