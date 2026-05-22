package com.bac.unirooms.data.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val listingId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)