package com.pfortbe22bgrupo2.architectapp.entities

import android.os.Parcel
import android.os.Parcelable
import androidx.compose.runtime.currentComposer

class Post (posteo:String?) : Parcelable {
    var posteo:String =""

    constructor(parcel: Parcel) : this(
        parcel.readString()
    )

    init {
        this.posteo = posteo!!
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(posteo)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post {
            return Post(parcel)
        }

        override fun newArray(size: Int): Array<Post?> {
            return arrayOfNulls(size)
        }
    }
}