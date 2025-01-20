package com.royalit.motherchoice.models

import com.google.gson.annotations.SerializedName

data class PlaceOrder_Response(


    @SerializedName("order_id") val order_id : Int,
    @SerializedName("amount") val amount : Int
)