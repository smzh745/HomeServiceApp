package home.service.appmanage.online.work.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TypeDetails(
    val subId: Int,
    val typeTitle: String,
    val typeDesp: String,
    val fare: String,
    val type: String, val currency: String
) : Parcelable