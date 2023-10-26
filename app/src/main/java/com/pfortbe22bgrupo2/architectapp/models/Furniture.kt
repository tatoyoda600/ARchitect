package com.pfortbe22bgrupo2.architectapp.models

import android.os.Parcel
import android.os.Parcelable

class Furniture(name: String?, category: String?, urlImage:Int?): Parcelable{
    var nombre:String = ""
    var category:String = ""
    var urlImage:Int = 0

    constructor(parcel: Parcel): this (
        parcel.readString(),
        parcel.readString(),
        parcel.readInt()
    )

    init {
        this.nombre = name!!
        this.category = category!!
        this.urlImage = urlImage!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nombre)
        parcel.writeString(category)
        parcel.writeInt(urlImage)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR: Parcelable.Creator<Furniture> {
        override fun createFromParcel(parcel: Parcel): Furniture {
            return Furniture(parcel)
        }

        override fun newArray(size: Int): Array<Furniture?> {
            return arrayOfNulls(size)
        }
    }
}