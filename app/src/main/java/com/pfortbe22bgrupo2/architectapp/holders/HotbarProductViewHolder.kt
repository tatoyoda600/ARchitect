package com.pfortbe22bgrupo2.architectapp.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pfortbe22bgrupo2.architectapp.databinding.HotbarItemBinding

class HotbarProductViewHolder(
    val binding: HotbarItemBinding
): RecyclerView.ViewHolder(binding.root) {
    fun setImage(url: String) {
        Glide.with(binding.root).load(url).into(binding.itemImg)
    }

    fun setOnClickListener(onClickListener: (View) -> Unit) {
        binding.itemImg.setOnClickListener(onClickListener)
    }
}