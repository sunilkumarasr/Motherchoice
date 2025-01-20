package com.royalit.motherchoice.ui.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.erepairs.app.api.Api
import com.google.gson.Gson
import com.royalit.motherchoice.HomeScreen
import com.royalit.motherchoice.R
import com.royalit.motherchoice.api.APIClient
import com.royalit.motherchoice.databinding.ProductdetailsScreenBinding
import com.royalit.motherchoice.models.AddtoCartResponse
import com.royalit.motherchoice.models.CartListResponse
import com.royalit.motherchoice.models.Category_subResponse
import com.royalit.motherchoice.models.Product_inDetailsListResponse
import com.royalit.motherchoice.models.Product_inDetailsResponse
import com.royalit.motherchoice.models.UpdateCartResponse
import com.royalit.motherchoice.roomdb.AppConstants
import com.royalit.motherchoice.roomdb.CartItems
import com.royalit.motherchoice.roomdb.CartViewModel
import com.royalit.motherchoice.roomdb.MotherChoiceDB
import com.royalit.motherchoice.utils.NetWorkConection
import com.royalit.motherchoice.utils.Utilities
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProductinDetails_Fragment : Fragment() {
    var productID: String? = null
    lateinit var cartQty: IntArray
     var detailsDataResponse: Product_inDetailsResponse?=null


    var bannersData: ArrayList<String> = ArrayList()
    var pic: String? = null
//    lateinit var cartqtty: String

    private var _binding: ProductdetailsScreenBinding? = null
    lateinit var root: View
    lateinit var motherChoiceDB: MotherChoiceDB
    var viewModel: CartViewModel? = null
    var cartItemsList: List<CartItems> = ArrayList()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    lateinit var sharedPreferences: SharedPreferences
    lateinit var category_id: String
    lateinit var customerid: String

    var productIDSB: StringBuilder? = null
    var quantitySB: StringBuilder? = null
    val imageList = ArrayList<SlideModel>() // Create image list
var cart_id=0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = ProductdetailsScreenBinding.inflate(inflater, container, false)
        root = binding.root
        sharedPreferences =
            requireContext().getSharedPreferences(
                "loginprefs",
                Context.MODE_PRIVATE
            )

        productID = sharedPreferences.getString("subcatid", "")!!
        customerid = sharedPreferences.getString("userid", "").toString()
        cartQty = intArrayOf(binding.cartQty.text.toString().toInt())

        Log.e("productID", productID!!)
        motherChoiceDB =
            Room.databaseBuilder(activity as Activity, MotherChoiceDB::class.java, "mother-choice")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries().build()
        motherChoiceDB = MotherChoiceDB.getInstance(activity as Activity)

        try {
            binding.cartIncBtn.setOnClickListener { v ->

                val carstQty = binding.cartQty.text.toString()
                if(detailsDataResponse==null)
                {
                    return@setOnClickListener
                }
                if (detailsDataResponse?.stock == carstQty) {

                    Toast.makeText(
                        activity,
                        "Stock Limit only " + detailsDataResponse?.stock,
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    if ((detailsDataResponse?.max_order_quantity!=null)&& (detailsDataResponse?.max_order_quantity!!.toInt()<=carstQty.toInt())){
                        Toast.makeText(context, "Can't add Max Quantity for this Product", Toast.LENGTH_LONG).show()

                        return@setOnClickListener
                    }
                    try {
                        cartQty[0]++
                        binding.cartQty.text = "" + cartQty[0]
                        val carstQty1 = binding.cartQty.text.toString()

                        if (carstQty1 == "1")
                            addToCart()
                        else
                            updateCart(carstQty1)
                    } catch (e: NumberFormatException) {
                        e.printStackTrace()
                    }

                }


            }

            binding.cartDecBtn.setOnClickListener { v ->
                if(detailsDataResponse==null)
                {
                    return@setOnClickListener
                }
                val carstQty = binding.cartQty.text.toString()
                if (detailsDataResponse?.stock == carstQty) {
                    return@setOnClickListener
                }
                if (cartQty[0] > 1) {
                    cartQty[0]--
                    binding.cartQty.text = "" + cartQty.get(0)
                    Log.e("item_cart_==>", "" + cartQty)
                    val carstQty1 = binding.cartQty.text.toString()
                    updateCart(carstQty1)

                }

            }
            /*val cartVM = CartViewModel(activity, false)
            cartVM.cartData()

            cartVM.getProductCartDetails(productID, activity)
            cartVM.productDetails.observe(
                requireActivity()
            ) { cartItems: List<CartItems> ->
                Log.e("data fggdgd==>", Gson().toJson(cartItems))

                if (cartItems.isNotEmpty()) {
                    val cartQty = intArrayOf(cartItems.get(0).getCartQty().toString().toInt())
                    Log.e("cartQty  fdfs", "" + cartItems[0].getCartQty())
                    cart_id=cartItems.get(0).cartID
                    if (binding.cartQty.text.toString().equals("0")) {
                        binding.addToBagBtn.text = "Add to bag"
                    } else {
                        binding.addToBagBtn.text = "Update"
                    }


                    binding.cartIncBtn.setOnClickListener { v ->

                        val carstQty = binding.cartQty.text.toString()
                        if (detailsDataResponse?.stock == carstQty) {

                            Toast.makeText(
                                activity,
                                "Stock Limit only " + detailsDataResponse?.stock,
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            try {
                                cartQty[0]++
                                binding.cartQty.text = "" + cartQty[0]
                            } catch (e: NumberFormatException) {
                                e.printStackTrace()
                            }

                        }


                    }

                    binding.cartDecBtn.setOnClickListener { v ->

                        if (cartQty[0] > 0) {
                            cartQty[0]--


                        }
                        binding.cartQty.text = "" + cartQty.get(0)
                        Log.e("item_cart_==>", "" + cartQty)

                    }

                } else {


                    val cartQty = intArrayOf(binding.cartQty.text.toString().toInt())
                    if (binding.cartQty.text.toString().equals("0")) {
                        binding.addToBagBtn.text = "Add to bag"
                    } else {
                        binding.addToBagBtn.text = "Update"
                    }

                    binding.cartIncBtn.setOnClickListener { v ->

                        val carstQty = binding.cartQty.text.toString()
                        if(detailsDataResponse==null)
                        {
                            return@setOnClickListener
                        }
                        if (detailsDataResponse?.stock == carstQty) {

                            Toast.makeText(
                                activity,
                                "Stock Limit only " + detailsDataResponse?.stock,
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            try {
                                cartQty[0]++
                                binding.cartQty.text = "" + cartQty[0]
                                val carstQty1 = binding.cartQty.text.toString()

                                if (carstQty1 == "1")
                                addToCart()
                                else
                                    updateCart(carstQty1)
                            } catch (e: NumberFormatException) {
                                e.printStackTrace()
                            }

                        }


                    }

                    binding.cartDecBtn.setOnClickListener { v ->
                        if(detailsDataResponse==null)
                        {
                            return@setOnClickListener
                        }
                        val carstQty = binding.cartQty.text.toString()
                        if (detailsDataResponse?.stock == carstQty) {
                            return@setOnClickListener
                        }
                        if (cartQty[0] > 1) {
                            cartQty[0]--
                            binding.cartQty.text = "" + cartQty.get(0)
                            Log.e("item_cart_==>", "" + cartQty)
                            val carstQty1 = binding.cartQty.text.toString()
                                updateCart(carstQty1)

                        }

                    }
                }


            }*/


            val ds = binding.cartQty.text.toString()



            binding.addToBagBtn.setOnClickListener { v ->
                val carstQty1 = binding.cartQty.text.toString()

                if (carstQty1 == "0") {


                    Toast.makeText(activity, "Select quantity..", Toast.LENGTH_LONG).show()
                } else {

                    addToBag(carstQty1)


                    // click.onAddToCartClicked(data, carstQty);
                }
            }
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }


        getCategories()
        checkItemAddedInCart()
        return root
    }
    private fun addToCart() {

        if (NetWorkConection.isNEtworkConnected(activity as Activity)) {
            val apiServices = APIClient.client.create(Api::class.java)
            var call:Call<AddtoCartResponse>?=null
            call = apiServices.addToCart(getString(R.string.api_key), customer_id =customerid, product_id = detailsDataResponse?.products_id.toString(), quantity = "1" )

            Log.e("cat", "Add to cart products_id" + detailsDataResponse?.products_id)
            Log.e("cat", "Add to cart customerid " + customerid)
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



                        } catch (e: java.lang.NullPointerException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<AddtoCartResponse>, t: Throwable) {
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
    private fun updateCart( cartQty:String?) {

        if (NetWorkConection.isNEtworkConnected(activity as Activity)) {
            val apiServices = APIClient.client.create(Api::class.java)
            var call=  apiServices.updateCart(getString(R.string.api_key),customer_id =customerid, quantity = cartQty!!, product_id = detailsDataResponse?.products_id.toString()!! )

            Log.e("cat", "quantity cartQty" + cartQty+" - "+detailsDataResponse?.products_id)

            call?.enqueue(object : Callback<UpdateCartResponse> {
                @SuppressLint("WrongConstant")
                override fun onResponse(call: Call<UpdateCartResponse>, response: Response<UpdateCartResponse>) {
                    Log.e(ContentValues.TAG, response.toString())
                    getCart()
                    if (response.isSuccessful) {

                        try {

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


    private fun checkItemAddedInCart() {
       /* val cartVM = CartViewModel(activity, false)
        cartVM.getProductCartDetails(productID, activity)
        cartVM.productDetails.observe(
            requireActivity()
        ) { cartItems: List<CartItems> ->
            Log.e("data==>", Gson().toJson(cartItems))
            if (cartItems.isNotEmpty()) {

                binding.cartQty.text = "" + cartItems[0].getCartQty()
                Log.e("fff", "" + cartItems[0].getCartQty())


            } else {
                binding.cartQty.text = "0"
            }
        }*/
    }

    /* */
    fun getData(
        id: String?,
        typeOfItem: Boolean
    ): ProductinDetails_Fragment? {
        val fragment: ProductinDetails_Fragment =
            ProductinDetails_Fragment()
        val bundle = Bundle()
        bundle.putString("single_product_data", id)
        fragment.arguments = bundle
        return fragment
    }


    private fun addToBag(quantity: String) {
        val items = CartItems(
            0,
            cart_id,
            Utilities.customerid,
            Utilities.getDeviceID(activity),
            AppConstants.PRODUCTS_CART,
            detailsDataResponse?.product_name,
            detailsDataResponse?.product_image,
            quantity,

            java.lang.String.valueOf(detailsDataResponse?.sales_price),
            java.lang.String.valueOf(detailsDataResponse?.offer_price),
            java.lang.String.valueOf(detailsDataResponse?.products_id),
            detailsDataResponse?.stock,
            detailsDataResponse?.max_order_quantity,
            detailsDataResponse?.category_2_price,
            detailsDataResponse?.product_name,
            detailsDataResponse?.product_title,
            detailsDataResponse?.quantity
        )
        val viewModel = CartViewModel(activity)
        viewModel.insert(items)
        Toast.makeText(context, "Item added to cart successfully", Toast.LENGTH_LONG).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding
    }


    private fun getCategories() {

        if (NetWorkConection.isNEtworkConnected(activity as Activity)) {

            //Set the Adapter to the RecyclerView//


            var apiServices = APIClient.client.create(Api::class.java)

            val call = productID?.let { apiServices.getindetails(getString(R.string.api_key), it) }



            binding.loadingPB.visibility = View.VISIBLE
            call?.enqueue(object : Callback<Product_inDetailsListResponse> {
                @SuppressLint("WrongConstant")
                override fun onResponse(
                    call: Call<Product_inDetailsListResponse>,
                    response: Response<Product_inDetailsListResponse>
                ) {

                    Log.e(ContentValues.TAG, response.body().toString())
                    binding.loadingPB.visibility = View.GONE

                    if (response.isSuccessful) {

                        try {
                            Log.e("djs", "" + response.body()?.response?.stock)


                            detailsDataResponse = response.body()!!.response

                            binding.priceTVID.text =
                                "\u20B9 " + detailsDataResponse?.sales_price
                            if(Utilities.customer_category==2)
                                binding.mrpTVID.text =
                                    "\u20B9 " + detailsDataResponse?.category_2_price
                            else
                            binding.mrpTVID.text =
                                "\u20B9 " + detailsDataResponse?.offer_price


                            binding.priceTVID.paintFlags =
                                binding.priceTVID.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                            if (detailsDataResponse?.stock?.toInt()==0) {
                                binding.outofstockBtn.visibility = View.VISIBLE
                                binding.addLayout.visibility = View.GONE
                            } else {
                                binding.outofstockBtn.visibility = View.GONE
                                binding.addLayout.visibility = View.VISIBLE
                            }
                            getCart()

//                            if (detailsDataResponse.offer_price != null && !detailsDataResponse.offer_price.equals(
//                                    ""
//                                )
//                            ) {
//                                binding.offTVID.text = "" + detailsDataResponse.offer_price + "%"
//                                binding.offTVID.visibility = View.VISIBLE
//                            } else {
//                                binding.offTVID.visibility = View.GONE
//                            }


                           /* Glide.with(binding.imageSlider.context)
                                .load(detailsDataResponse?.product_image)
                                .placeholder(R.drawable.logo)
                                .into(binding.imageSlider)*/

                            detailsDataResponse?.productImage?.forEach(
                                {
                                    Log.e("Profile Image","Profile Image ${it.fullPath}}")
                                    imageList.add(SlideModel(it.fullPath, ""))

                                }
                            )

                            if(imageList.isEmpty())
                                imageList.add(SlideModel(detailsDataResponse!!.product_image, ""))

                           // imageList.add(SlideModel(detailsDataResponse?.product_image, ""))

                            binding.imageSliderA.setImageList(imageList, scaleType = ScaleTypes.CENTER_INSIDE)


                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                binding.descTVID.text = Html.fromHtml(
                                    detailsDataResponse?.product_information,
                                    Html.FROM_HTML_MODE_COMPACT
                                )
                            } else {
                                binding.descTVID.text =
                                    Html.fromHtml(detailsDataResponse?.product_title)
                            }

                            //Set the Adapter to the RecyclerView//


                            binding.productNameTVID.text = detailsDataResponse?.product_name
                            binding.quantityTVID.text = detailsDataResponse?.quantity + " "


                        } catch (e: java.lang.NullPointerException) {
                            e.printStackTrace()
                        }

                    }


                }

                override fun onFailure(call: Call<Product_inDetailsListResponse>, t: Throwable) {
                    Log.e(ContentValues.TAG, t.toString())
                    binding.loadingPB.visibility = View.GONE

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

    private fun getCart() {

        if (NetWorkConection.isNEtworkConnected(activity as Activity)) {
            try{
                ( requireActivity() as HomeScreen).updateCartCount()
            }catch (e: java.lang.Exception)
            {

            }
            val apiServices = APIClient.client.create(Api::class.java)
            var call=  apiServices.getCartList(getString(R.string.api_key),customer_id =customerid)



            call?.enqueue(object : Callback<CartListResponse> {
                @SuppressLint("WrongConstant")
                override fun onResponse(call: Call<CartListResponse>, response: Response<CartListResponse>) {
                    Log.e(ContentValues.TAG, response.toString())


                    if (response.isSuccessful) {
                        try {

                            if (response.body()?.cartList?.size!! > 0) {
                                binding.cartQty.text = "0"

                                for (j in response.body()?.cartList!!.indices) {


                                    if (response.body()?.cartList!!.get(j).getProduct_id()
                                            .toInt() == (detailsDataResponse?.products_id)
                                    ) {
                                        binding.cartQty.text=response.body()?.cartList!!.get(j).cartQty
                                        cartQty[0]=response.body()?.cartList!!.get(j).cartQty.toInt()
                                    }
                                }

                                } else {
                                binding.cartQty.text = "0"
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
                activity as Activity,
                "Please Check your internet",
                Toast.LENGTH_LONG
            ).show()
        }


    }
}

