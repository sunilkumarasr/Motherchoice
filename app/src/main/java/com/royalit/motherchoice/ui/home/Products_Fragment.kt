package com.royalit.motherchoice.ui.home


import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.erepairs.app.api.Api
import com.erepairs.app.models.Search_Response
import com.royalit.motherchoice.HomeScreen
import com.royalit.motherchoice.R
import com.royalit.motherchoice.adapter.Search_Adapter
import com.royalit.motherchoice.adapter.SubProductList_Adapter
import com.royalit.motherchoice.api.APIClient
import com.royalit.motherchoice.databinding.ProductsScreenBinding
import com.royalit.motherchoice.models.AddtoCartResponse
import com.royalit.motherchoice.models.CartListResponse
import com.royalit.motherchoice.models.Category_subListResponse
import com.royalit.motherchoice.models.Category_subResponse
import com.royalit.motherchoice.models.Search_ListResponse
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


class Products_Fragment : Fragment(), SubProductList_Adapter.ProductItemClick {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: ProductsScreenBinding? = null
    lateinit var subproduct_adapter: SubProductList_Adapter
    var cartData: List<CartItems> = ArrayList<CartItems>()
    lateinit var motherChoiceDB: MotherChoiceDB
    lateinit var root: View
    private var isSortAscending = true
    private var selectedserviceslist: List<Category_subResponse> = ArrayList()
    private var lastVisibleItemPosition: Int = 0

    private val binding get() = _binding!!
    lateinit var sharedPreferences: SharedPreferences
    lateinit var category_id: String
    lateinit var value: String
    lateinit var customerid: String
    private var query = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(
                this,
                ViewModelProvider.NewInstanceFactory()
            ).get(HomeViewModel::class.java)

        _binding = ProductsScreenBinding.inflate(inflater, container, false)
        root = binding.root
        sharedPreferences =
            requireContext().getSharedPreferences(
                "loginprefs",
                Context.MODE_PRIVATE
            )
        customerid = sharedPreferences.getString("userid", "").toString()


        val edittextSearch = binding.edittextSearch


        binding.edittextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not used
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not used
                val query = s.toString()
                if (query.length >=2) {


                    try {
                        //val d = query.toInt()
                        val selectedserviceslistSearch= arrayListOf<Category_subResponse>()
                        selectedserviceslistSearch.addAll(selectedserviceslist)
                        // selectedserviceslistSearch.filterTo()
                        val filteredProducts=selectedserviceslistSearch.filter {
                            Log.d("Price compare","Price compare ${it.offer_price.toInt()} ${query.toInt()} : ${it.offer_price.toInt()==query.toInt()}")
                            it.product_name.toLowerCase().startsWith(query.toLowerCase())
                        }
                        subproduct_adapter = SubProductList_Adapter(
                            activity as Activity,
                            filteredProducts as ArrayList<Category_subResponse>,
                            cartData as ArrayList<CartItems>,
                            this@Products_Fragment
                        )

                        binding.subproductlist.adapter = subproduct_adapter
                        subproduct_adapter.notifyDataSetChanged()
                    }
                    catch (e: Exception) {

                        val selectedserviceslistSearch= arrayListOf<Category_subResponse>()
                        selectedserviceslistSearch.addAll(selectedserviceslist)
                        val filteredProducts=selectedserviceslistSearch.filter {
                            it.product_name.toLowerCase().contains(query.toLowerCase())
                        }
                        subproduct_adapter = SubProductList_Adapter(
                            activity as Activity,
                            filteredProducts as ArrayList<Category_subResponse>,
                            cartData as ArrayList<CartItems>,
                            this@Products_Fragment
                        )

                        binding.subproductlist.adapter = subproduct_adapter
                        subproduct_adapter.notifyDataSetChanged()

                    }
                    // Fetch products based on the offer price
                   // fetchProductsByOfferPrice(query)

                } else {
                    Log.d("selectedserviceslist ","selectedserviceslist ${selectedserviceslist.size}")
                    subproduct_adapter = SubProductList_Adapter(
                        activity as Activity,
                        selectedserviceslist as ArrayList<Category_subResponse>,
                        cartData as ArrayList<CartItems>,
                        this@Products_Fragment
                    )

                    binding.subproductlist.adapter = subproduct_adapter
                    subproduct_adapter.notifyDataSetChanged()
                }


            }

            override fun afterTextChanged(s: Editable?) {

        }
    })



        binding.filterBtn.setOnClickListener { view ->
            val popupMenu = PopupMenu(requireContext(), view)
            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

            // Set a click listener for the items in the pop-up menu
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.sort_low_to_high -> {
                        // Sort products in ascending order (Low to High)
                        isSortAscending = true
                        sortProductsByOfferPrice()
                        return@setOnMenuItemClickListener true
                    }
                    R.id.sort_high_to_low -> {
                        // Sort products in ascending order (Low to High)
                        isSortAscending = false
                        sortProductsByOfferPrice()
                        return@setOnMenuItemClickListener true
                    }

                    else -> false
                }
            }

            // Show the pop-up menu
            popupMenu.show()
        }

        category_id = sharedPreferences.getString("categoryid", "")!!
        motherChoiceDB =
            Room.databaseBuilder(activity as Activity, MotherChoiceDB::class.java, "mother-choice")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries().build()
//        motherChoiceDB = MotherChoiceDB.getInstance(activity as Activity)

        //val cartVM = CartViewModel(activity, false)
       // cartVM.cartData()
        /*cartVM.getCartItems.observe(requireActivity()) {
            cartItems -> cartData = cartItems

            if (cartItems.size > 0) {
                binding.cartcount.visibility = View.VISIBLE
                binding.cvOneLogin.visibility = View.VISIBLE
                binding.carttext.visibility = View.VISIBLE
                binding.rlOneLogin.visibility = View.VISIBLE
                binding.cartcount.text = "" + cartItems.size
            } else {
                binding.cartcount.visibility = View.GONE
                binding.cvOneLogin.visibility = View.GONE
                binding.carttext.visibility = View.GONE
                binding.rlOneLogin.visibility = View.GONE
                binding.cartcount.text = ""

            }

            Log.d("Added Cart Item","Added Cart Item "+cartData.size)
            subproduct_adapter =
                SubProductList_Adapter(
                    requireActivity() as Activity,
                    selectedserviceslist as ArrayList<Category_subResponse>,
                    cartData as ArrayList<CartItems>, this@Products_Fragment
                )
            //subproduct_adapter.resetCartData(cartDatas = cartData as ArrayList<CartItems>)
        }*/
       // cartVM.cartData()
       // val viewModel = CartViewModel(activity as Activity)
       /* viewModel.cartCount()
        viewModel.cartCount.observe(requireActivity()) { cartItems ->
            if (cartItems.size > 0) {
                binding.cartcount.visibility = View.VISIBLE
                binding.cvOneLogin.visibility = View.VISIBLE
                binding.carttext.visibility = View.VISIBLE
                binding.rlOneLogin.visibility = View.VISIBLE
                binding.cartcount.text = "" + cartItems.size
            } else {
                binding.cartcount.visibility = View.GONE
                binding.cvOneLogin.visibility = View.GONE
                binding.carttext.visibility = View.GONE
                binding.rlOneLogin.visibility = View.GONE
            }
        }*/

        val recyclerView = binding.subproductlist
        val layoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = layoutManager
        subproduct_adapter =
            SubProductList_Adapter(activity as Activity, ArrayList(), cartData as ArrayList<CartItems>, this)
        recyclerView.adapter = subproduct_adapter
        /*recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val totalItemCount = layoutManager.itemCount
                val visibleItemCount = layoutManager.childCount
                val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                lastVisibleItemPosition = firstVisibleItem + visibleItemCount - 1

                // Load more items if the user is close to the end of the list
                if (lastVisibleItemPosition >= totalItemCount - 5) {
                    // Load more items here if needed
                    // You can implement lazy loading logic here
                }
            }
        })*/
        getProducts()

        return root
    }
    private fun searchApi(search: String) {

        try {


            if (NetWorkConection.isNEtworkConnected(context as Activity)) {

                //Set the Adapter to the RecyclerView//
                var apiServices = APIClient.client.create(Api::class.java)

                val call =
                    apiServices.getsearch(
                        getString(R.string.api_key),
                        search
                    )


                call.enqueue(object : Callback<Search_ListResponse> {
                    override fun onResponse(
                        call: Call<Search_ListResponse>,
                        response: Response<Search_ListResponse>
                    ) {

                        Log.e(ContentValues.TAG, response.toString())


                        try {
                            if (response.isSuccessful) {

                                //Set the Adapter to the RecyclerView//

                                val selectedserviceslist =
                                    response.body()?.response!!

                                subproduct_adapter = SubProductList_Adapter(
                                    activity as Activity,
                                    response.body()?.response!! as ArrayList<Category_subResponse>,
                                    cartData as ArrayList<CartItems>,
                                    this@Products_Fragment
                                )

                                binding.subproductlist.adapter = subproduct_adapter
                                subproduct_adapter.notifyDataSetChanged()


                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: TypeCastException) {
                            e.printStackTrace()
                        }


                    }


                    override fun onFailure(call: Call<Search_ListResponse>, t: Throwable) {
                        Log.e(ContentValues.TAG, t.toString())
                        Toast.makeText(
                            context,
                            "Something went wrong",
                            Toast.LENGTH_LONG
                        ).show()


                    }
                })


            } else {
                Toast.makeText(context, "Please Check your internet", Toast.LENGTH_LONG).show()

            }
        } catch (e: UninitializedPropertyAccessException) {
            e.printStackTrace()
        }
    }
    private fun fetchProductsByOfferPrice(query: String) {
        if (NetWorkConection.isNEtworkConnected(activity as Activity)) {
            val apiServices = APIClient.client.create(Api::class.java)
            val call = apiServices.getsubcategoruesList(getString(R.string.api_key), category_id)

            Log.e("cat", "" + category_id)
            binding.subproductprogress.visibility = View.VISIBLE

            call.enqueue(object : Callback<Category_subListResponse> {
                @SuppressLint("WrongConstant")
                override fun onResponse(call: Call<Category_subListResponse>, response: Response<Category_subListResponse>) {
                    Log.e(ContentValues.TAG, response.toString())
                    binding.subproductprogress.visibility = View.GONE

                    if (response.isSuccessful) {
                        try {
                            response.body()?.toString()?.let { Log.d("API Response", it) }
                             selectedserviceslist = response.body()?.response!!
                            val filteredProducts = filterAndSortProducts(selectedserviceslist, query)
                           // val filteredAndSortedProducts = filterAndSortProducts(selectedserviceslist, query)
                            val sortedList = selectedserviceslist.sortedBy { it.offer_price.toDouble() }
                            subproduct_adapter = SubProductList_Adapter(
                                activity as Activity,
                                filteredProducts as ArrayList<Category_subResponse>,
                                cartData as ArrayList<CartItems>,
                                this@Products_Fragment
                            )

                            binding.subproductlist.adapter = subproduct_adapter
                            subproduct_adapter.notifyDataSetChanged()

                            val layoutManager = LinearLayoutManager(activity as Activity, RecyclerView.VERTICAL, false)
                            binding.subproductlist.layoutManager = layoutManager
                            binding.subproductlist.setItemViewCacheSize(sortedList.size)

                        } catch (e: java.lang.NullPointerException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<Category_subListResponse>, t: Throwable) {
                    Log.e(ContentValues.TAG, t.toString())
                    binding.subproductprogress.visibility = View.GONE
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
    private fun addToCart(itemsData: Category_subResponse?,cartQty:String?) {

        if (NetWorkConection.isNEtworkConnected(activity as Activity)) {
            val apiServices = APIClient.client.create(Api::class.java)
            var call:Call<AddtoCartResponse>?=null
                call = apiServices.addToCart(getString(R.string.api_key), customer_id =customerid, product_id = itemsData?.products_id.toString(), quantity = "1" )

            Log.e("cat", "Add to cart products_id" + itemsData?.products_id)
            Log.e("cat", "Add to cart customerid " + customerid)
            binding.subproductprogress.visibility = View.VISIBLE

            call.enqueue(object : Callback<AddtoCartResponse> {
                @SuppressLint("WrongConstant")
                override fun onResponse(call: Call<AddtoCartResponse>, response: Response<AddtoCartResponse>) {
                    Log.e(ContentValues.TAG, response.toString())
                    binding.subproductprogress.visibility = View.GONE

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
                    binding.subproductprogress.visibility = View.GONE
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
    private fun updateCart(itemsData: Category_subResponse?,cartQty:String?) {

        if (NetWorkConection.isNEtworkConnected(activity as Activity)) {
            val apiServices = APIClient.client.create(Api::class.java)
            var call=  apiServices.updateCart(getString(R.string.api_key),customer_id =customerid, quantity = cartQty!!, product_id = itemsData?.products_id.toString()!! )

            Log.e("cat", "quantity cartQty" + cartQty+" - "+itemsData?.products_id)
            binding.subproductprogress.visibility = View.VISIBLE

            call?.enqueue(object : Callback<UpdateCartResponse> {
                @SuppressLint("WrongConstant")
                override fun onResponse(call: Call<UpdateCartResponse>, response: Response<UpdateCartResponse>) {
                    Log.e(ContentValues.TAG, response.toString())
                    binding.subproductprogress.visibility = View.GONE
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
                    binding.subproductprogress.visibility = View.GONE
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
    private fun getCart() {

        if (NetWorkConection.isNEtworkConnected(activity as Activity)) {
            try{
                ( requireActivity() as HomeScreen).updateCartCount()
            }catch (e: java.lang.Exception)
            {

            }
            val apiServices = APIClient.client.create(Api::class.java)
            var call=  apiServices.getCartList(getString(R.string.api_key),customer_id =customerid)

            Log.e("cat", " customerid " + customerid)
            binding.subproductprogress.visibility = View.VISIBLE

            call?.enqueue(object : Callback<CartListResponse> {
                @SuppressLint("WrongConstant")
                override fun onResponse(call: Call<CartListResponse>, response: Response<CartListResponse>) {
                    Log.e(ContentValues.TAG, response.toString())
                    binding.subproductprogress.visibility = View.GONE

                    if (response.isSuccessful) {
                        try {

                            if (response.body()?.cartList?.size!! > 0) {
                                binding.cartcount.visibility = View.VISIBLE
                                binding.cvOneLogin.visibility = View.VISIBLE
                                binding.carttext.visibility = View.VISIBLE
                                binding.rlOneLogin.visibility = View.VISIBLE
                                binding.cartcount.text = "" + response.body()?.cartList?.size!!

                                Log.d("Added Cart Item","Added Cart Item 1 "+response.body()?.cartList?.size!!)

                            } else {
                                binding.cartcount.visibility = View.GONE
                                binding.cvOneLogin.visibility = View.GONE
                                binding.carttext.visibility = View.GONE
                                binding.rlOneLogin.visibility = View.GONE
                                binding.cartcount.text = ""
                                Log.d("Added Cart Item","Added Cart Item 0 "+response.body()?.cartList?.size!!)

                            }
                            subproduct_adapter.  setCartList(response.body()?.cartList!! as ArrayList<CartItems>)

                            /*subproduct_adapter =
                                SubProductList_Adapter(
                                    requireActivity() as Activity,
                                    selectedserviceslist as ArrayList<Category_subResponse>,
                                    response.body()?.cartList  as ArrayList<CartItems>, this@Products_Fragment
                                )
                            binding.subproductlist.adapter = subproduct_adapter
                          */
                            /*if(subproduct_adapter!=null) {
                                subproduct_adapter.cartData?.clear()
                                subproduct_adapter.cartData?.addAll(cartData)
                                subproduct_adapter.notifyDataSetChanged()
                            }*/


                            /*cartViewModel.getCartItems.observe(requireActivity()) {
                                    cartItems -> cartData = cartItems
                                Log.d("Cartview Items","Cartview Items 2 ${cartItems.size}")
                            }*/

                        } catch (e: java.lang.NullPointerException) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<CartListResponse>, t: Throwable) {
                    Log.e(ContentValues.TAG, t.toString())
                    binding.subproductprogress.visibility = View.GONE
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


    private fun sortProductsByOfferPrice() {     //filtr btn
        subproduct_adapter.sortByOfferPrice(ascending = isSortAscending)
        subproduct_adapter.notifyDataSetChanged()

    }


    private fun filterAndSortProducts(products: List<Category_subResponse>, query: String): List<Category_subResponse> {
        return products.filter { it.offer_price.contains(query, ignoreCase = true) }
            .sortedBy { it.offer_price.toDoubleOrNull() ?: 0.0 }// Filter based on product name matching the query
    }


    private fun getProducts() {

        if (NetWorkConection.isNEtworkConnected(activity as Activity)) {

            //Set the Adapter to the RecyclerView//


            var apiServices = APIClient.client.create(Api::class.java)

            val call =
                apiServices.getsubcategoruesList(getString(R.string.api_key), category_id)

            Log.e("cat", "" + category_id)
            binding.subproductprogress.visibility = View.VISIBLE
            call.enqueue(object : Callback<Category_subListResponse> {
                @SuppressLint("WrongConstant")
                override fun onResponse(
                    call: Call<Category_subListResponse>,
                    response: Response<Category_subListResponse>
                ) {

                    Log.e(ContentValues.TAG, response.toString())
                    binding.subproductprogress.visibility = View.GONE
                    getCart()
                    if (response.isSuccessful) {

                        try {

                            //Set the Adapter to the RecyclerView//

                             selectedserviceslist =
                                response.body()?.response!!

                           /* subproduct_adapter =
                                SubProductList_Adapter(
                                    activity as Activity,
                                    selectedserviceslist as ArrayList<Category_subResponse>,
                                    cartData as ArrayList<CartItems>, this@Products_Fragment
                                )
                            binding.subproductlist.adapter =
                                subproduct_adapter*/

                            subproduct_adapter.  setProductsList(selectedserviceslist as ArrayList<Category_subResponse>)
                           // subproduct_adapter.notifyDataSetChanged()
                            /*val layoutManager = LinearLayoutManager(activity as Activity, RecyclerView.VERTICAL, false)

                            binding.subproductlist.layoutManager = layoutManager

                            binding.subproductlist.setItemViewCacheSize(selectedserviceslist.size)*/
                        } catch (e: java.lang.NullPointerException) {
                            e.printStackTrace()
                        }

                    }

                }

                override fun onFailure(call: Call<Category_subListResponse>, t: Throwable) {
                    Log.e(ContentValues.TAG, t.toString())
                    binding.subproductprogress.visibility = View.GONE

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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding
    }

    override fun onProductItemClick(itemsData: Category_subResponse?) {

        ProductinDetails_Fragment().getData(itemsData?.products_id.toString(), true)!!
        val navController =
            Navigation.findNavController(
                context as Activity,
                R.id.nav_host_fragment_content_home_screen
            )
        navController.navigate(R.id.nav_product_details)


        val editor = sharedPreferences.edit()
        editor.putString("subcatid", itemsData?.products_id.toString())
        editor.commit()
    }

    override fun onAddToCartClicked(itemsData: Category_subResponse?, cartQty: String?,isAdd:Int) {
        if (isAdd == 0) {
            addToCart(itemsData, cartQty)
        } else
            updateCart(itemsData, cartQty)
        // Toast.makeText(activity, "Item added to cart successfully", Toast.LENGTH_LONG).show()
    }
}