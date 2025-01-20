package com.royalit.motherchoice.adapter

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.royalit.motherchoice.R
import com.royalit.motherchoice.databinding.SubproductAdapterBinding
import com.royalit.motherchoice.models.Category_subResponse
import com.royalit.motherchoice.models.Product_Response
import com.royalit.motherchoice.models.Product_priceresponse
import com.royalit.motherchoice.roomdb.CartItems
import com.royalit.motherchoice.utils.NetWorkConection
import com.royalit.motherchoice.utils.Utilities
import java.lang.Exception
import java.util.Locale

class SubProductList_Adapter(
    val context: Activity,
    var languageList: ArrayList<Category_subResponse>,
    var cartData: ArrayList<CartItems>?,
    var click: ProductItemClick?,
    private var lastVisibleItemPosition: Int = 0

) : RecyclerView.Adapter<SubProductList_Adapter.ViewHolder>() {

    // create an inner class with name ViewHolder
    //It takes a view argument, in which pass the generated class of single_item.xml
    // ie SingleItemBinding and in the RecyclerView.ViewHolder(binding.root) pass it like this
    inner class ViewHolder(val binding: SubproductAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    // inside the onCreateViewHolder inflate the view of SingleItemBinding
    // and return new ViewHolder object containing this layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SubproductAdapterBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }


    companion object;

    // bind the items with each item of the list languageList which than will be
    // shown in recycler view
    // to keep it simple we are not setting any image data to view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder) {
            with(languageList[position]) {
                // set name of the language from the list
//                binding.brndsTitleText.text = languageList.get(position).prodcut_name
//                binding.brandNameText.text = languageList.get(position).prodcut_desc

//                var data: Category_subResponse = languageList.get(position)
                //check user cart qty
                holder.binding.qtyBtn.text = "0";
                for (j in cartData!!.indices) {
                    if (cartData!!.get(j).getProduct_id()
                            .toInt() == (languageList.get(position).products_id.toInt())
                    ) {
                        Log.e("Product CArt data", "" + languageList.get(position).products_id.toInt()+" : "+cartData!!.get(j).getProduct_id().toInt()+" : "+cartData!!.get(j).getCartQty())


                        holder.binding.qtyBtn.text = "" + cartData!![j].cartQty

                        holder.binding.addtocartBtn.text = "Update"
                        Log.d("Added Cart Item","Added Cart Item inside adapter"+languageList[position].cart_id)
                        languageList[position].cart_id=cartData!!.get(j).cartID.toString()
                    } else {
                        //holder.binding.qtyBtn.text = "0"
                       // holder.binding.addtocartBtn.text = "Add"
                    }
                }

              /*  if (position <= lastVisibleItemPosition) {
                    // Load the image using Glide
                    Glide.with(context)
                        .load(languageList[position].product_image)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.rice)
                        .transform(CenterCrop(), RoundedCorners(10))
                        .into(holder.binding.subproductImage)
                }*/

                Glide.with(context)
                    .load(languageList[position].product_image)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.rice)

                    .into(holder.binding.subproductImage)
                binding.productQnty.text = "Quantity : " + languageList.get(position).quantity
                binding.subproductName.text = "" + languageList.get(position).product_name
//                binding.subproductDesc.text = "" + languageList.get(position).product_title
                if(Utilities.customer_category==2)
                binding.offerPrice.text = "\u20B9" + languageList.get(position).category_2_price
                else
                binding.offerPrice.text = "\u20B9" + languageList.get(position).offer_price
                binding.salePrice.text = "\u20B9" + languageList.get(position).sales_price


                binding.salePrice.paintFlags =
                    binding.salePrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG


                if (languageList.get(position).stock.toInt() == 0) {
                    binding.outofstockBtn.visibility = View.VISIBLE
                    binding.minusBtn.visibility = View.GONE
                    binding.qtyBtn.visibility = View.GONE
                    binding.plusBtn.visibility = View.GONE

                } else {
                    binding.outofstockBtn.visibility = View.GONE
                    binding.minusBtn.visibility = View.VISIBLE
                    binding.qtyBtn.visibility = View.VISIBLE
                    binding.plusBtn.visibility = View.VISIBLE
                }
                Log.e("category_image", "" + languageList.get(position).product_image)
                holder.binding.subproductImage.setOnClickListener { v ->
                    click!!.onProductItemClick(
                        languageList.get(
                            position
                        )
                    )
                }
                holder.binding.subproductName.setOnClickListener { v ->
                    click!!.onProductItemClick(
                        languageList.get(
                            position
                        )
                    )
                }


                val cartQty = intArrayOf(holder.binding.qtyBtn.text.toString().toInt())
                Log.e("item_cart_qty==>", "" + cartQty[0])
                binding.plusBtn.setOnClickListener {

                    val carstQty1: String = holder.binding.qtyBtn.text.toString()
                    Log.e("item_cart_stock==>", "" + languageList.get(position).stock)
                    Log.e("item_cart_cart==>", "" + carstQty1)

                    if (languageList.get(position).stock == carstQty1) {

                        Toast.makeText(
                            context,
                            "Stock Limit only " + languageList.get(position).stock,
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        Log.e("item_cart_==>", "" + cartQty[0])
                        Log.e("item_cart_stock==>", "" + languageList.get(position).stock)
                        val carstQty = binding.qtyBtn.text.toString()
                        Log.e("Order Quantity","Order Quantity max ${languageList.get(position).max_order_quantity} $carstQty")
                        if ((languageList.get(position).max_order_quantity!=null)&& (languageList.get(position).max_order_quantity!!.toInt()<=carstQty.toInt())){
                            Toast.makeText(context, "Can't add Max Quantity for this Product", Toast.LENGTH_LONG).show()

                            return@setOnClickListener
                        }
                        cartQty[0]++
                        holder.binding.qtyBtn.text = "" + cartQty.get(0)
                        try {


                            val carstQty: String = holder.binding.qtyBtn.text.toString()

                            if (carstQty == "0") {
                                Toast.makeText(context, "Select quantity..", Toast.LENGTH_LONG)
                                    .show()
                            } else {
                                if (NetWorkConection.isNEtworkConnected(context)) {
                                    if(carstQty == "1")
                                    click?.onAddToCartClicked(languageList.get(position), carstQty,0)
                                    else
                                    click?.onAddToCartClicked(languageList.get(position), carstQty,1)
                                }else
                                {
                                    Toast.makeText(context,
                                        "Please Check your internet",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                Log.e("data", "" + languageList.get(position))
                                Log.e("carstQty", carstQty)
                            }
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        }
                    }

                }

                holder.binding.minusBtn.setOnClickListener { v ->
                    if (cartQty[0] > 1) {
                        cartQty[0]--
                    }
//                    else if (cartQty[0] == 1){
//                        val builder1 =
//                            AlertDialog.Builder(context)
//                        builder1.setCancelable(false)
//                        builder1.setTitle(null)
//                        builder1.setMessage("Are you sure \nDo you want to delete this item from cart")
//                        builder1.setPositiveButton(
//                            "Yes"
//                        ) {
//                                dialogInterface: DialogInterface?, i1: Int ->
//
//
//                        }
//                        builder1.setNegativeButton("No", null)
//                        builder1.create().show()
//                    }


                    holder.binding.qtyBtn.text = "" + cartQty[0]
                    try {


                        val carstQty: String = holder.binding.qtyBtn.text.toString()

                        if (carstQty == "0") {
                            Toast.makeText(context, "Select quantity..", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            if (NetWorkConection.isNEtworkConnected(context)) {
                                click?.onAddToCartClicked(languageList.get(position), carstQty,1)
                            }else
                            {
                                Toast.makeText(context,
                                    "Please Check your internet",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            Log.e("data", "" + languageList.get(position))
                            Log.e("carstQty", carstQty)
                        }
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                }

                holder.binding.addtocartBtn.setOnClickListener {
                    try {


                        val carstQty: String = holder.binding.qtyBtn.text.toString()

                        if (carstQty == "0") {
                            Toast.makeText(context, "Select quantity..", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            if (NetWorkConection.isNEtworkConnected(context)) {
                                click?.onAddToCartClicked(languageList.get(position), carstQty,0)
                            }else
                            {
                                Toast.makeText(context,
                                    "Please Check your internet",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            Log.e("data", "" + languageList.get(position))
                            Log.e("carstQty", carstQty)
                        }
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    interface ProductItemClick {
        fun onProductItemClick(languageList: Category_subResponse?)
        fun onAddToCartClicked(languageList: Category_subResponse?, cartQty: String?,isAdd:Int)

    }

    // return the size of languageList
    override fun getItemCount(): Int {
        return languageList.size
    }

    fun sortByOfferPrice(ascending: Boolean) {    //for filter button
        if (ascending) {
            if(Utilities.customer_category==2)
            languageList.sortBy { it.category_2_price?.toInt() }
            else
            languageList.sortBy { it.offer_price.toInt() }

        } else {
            if(Utilities.customer_category==2)
            languageList.sortByDescending { it.category_2_price?.toInt() }
            else
            languageList.sortByDescending { it.offer_price.toInt() }
        }
        notifyDataSetChanged()
    }


    fun resetData(newData: List<Category_subResponse>) {
        languageList.clear()
        languageList.addAll(newData)
        notifyDataSetChanged()
    }
    fun resetCartData(cartDatas:ArrayList<CartItems>) {
        cartData?.clear()
        cartData?.addAll(cartDatas)
        notifyDataSetChanged()
    }

    fun filterProducts(query: String) {
        val filteredList = ArrayList<Category_subResponse>()

        if (query.isEmpty()) {
            // If the search query is empty, show the original data
            filteredList.addAll(languageList)
        } else {
            // Customize this filter logic based on your criteria
            // Here, we are filtering by offer price
            val filterPattern = query.toLowerCase(Locale.ROOT).trim()
            filteredList.addAll(languageList.filter { item ->
                item.offer_price.contains(filterPattern, ignoreCase = true)
            })
        }

        // Update the adapter with the filtered products
        resetData(filteredList)
    }

    fun setProductsList(languageLists:ArrayList<Category_subResponse>)
    {
        try {
            languageList.clear()
            languageList.addAll(languageLists)
            notifyDataSetChanged()
        }catch (e:Exception)
        {

        }
    }
    fun setCartList(cartDatas:ArrayList<CartItems>)
    {
        try {
            cartData?.clear()
            cartData?.addAll(cartDatas)
            notifyDataSetChanged()
        }catch (e:Exception)
        {

        }
    }
    // ...
}


