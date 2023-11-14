package com.pfortbe22bgrupo2.architectapp.models

import android.os.Parcel
import android.os.Parcelable

class CustomWall(description: String?): Parcelable {
    var id: String = ""
    var description: String = ""
    var image: String = ""


    constructor() : this("")
    constructor(parcel: Parcel) : this(
        parcel.readString()
    )

    init {
        this.description = description!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CustomWall> {
        override fun createFromParcel(parcel: Parcel): CustomWall {
            return CustomWall(parcel)
        }

        override fun newArray(size: Int): Array<CustomWall?> {
            return arrayOfNulls(size)
        }
    }
}