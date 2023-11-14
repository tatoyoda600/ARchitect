package com.pfortbe22bgrupo2.architectapp.holders

import android.view.View
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pfortbe22bgrupo2.architectapp.databinding.CustomWallItemBinding

class CustomWallHolder(binding: CustomWallItemBinding): RecyclerView.ViewHolder(binding.root) {
    private var binding: CustomWallItemBinding

    init {
        this.binding = binding
    }

    fun setDescription(description: String){
        //binding.customWallItemTextView.text = description
    }

    fun setImage(imageUrl:String, context: View){
        Glide.with(context).load(imageUrl).into(binding.userCustomWallImageView)
    }

    fun getDeleteButtom(): FloatingActionButton {
        return binding.deleteWallFloatingActionButton

    }

    fun getCardLayout(): CardView {
        return binding.customWallCardView
    }

}