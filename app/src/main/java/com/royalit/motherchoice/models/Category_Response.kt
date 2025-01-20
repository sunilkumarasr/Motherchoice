package com.royalit.motherchoice.models

import com.google.gson.annotations.SerializedName

data class Category_Response(

    @SerializedName("categories_id") val categories_id : Int?,
    @SerializedName("category_name") val category_name : String?,
    @SerializedName("category_image") val category_image : String?
)