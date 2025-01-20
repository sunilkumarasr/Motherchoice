package com.royalit.motherchoice.adapter

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.royalit.motherchoice.R
import com.royalit.motherchoice.databinding.CategoriesAdapterBinding
import com.royalit.motherchoice.models.Category_ListResponse
import com.royalit.motherchoice.models.Category_Response
import retrofit2.Callback

class CategoryList_Adapter(
    val context: Context,
    private var languageList: List<Category_Response>

) : RecyclerView.Adapter<CategoryList_Adapter.ViewHolder>() {
    interface CategoryClickListener {
        fun onCategoryClick(categoryId: Int)
    }
    // create an inner class with name ViewHolder
    //It takes a view argument, in which pass the generated class of single_item.xml
    // ie SingleItemBinding and in the RecyclerView.ViewHolder(binding.root) pass it like this
    inner class ViewHolder(val binding: CategoriesAdapterBinding) :
        RecyclerView.ViewHolder(binding.root)

    // inside the onCreateViewHolder inflate the view of SingleItemBinding
    // and return new ViewHolder object containing this layout
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CategoriesAdapterBinding
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



                Glide.with(context).load(languageList.get(position).category_image)
                    .error(R.drawable.placeholder_image)
                    .transform(CenterCrop(), RoundedCorners(10))

                    .into(holder.binding.categoryImage)

                binding.categoryText.text = "" + languageList.get(position).category_name

                Log.e("category_image", "" + languageList.get(position).category_image)

                holder.itemView.setOnClickListener {

                    val sharedPreferences =
                        context.getSharedPreferences(
                            "loginprefs",
                            Context.MODE_PRIVATE)

                    val editor = sharedPreferences.edit()
                    editor.putString(
                        "categoryid",
                        languageList.get(position).categories_id.toString())
                    editor.commit()

                    val navController =
                        Navigation.findNavController(
                            context as Activity,
                            R.id.nav_host_fragment_content_home_screen)
                    navController.navigate(R.id.navigation_products)

                }

            }
        }
    }


    // return the size of languageList
    override fun getItemCount(): Int {
        return languageList.size
    }
}






