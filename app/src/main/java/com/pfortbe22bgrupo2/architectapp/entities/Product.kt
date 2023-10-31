package com.pfortbe22bgrupo2.architectapp.entities

import android.os.Parcel
import android.os.Parcelable

class Product(name: String?, description: String?, price: Double?, imageUrl: Int, category: String?, stock: Int?) : Parcelable {
    var name: String = ""
    var description: String = ""
    var price: Double = 0.0
    var imageUrl: Int = 0
    var category: String = ""
    var stock: Int = 0

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble() ,
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt()
    )

    init {
        this.name = name!!
        this.description = description!!
        this.price = price!!
        this.imageUrl = imageUrl!!
        this.category = category!!
        this.stock = stock!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeDouble(price)
        parcel.writeInt(imageUrl)
        parcel.writeString(category)
        parcel.writeInt(stock)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }
}


/*
*
*
*
* */