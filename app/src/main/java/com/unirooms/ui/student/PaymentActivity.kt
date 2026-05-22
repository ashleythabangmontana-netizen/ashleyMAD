package com.bac.unirooms.ui.student

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bac.unirooms.data.model.Reservation
import com.bac.unirooms.data.repository.FirebaseManager
import com.bac.unirooms.databinding.ActivityPaymentBinding
import com.bac.unirooms.utils.ReceiptGenerator
import com.bac.unirooms.utils.SessionManager

class PaymentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentBinding
    private lateinit var session: SessionManager
    private var listingId: String = ""
    private var depositAmount: Double = 0.0
    private var listingTitle: String = ""
    private var listingAddress: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        listingId = intent.getStringExtra("listingId") ?: ""
        listingTitle = intent.getStringExtra("listingTitle") ?: ""
        listingAddress = intent.getStringExtra("listingAddress") ?: ""
        depositAmount = intent.getDoubleExtra("depositAmount", 0.0)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.tvPaymentListingTitle.text = listingTitle
        binding.tvPaymentLocation.text = listingAddress
        binding.tvPaymentAmount.text = "BWP ${String.format("%.2f", depositAmount)}"

        binding.btnConfirmPayment.setOnClickListener {
            processPayment()
        }
    }

    private fun processPayment() {
        val method = getSelectedPaymentMethod()
        if (method.isEmpty()) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnConfirmPayment.isEnabled = false
        binding.btnConfirmPayment.text = "Processing..."

        val refNumber = ReceiptGenerator.generateReferenceNumber()
        val reservation = Reservation(
            studentId = session.getUserId(),
            studentName = session.getUserName(),
            listingId = listingId,
            listingTitle = listingTitle,
            referenceNumber = refNumber,
            amount = depositAmount,
            paymentMethod = method
        )

        FirebaseManager.makeReservation(reservation) { success, result ->
            runOnUiThread {
                binding.btnConfirmPayment.isEnabled = true
                binding.btnConfirmPayment.text = "Confirm Payment"
                if (success) {
                    val intent = Intent(this, ReceiptActivity::class.java)
                    intent.putExtra("refNumber", refNumber)
                    intent.putExtra("listingTitle", listingTitle)
                    intent.putExtra("amount", depositAmount)
                    intent.putExtra("paymentMethod", method)
                    startActivity(intent)
                    finishAffinity()
                } else {
                    if (result == "ALREADY_RESERVED") {
                        Toast.makeText(this, "Sorry, this room was just reserved by someone else.", Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Payment failed. Please try again.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    private fun getSelectedPaymentMethod(): String {
        return when (binding.rgPaymentMethod.checkedRadioButtonId) {
            com.bac.unirooms.R.id.rbOrangeMoney -> "Orange Money"
            com.bac.unirooms.R.id.rbMyZaka -> "MyZaka"
            com.bac.unirooms.R.id.rbBankTransfer -> "Bank Transfer"
            com.bac.unirooms.R.id.rbCash -> "Cash Deposit"
            else -> ""
        }
    }
}