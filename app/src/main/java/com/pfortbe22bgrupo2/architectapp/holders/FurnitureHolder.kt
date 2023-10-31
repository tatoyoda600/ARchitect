package com.pfortbe22bgrupo2.architectapp.holders

import android.view.View
import android.widget.TextView
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

    fun setImageUrl(imageUrl : Int, context: View) {
       // binding.itemImageViewId.setImageResource(imageUrl)
        Glide.with(context).load(imageUrl).into(binding.itemImageViewId)
    }

    fun getCardLayout(): CardView {
        return binding.furnitureCardView
    }
}