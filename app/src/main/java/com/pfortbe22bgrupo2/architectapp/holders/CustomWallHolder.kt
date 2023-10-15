package com.pfortbe22bgrupo2.architectapp.holders

import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.databinding.CustomWallItemBinding

class CustomWallHolder(binding: CustomWallItemBinding): RecyclerView.ViewHolder(binding.root) {
    private var binding: CustomWallItemBinding

    init {
        this.binding = binding
    }

    fun setDescription(description: String){
        binding.custonWallItenTextView.text = description
    }

    fun getCardLayout(): CardView {
        return binding.customWallCardView
    }

}