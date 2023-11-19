package com.pfortbe22bgrupo2.architectapp.models

import android.os.Parcel
import android.os.Parcelable

class Furniture(name: String?, category: String?, urlImage:Int?, description: String?, image_url:String?): Parcelable{
    var name:String = ""
    var category:String = ""
    var urlImage:Int = 0
    var description:String = ""
    var image_url: String = ""

    constructor() : this("", "", 0, "","")


    constructor(parcel: Parcel): this (
        parcel.readString(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString()
    )

    init {
        this.name = name!!
        this.category = category!!
        this.urlImage = urlImage!!
        this.description = description!!
        this.image_url = image_url!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(category)
        parcel.writeInt(urlImage)
        parcel.writeString(description)
        //parcel.writeString(image_url)
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