package com.royalit.motherchoice.adapter

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.erepairs.app.models.Search_Response
import com.royalit.motherchoice.R
import com.royalit.motherchoice.databinding.SearchproductsAdapterBinding
import com.royalit.motherchoice.roomdb.CartItems
import com.royalit.motherchoice.utils.Utilities

class Search_Adapter(
    val context: Context,
    var languageList: ArrayList<Search_Response>,
    var cartData: List<CartItems>?,
    var click: SearchItemClick

) : RecyclerView.Adapter<Search_Adapter.ViewHolder>() {


    // create an inner class with name ViewHolder
    //It takes a view argument, in which pass the generated class of single_item.xml
    // ie SingleItemBinding and in the RecyclerView.ViewHolder(binding.root) pass it like this
    inner class ViewHolder(val binding: SearchproductsAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    // inside the onCreateViewHolder inflate the view of SingleItemBinding
    // and return new ViewHolder object containing this layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SearchproductsAdapterBinding
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

                //check user cart qty
                for (j in cartData!!.indices) {
                    if (cartData!!.get(j).getProduct_id()
                            .toInt() == (languageList.get(position).products_id.toInt())
                    ) {
                        holder.binding.quntyText.text = "" + cartData!![j].getCartQty()

                        holder.binding.addtocartBtn.text = "Update"
                        Log.e("addtocart qty", "" + cartData?.get(j)?.getCartQty())

                    } else {
                        holder.binding.addtocartBtn.text = "Add"
                    }
                }

                Glide.with(context).load(languageList.get(position).product_image)
                    .error(R.drawable.ic_launcher_background)
                    .transform(CenterCrop(), RoundedCorners(10))
                    .into(holder.binding.sProductImage)

                binding.sProductText.text = "" + languageList.get(position).product_name
                if(Utilities.customer_category==2)
                binding.offerPrice.text = "\u20B9" + languageList.get(position).category_2_price
                else
                binding.offerPrice.text = "\u20B9" + languageList.get(position).offer_price
                binding.salePrice.text = "\u20B9" + languageList.get(position).sales_price

                Log.e("category_image", "" + languageList.get(position).sales_price)
                if (languageList.get(position).stock.toInt() == 0) {
                    binding.outofstockBtn.visibility = View.VISIBLE
                    binding.addLayout.visibility = View.GONE
                } else {
                    binding.outofstockBtn.visibility = View.GONE
                    binding.addLayout.visibility = View.VISIBLE
                }

                holder.binding.sProductImage.setOnClickListener { v ->
                    click.SearchItemClick(
                        languageList.get(
                            position))
                }
                holder.binding.sProductText.setOnClickListener { v ->
                    click.SearchItemClick(
                        languageList.get(
                            position))
                }

                val cartQty = intArrayOf(holder.binding.quntyText.text.toString().toInt())
                Log.e("item_cart_qty==>", "" + cartQty[0])

                binding.salePrice.paintFlags =
                    binding.salePrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                binding.plusImage.setOnClickListener {
                    val carstQty: String = holder.binding.quntyText.text.toString()
                    if (languageList.get(position).stock == carstQty) {

                        Toast.makeText(
                            context,
                            "Stock Limit only " + languageList.get(position).stock,
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        Log.e("item_cart_==>", "" + carstQty[0])
                        Log.e("item_cart_stock==>", "" + languageList.get(position).stock)
                        val carstQty = binding.quntyText.text.toString()
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
                    val carstQty: String = holder.binding.quntyText.text.toString()

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
                            click.onAddToCartClicked(languageList.get(position), carstQty,0)
                            else
                            click.onAddToCartClicked(languageList.get(position), carstQty,1)
                            Log.e("carstQty", carstQty)
                        }
                    } catch (e: NullPointerException) {
                        e.printStackTrace()
                    }
                }

            }
        }
    }

    interface SearchItemClick {
        fun SearchItemClick(languageList: Search_Response?)
        fun onAddToCartClicked(languageList: Search_Response, cartQty: String?,isAss:Int)
    }


    // return the size of languageList
    override fun getItemCount(): Int {

        return languageList.size
        //return languageList.size
    }
}