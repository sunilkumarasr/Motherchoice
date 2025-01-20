package com.royalit.motherchoice.models

import com.google.gson.annotations.SerializedName

data class Placeorder_ListResponse(

    @SerializedName("Status") val status : Boolean,
    @SerializedName("Message") val message : String,
    @SerializedName("customer_status") var customer_status : String?,
    @SerializedName("Response") val response : PlaceOrder_Response?,
    @SerializedName("code") val code : Int
)