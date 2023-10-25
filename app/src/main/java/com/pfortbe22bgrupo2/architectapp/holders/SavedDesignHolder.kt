package com.pfortbe22bgrupo2.architectapp.holders

import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.databinding.SavedDesignItemBinding

class SavedDesignHolder(binding: SavedDesignItemBinding): RecyclerView.ViewHolder(binding.root) {
    private var binding: SavedDesignItemBinding

    init {
        this.binding = binding
    }

    fun setDescription(description:String) {
        binding.savedDesignItemTextView.text = description
    }

    fun getCardLayout(): CardView {
        return binding.savedDesignCardView
    }


}