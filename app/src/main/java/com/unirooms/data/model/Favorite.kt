package com.bac.unirooms.data.model

data class Favorite(
    val studentId: String = "",
    val listingId: String = "",
    val savedAt: Long = System.currentTimeMillis()
)