package com.pfortbe22bgrupo2.architectapp.listener

import com.pfortbe22bgrupo2.architectapp.entities.Product

interface OnViewItemClickedListener {
    fun onViewItemDetail(product: Product)
}