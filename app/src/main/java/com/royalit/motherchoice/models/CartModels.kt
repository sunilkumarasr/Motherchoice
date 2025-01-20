package com.royalit.motherchoice.models

import com.erepairs.app.models.Areas_Response
import com.google.gson.annotations.SerializedName
import com.royalit.motherchoice.roomdb.CartItems

data class CartListResponse(
    @SerializedName("Status") val status: Boolean,
    @SerializedName("Message") val message: String,
    @SerializedName("Response") val cartList: List<CartItems>?,
    @SerializedName("code") val code: Int,
    @SerializedName("customer_category") val customer_category: Int?

)

data class UpdateCartResponse(
    @SerializedName("Status") val status: Boolean,
    @SerializedName("Message") val message: String,
    @SerializedName("code") val code: Int
)

data class AddtoCartResponse(
    @SerializedName("Status") val status: Boolean,
    @SerializedName("Message") val message: String,
    @SerializedName("Response") val cartList: CartAddresponse?,
    @SerializedName("code") val code: Int
)

data class CartAddresponse(
    @SerializedName("customer_id") val customer_id: String,
    @SerializedName("product_id") val product_id: String,
    @SerializedName("cart_id") val cart_id: String,
    @SerializedName("quantity") val quantity: String,
)

data class DeleteCartResponse(
    @SerializedName("Status") val status: Boolean,
    @SerializedName("Message") val message: String,
    @SerializedName("code") val code: Int
)



data class UserStatusResponse(
    @SerializedName("Status") val status: Boolean,
    @SerializedName("Message") val message: String,
    @SerializedName("Response") val userResponse: UserResponse?,
    @SerializedName("code") val code: Int,

)
data class UserResponse(
    @SerializedName("status") val status: String,


)