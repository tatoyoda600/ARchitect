package com.pfortbe22bgrupo2.architectapp.holders

import android.view.View
import android.widget.ImageView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.R

class ProductHolder(v: View) : RecyclerView.ViewHolder(v) {

    private var view: View

    init {
        this.view = v
    }

    fun getCardLayout (): CardView {
        return view.findViewById(R.id.cardItem)
    }
    //to do: cambiar implementación cuando se traiga el producto de la base
    fun setImageUrl(url: Int){
        val image: ImageView = view.findViewById(R.id.itemImageViewId)
        image.setImageResource(url)
    }

}