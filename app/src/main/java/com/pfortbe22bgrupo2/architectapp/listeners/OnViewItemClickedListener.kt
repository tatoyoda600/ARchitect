package com.pfortbe22bgrupo2.architectapp.listeners

import com.pfortbe22bgrupo2.architectapp.entities.Product

interface OnViewItemClickedListener {
    fun onViewItemDetail(product: Product)
}