package com.pfortbe22bgrupo2.architectapp.entities

import android.os.Parcel
import android.os.Parcelable

class FurnitureModelData(
    val furnitureType: String,
    val id: String,
    val imageUrl: String,
    val allow_walls: Boolean,
    val dimension_x: Int,
    val dimension_y: Int,
    val dimension_z: Int,
    val link: String,
    val scale: Float,
    val name: String,
    val description: String,
    val category: String
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()?: "",
        parcel.readFloat(),
        parcel.readString()?: "",
        parcel.readString()?: "",
        parcel.readString()?: ""
    )

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(furnitureType)
        parcel.writeString(id)
        parcel.writeString(imageUrl)
        parcel.writeByte(if (allow_walls) 1.toByte() else 0.toByte())
        parcel.writeInt(dimension_x)
        parcel.writeInt(dimension_y)
        parcel.writeInt(dimension_z)
        parcel.writeString(link)
        parcel.writeFloat(scale)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeString(category)
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