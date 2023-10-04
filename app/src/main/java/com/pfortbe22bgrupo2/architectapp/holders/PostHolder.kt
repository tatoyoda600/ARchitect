package com.pfortbe22bgrupo2.architectapp.holders

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.databinding.FurnitureItemBinding
import com.pfortbe22bgrupo2.architectapp.databinding.PostItemBinding

class PostHolder(binding: PostItemBinding): RecyclerView.ViewHolder(binding.root) {
    private var binding: PostItemBinding
    private var view: View

    init {
        this.binding = binding
        this.view = binding.root
    }
    fun setPosteo(posteo:String) {
        binding.postTextView.text = posteo
    }
    fun getCardLayout(): CardView {
        return binding.postCardView
    }


}