package com.bac.unirooms.ui.student

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bac.unirooms.databinding.ActivityReceiptBinding
import com.bac.unirooms.utils.NotificationHelper
import com.bac.unirooms.utils.ReceiptGenerator

class ReceiptActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReceiptBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceiptBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val refNumber = intent.getStringExtra("refNumber") ?: ""
        val listingTitle = intent.getStringExtra("listingTitle") ?: "Room"
        val amount = intent.getDoubleExtra("amount", 0.0)
        val paymentMethod = intent.getStringExtra("paymentMethod") ?: ""
        val now = System.currentTimeMillis()

        binding.tvReceiptRef.text = refNumber
        binding.tvReceiptListing.text = listingTitle
        binding.tvReceiptAmount.text = "BWP ${String.format("%.2f", amount)}"
        binding.tvReceiptMethod.text = paymentMethod
        binding.tvReceiptDate.text = ReceiptGenerator.formatDate(now)

        val notifHelper = NotificationHelper(this)
        notifHelper.sendReservationConfirmationNotification(listingTitle, refNumber)

        binding.btnShareReceipt.setOnClickListener {
            val shareText = """
                UniRooms Booking Receipt
                Reference: $refNumber
                Listing: $listingTitle
                Amount: BWP ${String.format("%.2f", amount)}
                Payment: $paymentMethod
                Date: ${ReceiptGenerator.formatDate(now)}
            """.trimIndent()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
                putExtra(Intent.EXTRA_SUBJECT, "UniRooms Receipt - $refNumber")
            }
            startActivity(Intent.createChooser(shareIntent, "Share Receipt"))
        }

        binding.btnGoHome.setOnClickListener {
            startActivity(Intent(this, StudentHomeActivity::class.java))
            finishAffinity()
        }
    }
}