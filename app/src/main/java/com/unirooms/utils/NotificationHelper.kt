package com.bac.unirooms.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bac.unirooms.R
import com.bac.unirooms.ui.student.ListingDetailActivity

class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "unirooms_alerts"
        const val CHANNEL_NAME = "UniRooms Alerts"
        const val CHANNEL_DESC = "Notifications for new matching listings and updates"
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun sendListingMatchNotification(listingTitle: String, listingPrice: Double, listingId: Int) {
        val intent = Intent(context, ListingDetailActivity::class.java).apply {
            putExtra("listingId", listingId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, listingId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("New Listing Matches Your Preferences")
            .setContentText("$listingTitle - BWP ${String.format("%.0f", listingPrice)}/month is now available")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("A new listing matching your saved preferences is available: $listingTitle at BWP ${String.format("%.0f", listingPrice)} per month.")
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(listingId, notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun sendReservationConfirmationNotification(listingTitle: String, refNumber: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Reservation Confirmed")
            .setContentText("Your booking for $listingTitle has been confirmed.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your reservation for $listingTitle is confirmed. Reference: $refNumber. Keep this reference for your records.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(refNumber.hashCode(), notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}