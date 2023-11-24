package com.pfortbe22bgrupo2.architectapp.entities

import android.os.Parcel
import android.os.Parcelable

class Product(
    var name: String,
    var description: String,
    var price: Double,
    var imageUrl: String,
    var tag: String,
    var stock: Int,
    var link: String,
    var scale: Float,
    var allowWalls: Boolean
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readDouble() ,
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readInt(),
        parcel.readString()?: "",
        parcel.readFloat(),
        parcel.readInt() > 0
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeDouble(price)
        parcel.writeString(imageUrl)
        parcel.writeString(tag)
        parcel.writeInt(stock)
        parcel.writeString(link)
        parcel.writeFloat(scale)
        parcel.writeInt(if (allowWalls) 1 else -1)
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