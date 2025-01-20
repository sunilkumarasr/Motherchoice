package com.erepairs.app.models

import com.google.gson.annotations.SerializedName

data class Search_Response(

    @SerializedName("products_id") val products_id : Int,
    @SerializedName("categories_id") val categories_id : Int,
    @SerializedName("product_num") val product_num : String,
    @SerializedName("product_name") val product_name : String,
    @SerializedName("product_title") val product_title : String,
    @SerializedName("product_information") val product_information : String,
    @SerializedName("product_image") val product_image : String,
    @SerializedName("quantity") val quantity : String,
    @SerializedName("sales_price") val sales_price : String,
    @SerializedName("offer_price") val offer_price : String,
    @SerializedName("stock") val stock : String,
    @SerializedName("status") val status : String,
    @SerializedName("order_by") val order_by : String,
    @SerializedName("created_date") val created_date : String,
    @SerializedName("updated_date") val updated_date : String,
    @SerializedName("final_amount") val final_amount : String,
    @SerializedName("max_order_quantity") val max_order_quantity : String?,
    @SerializedName("category_2_price") val category_2_price : String?
)