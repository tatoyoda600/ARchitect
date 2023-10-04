package com.pfortbe22bgrupo2.architectapp.holders

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.databinding.FurnitureItemBinding
import com.pfortbe22bgrupo2.architectapp.databinding.PostItemBinding

class FurnitureHolder(v :View) : RecyclerView.ViewHolder(v) {

    private val binding = FurnitureItemBinding.bind(v)

    fun setNombre(nombre:String){
        binding.furnitureNameTextView.text = nombre
    }

    fun getCardLayout () : CardView {
        return binding.furnitureCardView
    }
}