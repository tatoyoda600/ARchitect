package com.pfortbe22bgrupo2.architectapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.databinding.HotbarItemBinding
import com.pfortbe22bgrupo2.architectapp.entities.Product
import com.pfortbe22bgrupo2.architectapp.holders.HotbarProductViewHolder

class ProductHotbarAdapter(
    val productList: List<Product>,
    val onClickListener: (Product) -> Unit
): RecyclerView.Adapter<HotbarProductViewHolder>() {
    override fun getItemCount() = productList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotbarProductViewHolder {
        val binding = HotbarItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HotbarProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HotbarProductViewHolder, position: Int) {
        val product = productList[position]
        holder.setImage(product.imageUrl)
        holder.setOnClickListener {
            onClickListener(product)
        }
    }
}