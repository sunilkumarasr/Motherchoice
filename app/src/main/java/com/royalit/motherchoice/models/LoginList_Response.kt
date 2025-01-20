package com.royalit.motherchoice.models

import com.google.gson.annotations.SerializedName

data class LoginList_Response(

    @SerializedName("Status") val status: Boolean,
    @SerializedName("Message") val message: String,
    @SerializedName("success") val success: Boolean,
    @SerializedName("order_id") val order_id: String,
    @SerializedName("Response") val response: Login_Response,
    @SerializedName("code") val code: Int
)