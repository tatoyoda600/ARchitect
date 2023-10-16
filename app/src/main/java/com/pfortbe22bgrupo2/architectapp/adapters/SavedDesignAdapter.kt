package com.pfortbe22bgrupo2.architectapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.databinding.FurnitureItemBinding
import com.pfortbe22bgrupo2.architectapp.databinding.SavedDesignItemBinding
import com.pfortbe22bgrupo2.architectapp.holders.FurnitureHolder
import com.pfortbe22bgrupo2.architectapp.holders.SavedDesignHolder
import com.pfortbe22bgrupo2.architectapp.models.SavedDesign

class SavedDesignAdapter(
    private var savedDesignList: MutableList<SavedDesign>
): RecyclerView.Adapter<SavedDesignHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedDesignHolder {
        val binding = SavedDesignItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return (SavedDesignHolder(binding))
    }

    override fun getItemCount(): Int = savedDesignList.size


    override fun onBindViewHolder(holder: SavedDesignHolder, position: Int) {
        holder.setDescription(savedDesignList[position].description)


    }
}