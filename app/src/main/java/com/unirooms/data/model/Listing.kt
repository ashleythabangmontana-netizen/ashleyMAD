package com.bac.unirooms.data.model

data class Listing(
    val id: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val title: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val depositAmount: Double = 0.0,
    val location: String = "",
    val address: String = "",
    val type: String = "",
    val amenities: String = "",
    val availabilityDate: Long = 0L,
    val latitude: Double = -24.6561,
    val longitude: Double = 25.9144,
    val sharingAllowed: Boolean = false,
    val status: String = "AVAILABLE",
    val postedDate: Long = System.currentTimeMillis(),
    val photoPath: String = ""
)