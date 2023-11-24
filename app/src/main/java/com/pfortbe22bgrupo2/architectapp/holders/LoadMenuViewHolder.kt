package com.pfortbe22bgrupo2.architectapp.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.pfortbe22bgrupo2.architectapp.databinding.LoadDesignItemBinding
import com.pfortbe22bgrupo2.architectapp.databinding.LoadFloorItemBinding

class LoadMenuViewHolder(
    val binding: ViewBinding
): RecyclerView.ViewHolder(binding.root) {
    fun setText(text: String) {
        if (binding is LoadFloorItemBinding) {
            binding.floorItemName.text = text
        }
        else {
            if (binding is LoadDesignItemBinding) {
                binding.designItemName.text = text
            }
        }
    }

    fun setOnClickListener(onClickListener: (View) -> Unit) {
        if (binding is LoadFloorItemBinding) {
            binding.floorItem.setOnClickListener(onClickListener)
        }
        else {
            if (binding is LoadDesignItemBinding) {
                binding.designItem.setOnClickListener(onClickListener)
            }
        }
    }
}