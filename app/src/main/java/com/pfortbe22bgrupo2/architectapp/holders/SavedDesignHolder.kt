package com.pfortbe22bgrupo2.architectapp.holders

import android.view.View
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pfortbe22bgrupo2.architectapp.databinding.SavedDesignItemBinding

class SavedDesignHolder(binding: SavedDesignItemBinding): RecyclerView.ViewHolder(binding.root) {
    private var binding: SavedDesignItemBinding

    init {
        this.binding = binding
    }

    fun setDescription(description:String) {
        //binding.savedDesignItemTextView.text = description
    }

    fun setImage(imageUrl:String, context: View){
        Glide.with(context).load(imageUrl).into(binding.itemSavedDesignImageView)
    }

    fun getDeleteButtom(): FloatingActionButton{
        return binding.deleteFloatingActionButton

    }

    fun getCardLayout(): CardView {
        return binding.savedDesignCardView
    }


}