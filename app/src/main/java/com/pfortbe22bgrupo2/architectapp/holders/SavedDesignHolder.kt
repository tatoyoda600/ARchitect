package com.pfortbe22bgrupo2.architectapp.holders

import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pfortbe22bgrupo2.architectapp.databinding.SavedDesignItemBinding

class SavedDesignHolder(binding: SavedDesignItemBinding): RecyclerView.ViewHolder(binding.root) {
    private var binding: SavedDesignItemBinding

    init {
        this.binding = binding
    }

    fun setName(name:String) {
        binding.savedDesignItemTextView.text = name
    }

/*    fun setImage(imageUrl:String, context: View){
        Glide.with(context).load(imageUrl).into(binding.itemSavedDesignImageView)
    }*/

    fun getDeleteButtom(): FloatingActionButton{
        return binding.deleteFloatingActionButton

    }

    fun getCardLayout(): CardView {
        return binding.savedDesignCardView
    }


}