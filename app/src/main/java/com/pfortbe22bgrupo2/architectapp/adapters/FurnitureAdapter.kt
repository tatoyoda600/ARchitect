package com.pfortbe22bgrupo2.architectapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.pfortbe22bgrupo2.architectapp.databinding.FurnitureItemBinding
import com.pfortbe22bgrupo2.architectapp.models.Furniture
import com.pfortbe22bgrupo2.architectapp.holders.FurnitureHolder
import com.pfortbe22bgrupo2.architectapp.listeners.ShowDetailsFurniture

class FurnitureAdapter(private var context : Context,
                       private var furnitureList: MutableList<Furniture>,
                       private val showDetailsFurniture: ShowDetailsFurniture
): RecyclerView.Adapter<FurnitureHolder>() {

    private lateinit var binding: FurnitureItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FurnitureHolder {
        //val view = LayoutInflater.from(parent.context).inflate(R.layout.furniture_item,parent,false)
        binding = FurnitureItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return (FurnitureHolder(binding))
    }

    override fun getItemCount(): Int = furnitureList.size


    override fun onBindViewHolder(holder: FurnitureHolder, position: Int) {
        //holder.setName(furnitureList[position].nombre)
        Glide.with(context).load(furnitureList[position].urlImage).into(binding.itemImageViewId)
        //holder.setImageUrl(furnitureList[position].urlImage)
        holder.getCardLayout().setOnClickListener() {
            showDetailsFurniture.showDetails(furnitureList[position])
        }
    }

    fun updatesFurnitures(furnitureList: List<Furniture>) {
        this.furnitureList = furnitureList.toMutableList()
        notifyDataSetChanged()
    }
}