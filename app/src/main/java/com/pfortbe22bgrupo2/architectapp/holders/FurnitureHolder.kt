package com.pfortbe22bgrupo2.architectapp.holders

import android.view.View
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pfortbe22bgrupo2.architectapp.databinding.FurnitureItemBinding

class FurnitureHolder(binding: FurnitureItemBinding): RecyclerView.ViewHolder(binding.root) {
    private var binding: FurnitureItemBinding
    //private var view: View

    init {
        this.binding = binding
        //this.view = binding.root
    }

    fun setName(nombre:String) {
        binding.furnitureNameTextView.text = nombre
    }

    fun setImage(imageUrl: String, context: View){
        Glide.with(context).load(imageUrl).into(binding.itemImageViewId)
    }

    fun getCardLayout(): CardView {
        return binding.furnitureCardView
    }
}