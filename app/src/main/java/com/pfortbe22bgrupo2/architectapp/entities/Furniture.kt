package com.pfortbe22bgrupo2.architectapp.entities

import android.os.Parcel
import android.os.Parcelable

class Furniture(nombre: String?) : Parcelable{
    var nombre:String = ""

    constructor(parcel: Parcel) : this (
        parcel.readString()
    )

    init {
        this.nombre = nombre!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nombre)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Furniture> {
        override fun createFromParcel(parcel: Parcel): Furniture {
            return Furniture(parcel)
        }

        override fun newArray(size: Int): Array<Furniture?> {
            return arrayOfNulls(size)
        }
    }
}