package home.service.appmanage.online.work.models

data class Booking(
    val bookId: String, val areaLat: String, val areaLong: String,
    val bookedAt: String, val fare: String, val currency: String, val type: String,
    val subType: String, val workedType: String
)