package com.royalit.motherchoice.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.erepairs.app.models.OrderHisProductDe_Response
import com.royalit.motherchoice.R
import com.royalit.motherchoice.databinding.MyOrderListItemBinding
import java.util.*

class OrdersFood(
    activity: Activity,
    itemsData: List<OrderHisProductDe_Response>
) :
    RecyclerView.Adapter<OrdersFood.ProductsVH?>() {
    var itemsData: List<OrderHisProductDe_Response> = ArrayList<OrderHisProductDe_Response>()
    var activity: Activity

    // inside the onCreateViewHolder inflate the view of SingleItemBinding
    // and return new ViewHolder object containing this layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersFood.ProductsVH {
        val binding = MyOrderListItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return OrdersFood.ProductsVH(binding)
    }


    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: ProductsVH, position: Int) {
        val data: OrderHisProductDe_Response = itemsData[position]
        holder.binding.nameTVID.text = data.product_name
        holder.binding.quantityTVID.text = "Quantity : " + data.qty
        holder.binding.priceTVID.text = "\u20b9 " + data.price
//        holder.binding.createdDateTVID.setText(data.)
//        holder.binding.deliveryStatusTVID.setText(data)

        Log.e("binding.imageI","binding.imageI ${data.image}")
        Glide.with(activity).load(data.image).error(R.drawable.logo)
            .placeholder(R.drawable.logo)
            .into(holder.binding.imageID)
        if (position == itemsData.size - 1) {
            holder.binding.viewID.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return itemsData.size
    }

    class ProductsVH(binding: MyOrderListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        var binding: MyOrderListItemBinding = binding

    }


    init {
        this.itemsData = itemsData
        this.activity = activity
    }
}