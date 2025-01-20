package com.royalit.motherchoice.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.room.Room
import com.erepairs.app.adapter.Cart_Adapter
import com.erepairs.app.api.Api
import com.erepairs.app.models.AppMaintananceResponseModel
import com.razorpay.Checkout
import com.royalit.motherchoice.AppMaintananceActivity
import com.royalit.motherchoice.First_Screen
import com.royalit.motherchoice.HomeScreen
import com.royalit.motherchoice.NotificationCenter
import com.royalit.motherchoice.Observer
import com.royalit.motherchoice.R
import com.royalit.motherchoice.api.APIClient
import com.royalit.motherchoice.databinding.CartScreenBinding
import com.royalit.motherchoice.models.AddtoCartResponse
import com.royalit.motherchoice.models.CartListResponse
import com.royalit.motherchoice.models.DeleteCartResponse
import com.royalit.motherchoice.models.LoginList_Response
import com.royalit.motherchoice.models.Placeorder_ListResponse
import com.royalit.motherchoice.models.Product_Response
import com.royalit.motherchoice.models.UpdateCartResponse
import com.royalit.motherchoice.roomdb.AppConstants
import com.royalit.motherchoice.roomdb.CartItems
import com.royalit.motherchoice.roomdb.CartViewModel
import com.royalit.motherchoice.roomdb.MotherChoiceDB
import com.royalit.motherchoice.utils.NetWorkConection
import com.royalit.motherchoice.utils.Utilities
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception


class Cart_Fragment : Fragment(), Cart_Adapter.ProductItemClick, Cart_Adapter.CartItemQuantityChangeListener,
    Observer {
    lateinit var usersubcategory_id_: String

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: CartScreenBinding? = null
    lateinit var cart_adapter: Cart_Adapter
    var cartData: List<CartItems> = ArrayList<CartItems>()
    lateinit var motherChoiceDB: MotherChoiceDB
    var viewModel: CartViewModel? = null
    var cartItemsList: List<CartItems> = ArrayList()
    lateinit var root: View
    lateinit var cartItemListener:Cart_Adapter.CartItemQuantityChangeListener

    lateinit var productIDStr: String
    lateinit var productQtyStr: String
    lateinit var address: String
    lateinit var username: String
    var TotalPrice: Double? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var sharedPreferences: SharedPreferences
    lateinit var category_id: String
    lateinit var customerid: String
    var productIDSB: StringBuilder? = null
    var quantitySB: StringBuilder? = null
    val cartVM=null
    var adapter:Cart_Adapter?=null
    private var payment_id: String = ""
    private var order_id: String = ""
    private var signature_id: String = ""
    lateinit var productItemClick:Cart_Adapter.ProductItemClick
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        cartItemListener=this
        homeViewModel =
            ViewModelProvider(
                this,
                ViewModelProvider.NewInstanceFactory()
            ).get(HomeViewModel::class.java)

        _binding = CartScreenBinding.inflate(inflater, container, false)
        root = binding.root
        sharedPreferences =
            requireContext().getSharedPreferences(
                "loginprefs",
                Context.MODE_PRIVATE
            )

        Checkout.preload(requireContext())
        productItemClick=this
        category_id = sharedPreferences.getString("categoryid", "")!!
        customerid = sharedPreferences.getString("userid", "").toString()
        address = sharedPreferences.getString("address", "").toString()
        username = sharedPreferences.getString("username", "").toString()

        Log.e("cust", customerid)
        Log.e("address", address)
        motherChoiceDB =
            Room.databaseBuilder(activity as Activity, MotherChoiceDB::class.java, "mother-choice")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries().build()
//        motherChoiceDB = MotherChoiceDB.getInstance(activity as Activity)


        viewModel = CartViewModel(activity as Activity)
        viewModel!!.cartData()

         adapter = Cart_Adapter(
            activity as Activity,
            cartItemsList as ArrayList<CartItems>, cartData, this@Cart_Fragment,cartItemListener
        )
        binding.cartRCID.adapter = adapter
        /*viewModel!!.getCartItems.observe(requireActivity(), {
                cartItems -> cartData = cartItems
            productIDStr=""
            productQtyStr=""
            cartData.forEach({
                if(productIDStr.isEmpty())
                    productIDStr=it.product_id.toString()
                    else
                productIDStr=productIDStr+"##"+it.product_id

                if(productQtyStr.isEmpty())
                    productQtyStr=it.cartQty.toString()
                else
                productQtyStr=productQtyStr+"##"+it.cartQty
            })
        })*/

        binding.addressTVID.text = "" + address
        binding.userNameTVID.text = "" + username

        getCart()




        binding.btnPlaceOrder.setOnClickListener {
            if (binding.rbOnline.isChecked){
//                rzp_live_mSjl2kggxi77aG
            }
            else if (binding.rbCashOnDelivery.isChecked)
            {
                productIDStr=""
                productQtyStr=""
                cartItemsList.forEach({
                    if(productIDStr.isEmpty())
                        productIDStr=it.products_id.toString()
                    else
                        productIDStr=productIDStr+"##"+it.products_id

                    if(productQtyStr.isEmpty())
                        productQtyStr=it.cartQty.toString()
                    else
                        productQtyStr=productQtyStr+"##"+it.cartQty
                })
                postOrder()
            }
            else{
                Toast.makeText(activity,"Please Select Payment Method",Toast.LENGTH_LONG).show()
            }


        }




        try {


            viewModel!!.getCartItems.observe(requireActivity()) { cartItems ->
                cartItemsList = cartItems
                if (cartItems.size > 0) {
                    binding.itemCountTVID.text = "" + cartItems.size.toString() + " Items"
                    getTotalPrice(cartItemsList)
                    binding.mainRLID.visibility = View.VISIBLE
                    binding.noDataTVID.visibility = View.GONE

                    /*try {

                        val adapter = Cart_Adapter(
                            activity as Activity,
                            cartItems as ArrayList<CartItems>, cartData, this,this
                        )
                        binding.cartRCID.adapter = adapter

                    } catch (e: java.lang.NullPointerException) {
                        e.printStackTrace()
                    }*/
                } else {
                    binding.mainRLID.visibility = View.GONE
                    binding.noDataTVID.visibility = View.VISIBLE
                }
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
        return root
    }

    override fun onQuantityChanged(cartItem: CartItems, newQuantity: Int) {
        // Handle the quantity change here
        val index = cartItemsList.indexOfFirst { it.product_id == cartItem.product_id }
        if (index != -1) {
            cartItemsList[index].setCartQty(newQuantity.toString())
            getTotalPrice(cartItemsList) // Recalculate and update the total price
        }
    }

    override fun onDeleteCartItem(cartItem: CartItems) {

        if (NetWorkConection.isNEtworkConnected(activity as Activity)) {
            val apiServices = APIClient.client.create(Api::class.java)
            var call=  apiServices.removeFromCart(getString(R.string.api_key),customer_id =customerid, product_id = cartItem.product_id,cart_id = cartItem?.cartID.toString()!! )

            Log.e("cat", "" + category_id)
            Log.e("customer_id", "Cart Delete customer_id" + customerid)
            Log.e("product_id", "Cart Delete product_id" + cartItem.product_id)
            Log.e("product_id", "Cart Delete cart_id" + cartItem?.cartID)


            call?.enqueue(object : Callback<DeleteCartResponse> {
                @SuppressLint("WrongConstant")
                override fun onResponse(call: Call<DeleteCartResponse>, response: Response<DeleteCartResponse>) {
                    Log.e(ContentValues.TAG, response.toString())


                    getCart()
                    if (response.isSuccessful) {
                        try {




                        } catch (e: java.lang.NullPointerException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<DeleteCartResponse>, t: Throwable) {
                    Log.e(ContentValues.TAG, t.toString())
getCart()
                }
            })
        } else {
            Toast.makeText(
                activity as Activity,
                "Please Check your internet",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getTotalPrice(cartItemsList: List<CartItems>) {
        try {
             TotalPrice = 0.0
            for (i in cartItemsList.indices) {
                try {
                    if (Utilities.customer_category == 2)
                        TotalPrice = TotalPrice!! + cartItemsList[i].category_2_price.toDouble() * cartItemsList[i].getCartQty()
                            .toDouble()
                    else
                        TotalPrice = TotalPrice!! + cartItemsList[i].getOffer_price()
                            .toDouble() * cartItemsList[i].getCartQty().toDouble()
                }catch (e:Exception)
                {

                }
            }
            binding.totalPriceTVID.text = "\u20b9 $TotalPrice"
        } catch (e: NumberFormatException) {
            e.printStackTrace()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }


        override fun onDestroyView() {
            super.onDestroyView()
            _binding
        }




        override fun onProductItemClick(itemsData: CartItems?) {

            ProductinDetails_Fragment().getData(itemsData?.product_id.toString(), true)!!

            val navController =
                Navigation.findNavController(
                    context as Activity,
                    R.id.nav_host_fragment_content_home_screen
                )
            navController.navigate(R.id.nav_product_details)

            val editor = sharedPreferences.edit()
            editor.putString("subcatid", itemsData?.product_id)
            editor.clear()

        }
    private fun addToCart(itemsData: CartItems?, cartQty:String?) {

        if (NetWorkConection.isNEtworkConnected(activity as Activity)) {
            val apiServices = APIClient.client.create(Api::class.java)
            var call:Call<AddtoCartResponse>?=null
            call = apiServices.addToCart(getString(R.string.api_key), customer_id = Utilities.customerid, product_id = itemsData?.products_id.toString(), quantity = "1" )

            Log.e("cat", "Add to cart products_id" + itemsData?.products_id)
            Log.e("cat", "Add to cart customerid " + Utilities.customerid)

            call.enqueue(object : Callback<AddtoCartResponse> {
                @SuppressLint("WrongConstant")
                override fun onResponse(call: Call<AddtoCartResponse>, response: Response<AddtoCartResponse>) {
                    Log.e(ContentValues.TAG, response.toString())

                    var cart_id=0
                    response.body()?.cartList?.cart_id?.toInt()

                    if (response.isSuccessful) {
                        try {
                            if(response.body()?.cartList?.cart_id!=null)
                                cart_id=    response.body()?.cartList?.cart_id!!.toInt()
                            getCart()
                            /* val items = CartItems(
                                 0,
                                 cart_id,
                                 Utilities.customerid,
                                 Utilities.getDeviceID(activity),
                                 AppConstants.PRODUCTS_CART,
                                 itemsData!!.product_name,
                                 itemsData.product_image,
                                 cartQty,
                                 java.lang.String.valueOf(itemsData.sales_price),
                                 java.lang.String.valueOf(itemsData.offer_price),
                                 java.lang.String.valueOf(itemsData.products_id),
                                 itemsData.stock,
                                 itemsData.max_order_quantity,
                                 itemsData.category_2_price,
                                 itemsData.product_name,
                                 itemsData.product_title,
                                 itemsData.quantity
                             )
                             Log.e("stock", "" + itemsData.stock)
                             val viewModel = CartViewModel(activity)
                             viewModel.deleteSingle(items)
                             viewModel.insert(items)*/


                        } catch (e: java.lang.NullPointerException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<AddtoCartResponse>, t: Throwable) {
                    Log.e(ContentValues.TAG, t.toString())
                    getCart()
                }
            })
        } else {
            Toast.makeText(
                activity as Activity,
                "Please Check your internet",
                Toast.LENGTH_LONG
            ).show()
        }


    }
    private fun updateCart(itemsData: CartItems?, cartQty:String?) {

        if (NetWorkConection.isNEtworkConnected(activity as Activity)) {
            val apiServices = APIClient.client.create(Api::class.java)
            var call=  apiServices.updateCart(getString(R.string.api_key),customer_id = Utilities.customerid, quantity = cartQty!!, product_id = itemsData?.products_id.toString()!! )

            Log.e("cat", "quantity cartQty" + cartQty+" - "+itemsData?.products_id)

            call?.enqueue(object : Callback<UpdateCartResponse> {
                @SuppressLint("WrongConstant")
                override fun onResponse(call: Call<UpdateCartResponse>, response: Response<UpdateCartResponse>) {
                    Log.e(ContentValues.TAG, response.toString())
                    getCart()
                    if (response.isSuccessful) {

                        try {
                            /*  val items = CartItems(
                                  0,
                                  0,
                                  Utilities.customerid,
                                  Utilities.getDeviceID(activity),
                                  AppConstants.PRODUCTS_CART,
                                  itemsData!!.product_name,
                                  itemsData.product_image,
                                  cartQty,
                                  java.lang.String.valueOf(itemsData.sales_price),
                                  java.lang.String.valueOf(itemsData.offer_price),
                                  java.lang.String.valueOf(itemsData.products_id),
                                  itemsData.stock,
                                  itemsData.max_order_quantity,
                                  itemsData.category_2_price,
                                  itemsData.product_name,
                                  itemsData.product_title,
                                  itemsData.quantity
                              )
                              Log.e("stock", "" + itemsData.stock)
                              val viewModel = CartViewModel(activity)
                              viewModel.insert(items)
  */

                        } catch (e: java.lang.NullPointerException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<UpdateCartResponse>, t: Throwable) {
                    Log.e(ContentValues.TAG, t.toString())
                }
            })
        } else {
            getCart()
            Toast.makeText(
                activity as Activity,
                "Please Check your internet",
                Toast.LENGTH_LONG
            ).show()
        }


    }
        override fun onAddToCartClicked(itemsData: CartItems?, cartQty: String?,isAdd:Int) {
            if(isAdd==0) {
                addToCart(itemsData, cartQty)
            }
            else
                updateCart(itemsData, cartQty)
            /*val items = CartItems(
                0,
                itemsData?.cartID!!,
                customerid,
                Utilities.getDeviceID(activity),
                AppConstants.PRODUCTS_CART,
                itemsData!!.itemName,
                itemsData.itemImage,
                cartQty,
                java.lang.String.valueOf(itemsData.sales_price),
                java.lang.String.valueOf(itemsData.offer_price),
                java.lang.String.valueOf(itemsData.product_id),
                itemsData.product_id.toString(), itemsData.stock,
                itemsData.max_order_quantity,
                itemsData.category_2_price,
                itemsData.product_name,
                itemsData.product_title
            )
            val viewModel = CartViewModel(activity)
            viewModel.insert(items)
            Toast.makeText(activity, "Item added to cart successfully", Toast.LENGTH_LONG).show()*/
        }

        private fun postOrder() {

            try {


                if (NetWorkConection.isNEtworkConnected(context as Activity)) {

                    //Set the Adapter to the RecyclerView//

                    val addnotes = binding.cartnotes.text.toString()

                    var apiServices = APIClient.client.create(Api::class.java)


                    val call =
                        apiServices.getplaceordersave(
                            getString(R.string.api_key),
                            customerid, productIDStr, productQtyStr, addnotes
                        )
                    binding.progressbar.visibility = View.VISIBLE

                    call.enqueue(object : Callback<Placeorder_ListResponse> {
                        override fun onResponse(
                            call: Call<Placeorder_ListResponse>,
                            response: Response<Placeorder_ListResponse>
                        ) {

                            binding.progressbar.visibility = View.GONE

                            try {
                                if (response.isSuccessful) {

                                    if(response.body()?.customer_status!="active")
                                    {
                                        Toast.makeText(requireActivity(),"User is Inactive, Order not placed",Toast.LENGTH_LONG).show()
                                        return
                                    }
                                    //Set the Adapter to the RecyclerView//

                                    val navController =
                                        Navigation.findNavController(
                                            context as Activity,
                                            R.id.nav_host_fragment_content_home_screen
                                        )
                                    navController.navigate(R.id.navigation_sucess)
                                    val viewModel = CartViewModel(activity as Activity)
                                    viewModel.delete(cartItemsList[0], true, true)

                                    val prefs = sharedPreferences.edit()
                                    prefs.putString(
                                        "Orderid",
                                        response.body()?.response?.order_id.toString()
                                    )
                                    prefs.putString(
                                        "orderamount",
                                        response.body()?.response?.amount.toString()
                                    )
                                    prefs.commit()

                                    Toast.makeText(
                                        context,
                                        "Order Placed Sucessfully",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {


                                }
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            } catch (e: TypeCastException) {
                                e.printStackTrace()
                            }


                        }


                        override fun onFailure(call: Call<Placeorder_ListResponse>, t: Throwable) {
                            Log.e(ContentValues.TAG, t.toString())
                            Toast.makeText(
                                context,
                                "Something went wrong",
                                Toast.LENGTH_LONG
                            ).show()

                            binding.progressbar.visibility = View.GONE

                        }
                    })


                } else {
                    Toast.makeText(context, "Please Check your internet", Toast.LENGTH_LONG).show()

                }
            } catch (e: UninitializedPropertyAccessException) {
                e.printStackTrace()
            }
        }
    private fun getCart() {

        if (NetWorkConection.isNEtworkConnected(activity as Activity)) {
           try{
               ( requireActivity() as HomeScreen ).updateCartCount()
           }catch (e:Exception )
           {

           }
            val apiServices = APIClient.client.create(Api::class.java)
            var call=  apiServices.getCartList(getString(R.string.api_key),customer_id =customerid)

            Log.e("cat", " customerid " + customerid)
           // binding.subproductprogress.visibility = View.VISIBLE

            call?.enqueue(object : Callback<CartListResponse> {
                @SuppressLint("WrongConstant")
                override fun onResponse(call: Call<CartListResponse>, response: Response<CartListResponse>) {
                    Log.e(ContentValues.TAG, response.toString())
                   // binding.subproductprogress.visibility = View.GONE

                    if (response.isSuccessful) {
                        try {


                             val editor = sharedPreferences.edit()

                            editor.putInt(
                                "customer_category",
                                response.body()!!.customer_category!!
                            )
                            editor.commit()
                            cartItemsList= response.body()?.cartList!!

                            if (cartItemsList.size > 0) {
                                binding.itemCountTVID.text = "" + cartItemsList.size.toString() + " Items"
                                getTotalPrice(cartItemsList)
                                binding.mainRLID.visibility = View.VISIBLE
                                binding.noDataTVID.visibility = View.GONE

                                try {

                                    val adapter = Cart_Adapter(
                                        activity as Activity,
                                        cartItemsList as ArrayList<CartItems>, cartData, productItemClick,cartItemListener
                                    )
                                    binding.cartRCID.adapter = adapter
                                    productIDStr=""
                                    productQtyStr=""
                                    cartItemsList.forEach({
                                        if(productIDStr.isEmpty())
                                            productIDStr=it.cartID.toString()
                                        else
                                            productIDStr=productIDStr+","+it.cartID

                                        if(productQtyStr.isEmpty())
                                            productQtyStr=it.cartQty.toString()
                                        else
                                            productQtyStr=productQtyStr+","+it.cartQty
                                    })
                                } catch (e: java.lang.NullPointerException) {
                                    e.printStackTrace()
                                }
                            } else {
                                binding.mainRLID.visibility = View.GONE
                                binding.noDataTVID.visibility = View.VISIBLE
                            }

                        } catch (e: java.lang.NullPointerException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<CartListResponse>, t: Throwable) {
                    Log.e(ContentValues.TAG, t.toString())
                   // binding.subproductprogress.visibility = View.GONE
                }
            })
        } else {
            Toast.makeText(
                activity as Activity,
                "Please Check your internet",
                Toast.LENGTH_LONG
            ).show()
        }


    }

    private fun crateOrderId() {

        if (NetWorkConection.isNEtworkConnected(activity as Activity)) {

            val apiServices = APIClient.client.create(Api::class.java)
            var call=  apiServices.createOrder(TotalPrice.toString())


            call?.enqueue(object : Callback<LoginList_Response> {
                @SuppressLint("WrongConstant")
                override fun onResponse(call: Call<LoginList_Response>, response: Response<LoginList_Response>) {
                    Log.e(ContentValues.TAG, response.toString())

                    if (response.isSuccessful) {
                        try {
                            val order_id= response.body()?.order_id
                            order_id?.let { startPayment(it) }
                        } catch (e: java.lang.NullPointerException) {
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
                activity as Activity,
                "Please Check your internet",
                Toast.LENGTH_LONG
            ).show()
        }


    }
    fun razorpayCallback() {

        if (NetWorkConection.isNEtworkConnected(activity as Activity)) {

            val apiServices = APIClient.client.create(Api::class.java)
            var call=  apiServices.update_payment_id(order_id,payment_id)


            call?.enqueue(object : Callback<LoginList_Response> {
                @SuppressLint("WrongConstant")
                override fun onResponse(call: Call<LoginList_Response>, response: Response<LoginList_Response>) {
                    Log.e(ContentValues.TAG, response.toString())

                    if (response.isSuccessful) {
                        try {
                            val success = response.body()?.success
                            if (success == true){
                                postOrder()
                            }
                            else{
                                Toast.makeText(activity,"Something went wrong",Toast.LENGTH_LONG).show()
                            }
                        } catch (e: java.lang.NullPointerException) {
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
                activity as Activity,
                "Please Check your internet",
                Toast.LENGTH_LONG
            ).show()
        }

    }
    private fun startPayment(orderId:String) {
        val activity: Activity = activity as Activity
        val co = Checkout()
        co.setKeyID("rzp_live_mSjl2kggxi77aG")
//        co.setKeyID("rzp_test_asvFtOXTSdhbXC")


        try {
            val options = JSONObject()

            options.put("name", "Mother Choice")
            options.put("description", "")
            //You can omit the image option to fetch the image from dashboard
            options.put("image", R.drawable.logo)
            options.put("theme.color", R.color.red);
            options.put("order_id", orderId)

            options.put("currency", "INR");
            val payment: String = TotalPrice.toString()
            var total = payment.toDouble()
            total = total * 100
            options.put("amount", total)

            val preFill = JSONObject()
            preFill.put("email", "")
            preFill.put("contact", "")

            options.put("prefill", preFill)

            co.open(activity, options)

        }catch (e: Exception){
            Toast.makeText(activity, "Error in payment: " + e.message, Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
    override fun onUpdate(message: String) {
        if (message.isNotEmpty() && message.first() == '1') {

            val concatenatedValues = message.substring(1)
            val separatedValues = concatenatedValues.split(",")
            order_id = separatedValues[0]
            payment_id = separatedValues[1]
            signature_id = separatedValues[2]
            razorpayCallback()
            NotificationCenter.clearData()
        }
    }


    }
