package com.bac.unirooms.data.model

data class User(
    val id: String = "",
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val phone: String = "",
    val role: String = "",
    val registeredDate: Long = System.currentTimeMillis()
)