package com.pfortbe22bgrupo2.architectapp.holders

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.R

class FurnitureHolder(v :View) : RecyclerView.ViewHolder(v) {
    private var view = v

    init {
        this.view = v
    }

    fun setNombre(nombre:String){
        this.view.findViewById<TextView>(R.id.furniture_name_textView).text = nombre
    }

    fun getCardLayout () : CardView {
        return view.findViewById(R.id.furniture_cardView)
    }
}