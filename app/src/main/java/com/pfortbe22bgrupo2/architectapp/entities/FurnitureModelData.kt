package com.pfortbe22bgrupo2.architectapp.entities

import android.os.Parcel
import android.os.Parcelable

class FurnitureModelData(
    val imageUrl: String?,
    val allow_walls: Boolean?,
    val dimension_x: Int,
    val dimension_y: Int,
    val dimension_z: Int,
    val link: String?,
    val scala: Int,
    val name: String?,
    val description: String?,
    val category: String?
): Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    constructor() : this("",false,0,0,0,"",0,"","","")


    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(imageUrl)
    }

    companion object CREATOR : Parcelable.Creator<FurnitureModelData> {
        override fun createFromParcel(parcel: Parcel): FurnitureModelData {
            return FurnitureModelData(parcel)
        }

        override fun newArray(size: Int): Array<FurnitureModelData?> {
            return arrayOfNulls(size)
        }
    }
}