package com.pfortbe22bgrupo2.architectapp.models

import android.os.Parcel
import android.os.Parcelable

class SavedDesign(description: String?): Parcelable {
    var description: String = ""


    constructor(parcel: Parcel) : this(
        parcel.readString()
    )
    constructor() : this("")

    init {
        this.description = description!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SavedDesign> {
        override fun createFromParcel(parcel: Parcel): SavedDesign {
            return SavedDesign(parcel)
        }

        override fun newArray(size: Int): Array<SavedDesign?> {
            return arrayOfNulls(size)
        }
    }


}