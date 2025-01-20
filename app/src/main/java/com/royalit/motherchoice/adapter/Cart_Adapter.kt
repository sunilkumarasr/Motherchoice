package com.erepairs.app.adapter


import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.royalit.motherchoice.R
import com.royalit.motherchoice.databinding.CartListItemBinding
import com.royalit.motherchoice.roomdb.CartItems
import com.royalit.motherchoice.roomdb.CartViewModel
import com.royalit.motherchoice.utils.Utilities

class Cart_Adapter(
    val context: Context,
    var itemsData: ArrayList<CartItems>,
    var cartData: List<CartItems>?,
    var click: ProductItemClick?,
    var quantityChangeListener: CartItemQuantityChangeListener?
) : RecyclerView.Adapter<Cart_Adapter.ViewHolder>() {
    var serviceidlist: MutableList<String> = ArrayList()
    lateinit var usersubcategory_id_: String

    // create an inner class with name ViewHolder
    //It takes a view argument, in which pass the generated class of single_item.xml
    // ie SingleItemBinding and in the RecyclerView.ViewHolder(binding.root) pass it like this
    inner class ViewHolder(val binding: CartListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    // inside the onCreateViewHolder inflate the view of SingleItemBinding
    // and return new ViewHolder object containing this layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CartListItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    companion object;

    // bind the items with each item of the list languageList which than will be
    // shown in recycler view
    // to keep it simple we are not setting any image data to view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        with(holder) {
            with(itemsData[position]) {
                // set name of the language from the list
//                binding.brndsTitleText.text = languageList.get(position).prodcut_name
//                binding.brandNameText.text = languageList.get(position).prodcut_desc

                val data: CartItems = itemsData.get(position)

                holder.binding.cartQty.text = "" + data.cartQty
                if(Utilities.customer_category==2)
                holder.binding.price.text = "₹ " + data.category_2_price+" )"
                else
                holder.binding.price.text = "₹ " + data.offer_price+" )"

                val cartQty = intArrayOf(holder.binding.cartQty.text.toString().toInt())
                Log.e("item_cart_qty==>", "" + cartQty[0])
                holder.binding.root.setOnClickListener { v ->
                    click!!.onProductItemClick(
                        itemsData.get(
                            position
                        )
                    )
                }
                holder.binding.nameTVID.text = data.product_name
                holder.binding.qtyTVID.text = data.cartQty.toString() + ""
//        holder.binding.priceTVID.setText("Total : \u20b9 " + data.getFinal_price());

                //        holder.binding.priceTVID.setText("Total : \u20b9 " + data.getFinal_price());
                try {
                    if(Utilities.customer_category==2) {
                        val final_amt: Int = data.category_2_price.toInt() * data.cartQty.toInt()
                        holder.binding.priceTVID.text = "Total : \u20b9 $final_amt"
                    }else{
                        val final_amt: Int = data.getOffer_price().toInt() * data.cartQty.toInt()
                        holder.binding.priceTVID.text = "Total : \u20b9 $final_amt"
                    }

                } catch (e: NullPointerException) {
                    e.printStackTrace()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                }
                holder.binding.weightVID.text = "" + data.weight


                holder.binding.removeTVID.setOnClickListener { v ->
                    val builder1 =
                        AlertDialog.Builder(context)
                    builder1.setCancelable(false)
                    builder1.setTitle(null)
                    builder1.setMessage("Are you sure \nDo you want to delete this item from cart")
                    builder1.setPositiveButton(
                        "Yes"
                    ) {
                        dialogInterface: DialogInterface?, i1: Int ->
                        quantityChangeListener?.onDeleteCartItem(data)

                    }
                    builder1.setNegativeButton("No", null)
                    builder1.create().show()
                }

                Log.e("data.getItemImage()","data.getItemImage() ${data.getItemImage()}")
                Glide.with(holder.binding.imageID.context)
                    .load("https://motherchoiceproducts.in/uploads/products/"+data.getItemImage())
                    .placeholder(R.drawable.logo)
                    .into(holder.binding.imageID)

                holder.binding.cartIncBtn.setOnClickListener { v ->


                    val carstQty = binding.cartQty.text.toString()
                    if ((data.max_order_quantity!=null)&& (data.max_order_quantity.toInt()<=carstQty.toInt())){
                        Toast.makeText(context, "Can't add Max Quantity for this Product", Toast.LENGTH_LONG).show()

                        return@setOnClickListener
                    }
                    if (data.stock == carstQty) {

                        Toast.makeText(
                            context,
                            "Stock Limit only " + data.stock,
                            Toast.LENGTH_LONG
                        ).show()
                    }else {
                        Log.e("item_cart_==>", "" + carstQty[0])

                        cartQty[0]++
                         if ((data.max_order_quantity!=null)&& (data.max_order_quantity.toInt()<=carstQty.toInt())){
                             Toast.makeText(context, "Can't add Max Quantity for this Product", Toast.LENGTH_LONG).show()

                             return@setOnClickListener
                        }
                        holder.binding.cartQty.text = cartQty[0].toString()

                        // Notify the quantity change to the listener
                        quantityChangeListener?.onQuantityChanged(data, cartQty[0])
                        holder.binding.cartQty.text = "" + cartQty.get(0)
                        holder.binding.addToCartBtn.performClick()
                    }


                }

                holder.binding.addToCartBtn.text = "Update"

                holder.binding.cartDecBtn.setOnClickListener { v ->
                    if (cartQty[0] > 1) {
                        cartQty[0]--
                        holder.binding.cartQty.text = "" + cartQty[0]
                        holder.binding.cartQty.text = cartQty[0].toString()

                        // Notify the quantity change to the listener
                       /* if(cartQty[0]==1)
                        quantityChangeListener?.onQuantityChanged(data, 0)
                        else
                        quantityChangeListener?.onQuantityChanged(data, cartQty[0])
*/
                        val carstQty = holder.binding.cartQty.text.toString()

                        if(cartQty[0]==1)
                        click!!.onAddToCartClicked(data, carstQty,1)
                        else
                        click!!.onAddToCartClicked(data, carstQty,1)
                       // holder.binding.addToCartBtn.performClick()
                    }

                }

                holder.binding.addToCartBtn.setOnClickListener { v ->
                    val carstQty = holder.binding.cartQty.text.toString()
                    if (carstQty == "0") {

                        Toast.makeText(context, "Select quantity..", Toast.LENGTH_LONG).show()

                    }else {
                        if(carstQty == "1")
                        click!!.onAddToCartClicked(data, carstQty,0)
                        else
                        click!!.onAddToCartClicked(data, carstQty,1)
                    }
                }


            }
        }
    }

    interface ProductItemClick {
        fun onProductItemClick(itemsData: CartItems?)
        fun onAddToCartClicked(itemsData: CartItems?, cartQty: String?,isAdd:Int)
    }

    interface CartItemQuantityChangeListener {
        fun onQuantityChanged(cartItem: CartItems, newQuantity: Int)
        fun onDeleteCartItem(cartItem: CartItems)
    }


    // return the size of languageList
    override fun getItemCount(): Int {
        return itemsData.size
    }
}