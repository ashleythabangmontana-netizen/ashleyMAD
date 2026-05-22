package com.bac.unirooms.data.model

data class Reservation(
    val id: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val listingId: String = "",
    val listingTitle: String = "",
    val referenceNumber: String = "",
    val amount: Double = 0.0,
    val paymentMethod: String = "",
    val status: String = "CONFIRMED",
    val timestamp: Long = System.currentTimeMillis()
)