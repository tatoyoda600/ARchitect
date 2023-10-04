package com.pfortbe22bgrupo2.architectapp.holders

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.databinding.FurnitureItemBinding

class FurnitureHolder(binding: FurnitureItemBinding): RecyclerView.ViewHolder(binding.root) {
    private var binding: FurnitureItemBinding
    private var view: View

    init {
        this.binding = binding
        this.view = binding.root
    }

    fun setNombre(nombre:String) {
        binding.furnitureNameTextView.text = nombre
    }

    fun getCardLayout(): CardView {
        return binding.furnitureCardView
    }
}