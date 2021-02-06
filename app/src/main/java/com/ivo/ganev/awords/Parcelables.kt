package com.ivo.ganev.awords

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable


class EditorFragmentArguments(val fileHandlerAction: Int, val fileUri: Uri?) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readParcelable(Uri::class.java.classLoader)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(fileHandlerAction)
        parcel.writeParcelable(fileUri, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<EditorFragmentArguments> {
        override fun createFromParcel(parcel: Parcel): EditorFragmentArguments {
            return EditorFragmentArguments(parcel)
        }

        override fun newArray(size: Int): Array<EditorFragmentArguments?> {
            return arrayOfNulls(size)
        }
    }

    override fun toString(): String {
        return "action: ${FileHandler.Action.values()[fileHandlerAction]}, \n uri: $fileUri"
    }
}