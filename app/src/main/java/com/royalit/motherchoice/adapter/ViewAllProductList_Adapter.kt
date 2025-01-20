package com.royalit.motherchoice.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.royalit.motherchoice.R
import com.royalit.motherchoice.databinding.HomeproductsAdapterBinding
import com.royalit.motherchoice.models.Product_Response
import com.royalit.motherchoice.roomdb.CartItems
import com.royalit.motherchoice.utils.Utilities
import java.util.*

class ViewAllProductList_Adapter(
    val context: Context,
    var languageList: ArrayList<Product_Response>,
    var cartData: ArrayList<CartItems>,
    var click: ProductItemClick?,

) : RecyclerView.Adapter<ViewAllProductList_Adapter.ViewHolder>() {


    // create an inner class with name ViewHolder
    //It takes a view argument, in which pass the generated class of single_item.xml
    // ie SingleItemBinding and in the RecyclerView.ViewHolder(binding.root) pass it like this
    inner class ViewHolder(val binding: HomeproductsAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    // inside the onCreateViewHolder inflate the view of SingleItemBinding
    // and return new ViewHolder object containing this layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HomeproductsAdapterBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return languageList.size
    }

    fun ViewAllProductList_Adapter(
        itemsData: ArrayList<Product_Response>,
        click: ProductItemClick?,
        cartData: ArrayList<CartItems>
    ) {
        this.languageList = itemsData
        this.click = click
        this.cartData = cartData
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

                Glide.with(context).load(languageList.get(position).product_image)
                    .error(R.drawable.placeholder_image)

                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Don't cache the image
                    .priority(Priority.HIGH)
                    .into(holder.binding.productImage)
                for (j in cartData!!.indices) {
                    if (cartData!!.get(j).getProduct_id()
                            .toInt() == (languageList.get(position).products_id.toInt())
                    ) {
                        holder.binding.quntyText.text = "" + cartData!![j].getCartQty()


                        Log.e("addtocart qty", "" + cartData?.get(j)?.getCartQty())

                    } else {
                        // holder.binding.addtocartBtn.text = "Add"
                    }
                }
                binding.productText.text = "" + languageList.get(position).product_name
                if(Utilities.customer_category==2)
                binding.offerPrice.text = "\u20B9" + languageList.get(position).category_2_price
                else
                binding.offerPrice.text = "\u20B9" + languageList.get(position).offer_price
                binding.salePrice.text = "\u20B9" + languageList.get(position).sales_price

                Log.e("category_image", "" + languageList.get(position).product_image)
                binding.salePrice.paintFlags =
                    binding.salePrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                if (languageList.get(position).stock.toInt() == 0) {
                    binding.outofstockBtn.visibility = View.VISIBLE
                    binding.addLayout.visibility = View.GONE
                } else {
                    binding.outofstockBtn.visibility = View.GONE
                    binding.addLayout.visibility = View.VISIBLE
                }
                val cartQty = intArrayOf(holder.binding.quntyText.text.toString().toInt())
                binding.plusImage.setOnClickListener {

                    val carstQty: String = holder.binding.quntyText.text.toString()

                    if (languageList.get(position).stock == carstQty) {

                        Toast.makeText(
                            context,
                            "Stock Limit only " + languageList.get(position).stock,
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        Log.e("item_cart_qty==>", "" + cartQty[0])
                        if ((languageList.get(position).max_order_quantity!=null)&& (languageList.get(position).max_order_quantity!!.toInt()<=carstQty.toInt())){
                            Toast.makeText(context, "Can't add Max Quantity for this Product", Toast.LENGTH_LONG).show()

                            return@setOnClickListener
                        }
                        cartQty[0]++
                        holder.binding.quntyText.text = "" + cartQty.get(0)
                        holder.binding.addtocartBtn.performClick()
                    }

                }

                holder.binding.minusimgeBtn.setOnClickListener { v ->
                    if (cartQty[0] > 1) {
                        cartQty[0]--
                        holder.binding.quntyText.text = "" + cartQty[0]
                        holder.binding.addtocartBtn.performClick()
                    }

                }

                holder.binding.addtocartBtn.setOnClickListener {
                    try {


                        val carstQty: String = holder.binding.quntyText.text.toString()

                        if (carstQty == "0") {
                            Toast.makeText(context, "Select quantity..", Toast.LENGTH_LONG)
                                .show()
                        } else {
                            if(carstQty == "1")
                            click?.onAddToCartClicked(languageList.get(position), carstQty,0)
                           else
                               click?.onAddToCartClicked(languageList.get(position), carstQty,1)
                            Log.e("data", "" + languageList.get(position))
                            Log.e("carstQty", carstQty)
                        }
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                }


            }

            holder.binding.productImage.setOnClickListener {
                if (languageList.get(position).stock.toInt() == 0) {

                    Toast.makeText(
                        context,
                        "Out Of Stack",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
                val sharedPreferences =
                    context.getSharedPreferences(
                        "loginprefs",
                        Context.MODE_PRIVATE
                    )
                val navController = Navigation.findNavController(
                    context as Activity,
                    R.id.nav_host_fragment_content_home_screen
                )
                navController.navigate(R.id.nav_product_details)


                val editor = sharedPreferences.edit()
                editor.putString("subcatid", languageList.get(position).products_id.toString())
                editor.commit()
            }
            holder.binding.productText.setOnClickListener {
                if (languageList.get(position).stock.toInt() == 0) {

                    Toast.makeText(
                        context,
                        "Out Of Stack",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
                val sharedPreferences =
                    context.getSharedPreferences(
                        "loginprefs",
                        Context.MODE_PRIVATE
                    )
                val navController = Navigation.findNavController(
                    context as Activity,
                    R.id.nav_host_fragment_content_home_screen
                )
                navController.navigate(R.id.nav_product_details)


                val editor = sharedPreferences.edit()
                editor.putString("subcatid", languageList.get(position).products_id.toString())
                editor.commit()
            }
        }

    }


    interface ProductItemClick {
        fun onProductItemClick(itemsData: Product_Response?)
        fun onAddToCartClicked(itemsData: Product_Response, cartQty: String?,isAdd:Int)
    }

    fun sortByOfferPrice(ascending:Boolean) {
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
    // Filter the list by offer price
    fun filterItemsByPrice(query: String) {
        val filteredList = ArrayList<Product_Response>()

        if (query.isEmpty()) {
            filteredList.addAll(languageList)
        } else {
            val filterPattern = query.toLowerCase(Locale.ROOT).trim()
            filteredList.addAll(languageList.filter { item ->
                item.offer_price.toString().contains(filterPattern)
            })
        }

        languageList = filteredList
        notifyDataSetChanged()
    }
    fun resetData(newData: List<Product_Response>) {
        languageList.clear()
        languageList.addAll(newData)
        notifyDataSetChanged()
    }
    fun updateData(newData: List<Product_Response>) {
        languageList.clear()
        languageList.addAll(newData)
        notifyDataSetChanged()
    }

    fun addProductsLIst(languageLists: ArrayList<Product_Response>, cartDatas: ArrayList<CartItems>)
    {
        if(languageList!=null) {
            languageList.clear()
            languageList.addAll(languageLists)

        }
        if(cartData!=null) {
            cartData.clear()
            cartData.addAll(cartDatas)
            notifyDataSetChanged()
        }
        notifyDataSetChanged()
    }

    fun addCartData(cartDatas: ArrayList<CartItems>)
    {

    }
}