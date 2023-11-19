package com.pfortbe22bgrupo2.architectapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.databinding.FurnitureItemBinding
import com.pfortbe22bgrupo2.architectapp.entities.FurnitureModelData
import com.pfortbe22bgrupo2.architectapp.holders.FurnitureHolder
import com.pfortbe22bgrupo2.architectapp.listeners.ShowDetailsFurniture

class FurnitureAdapter(private var furnitureList: MutableList<FurnitureModelData>,
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
        var image = furnitureList[position].imageUrl
        if(image == null) image = ""
        holder.setImage(image,binding.root)
        holder.getCardLayout().setOnClickListener() {
            showDetailsFurniture.showDetails(furnitureList[position])
        }
    }

    fun updatesFurnitures(furnitureList: List<FurnitureModelData>) {
        this.furnitureList = furnitureList.toMutableList()
        notifyDataSetChanged()
    }

}