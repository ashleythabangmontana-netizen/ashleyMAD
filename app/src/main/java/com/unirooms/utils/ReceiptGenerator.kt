package com.bac.unirooms.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object ReceiptGenerator {

    fun generateReferenceNumber(): String {
        val uuid = UUID.randomUUID().toString().replace("-", "").uppercase()
        return "UR${uuid.substring(0, 8)}"
    }

    fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun generateReceiptText(
        refNumber: String,
        studentName: String,
        listingTitle: String,
        listingAddress: String,
        amount: Double,
        paymentMethod: String,
        timestamp: Long
    ): String {
        return """
            UNIROOMS PAYMENT RECEIPT
            ========================
            Reference: $refNumber
            Date: ${formatDate(timestamp)}
            
            Student: $studentName
            Listing: $listingTitle
            Address: $listingAddress
            
            Payment Method: $paymentMethod
            Amount Paid: BWP ${String.format("%.2f", amount)}
            Status: CONFIRMED
            
            Room Status: RESERVED
            
            This serves as official confirmation of your
            deposit payment. Please retain this reference
            number for your records.
            
            UniRooms - Student Accommodation Made Simple
        """.trimIndent()
    }
}