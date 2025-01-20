package com.royalit.motherchoice.adapter

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.drawable.Drawable
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
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.target.Target
import com.royalit.motherchoice.R
import com.royalit.motherchoice.databinding.HomeproductsAdapterBinding
import com.royalit.motherchoice.models.Product_Response
import com.royalit.motherchoice.roomdb.CartItems
import com.royalit.motherchoice.utils.Utilities
import java.util.*


class ProductList_Adapter(val context: Context,
                          var languageList: ArrayList<Product_Response>,
                          var cartData: List<CartItems>?,
                          var click: ProductItemClick) : RecyclerView.Adapter<ProductList_Adapter.ViewHolder>() {


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
                    Log.e("Product CArt data", "" + languageList.get(position).products_id.toInt()+" : "+cartData!!.get(j).getProduct_id()
                        .toInt()+" : "+cartData!!.get(j).getCartQty())

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

//                Glide.with(context).load(languageList.get(position).product_image)
//                    .error(R.drawable.placeholder_image)
//                    .transform(CenterCrop(), RoundedCorners(10))
//                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Don't cache the image
//                    .priority(Priority.IMMEDIATE)
//                    .into(holder.binding.productImage)


                try {
                    Glide.with(context)
                        .load(languageList[position].product_image)
                        .error(R.drawable.placeholder_image)
                        .transform(CenterCrop(), RoundedCorners(10))
                        .diskCacheStrategy(DiskCacheStrategy.ALL) // Don't cache the image
                        .priority(Priority.IMMEDIATE)
                        .into(holder.binding.productImage)
                } catch (e: GlideException) {
                    // Handle the Glide exception
                    e.logRootCauses("Glide Root Causes")
                    e.printStackTrace()
                }



                binding.productText.text = "" + languageList.get(position).product_name
                if(Utilities.customer_category==2)
                binding.offerPrice.text = "\u20B9" + languageList.get(position).category_2_price
                else
                binding.offerPrice.text = "\u20B9" + languageList.get(position).offer_price
                binding.salePrice.text = "\u20B9" + languageList.get(position).sales_price

                binding.salePrice.paintFlags =
                    binding.salePrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                Log.e("category_image", "" + languageList.get(position).sales_price)

                val cartQty = intArrayOf(holder.binding.quntyText.text.toString().toInt())
                Log.e("item_cart_qty==>", "" + cartQty[0])

                if(languageList.get(position).stock.toInt()==0){
                    binding.outofstockBtn.visibility= View.VISIBLE
                    binding.addLayout.visibility= View.GONE
                }else{
                    binding.outofstockBtn.visibility= View.GONE
                    binding.addLayout.visibility= View.VISIBLE
                }

                binding.plusImage.setOnClickListener {
                    val carstQty: String = holder.binding.quntyText.text.toString()
                    Log.e("item_cart_cart==>", "" + cartQty)

                    if (languageList.get(position).stock == carstQty) {

                        Toast.makeText(
                            context,
                            "Stock Limit only " + languageList.get(position).stock,
                            Toast.LENGTH_LONG
                        ).show()

                    } else {
                        val carstQty = binding.quntyText.text.toString()
                        Log.e("Order Quantity","Order Quantity ${languageList.get(position).max_order_quantity}")
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

                holder.binding.productImage.setOnClickListener {

                    val sharedPreferences = context.getSharedPreferences("loginprefs", Context.MODE_PRIVATE)
                    val navController =
                        Navigation.findNavController(
                            context as Activity,
                            R.id.nav_host_fragment_content_home_screen)
                    navController.navigate(R.id.nav_product_details)


                    val editor = sharedPreferences.edit()
                    editor.putString("subcatid", languageList.get(position).products_id.toString())
                    editor.commit()
                }
                holder.binding.productText.setOnClickListener {

                    val sharedPreferences =
                        context.getSharedPreferences(
                            "loginprefs",
                            Context.MODE_PRIVATE
                        )
                    val navController =
                        Navigation.findNavController(
                            context as Activity,
                            com.royalit.motherchoice.R.id.nav_host_fragment_content_home_screen
                        )
                    navController.navigate(com.royalit.motherchoice.R.id.nav_product_details)


                    val editor = sharedPreferences.edit()
                    editor.putString("subcatid", languageList.get(position).products_id.toString())
                    editor.commit()
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

            }
        }
    }

    interface ProductItemClick {
        fun onProductItemClick(languageList: Product_Response?)
        fun onAddToCartClicked(languageList: Product_Response?, cartQty: String?,isAdd:Int)
    }


    // return the size of languageList
    override fun getItemCount(): Int {
        if (languageList.size > 10) {

            return 10
        }
        return languageList.size
        //return languageList.size
    }
}