package com.pfortbe22bgrupo2.architectapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.entities.Product
import com.pfortbe22bgrupo2.architectapp.holders.ProductHolder
import com.pfortbe22bgrupo2.architectapp.listener.OnViewItemClickedListener

class ProductListAdapter (
    private val productList: MutableList<Product>,
    private val onItemClick: OnViewItemClickedListener
) : RecyclerView.Adapter<ProductHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductHolder {
        val view =  LayoutInflater.from(parent.context).inflate(R.layout.item_product,parent,false)
        return (ProductHolder(view))
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun onBindViewHolder(holder: ProductHolder, position: Int) {
        val product = productList[position]
        holder.setImageUrl(product.imageUrl)
        holder.getCardLayout().setOnClickListener{
            onItemClick.onViewItemDetail(product)
        }
    }
}
