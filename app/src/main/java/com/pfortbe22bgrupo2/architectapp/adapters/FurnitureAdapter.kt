package com.pfortbe22bgrupo2.architectapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.databinding.FurnitureItemBinding
import com.pfortbe22bgrupo2.architectapp.entities.Furniture
import com.pfortbe22bgrupo2.architectapp.holders.FurnitureHolder
import com.pfortbe22bgrupo2.architectapp.listeners.ShowDetailsFurniture

class FurnitureAdapter(
    private var furnitureList: MutableList<Furniture>,
    private val showDetailsFurniture: ShowDetailsFurniture
): RecyclerView.Adapter<FurnitureHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FurnitureHolder {
        //val view = LayoutInflater.from(parent.context).inflate(R.layout.furniture_item,parent,false)
        val binding = FurnitureItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return (FurnitureHolder(binding))
    }

    override fun getItemCount(): Int {
        return furnitureList.size
    }

    override fun onBindViewHolder(holder: FurnitureHolder, position: Int) {
        holder.setNombre(furnitureList[position].nombre)

        holder.getCardLayout().setOnClickListener() {
            showDetailsFurniture.showDetails(furnitureList[position])
        }
    }

    fun updatesFurnitures(furnitureList: List<Furniture>) {
        this.furnitureList = furnitureList.toMutableList()
        notifyDataSetChanged()
    }
}