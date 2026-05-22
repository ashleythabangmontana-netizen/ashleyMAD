package com.bac.unirooms.ui.student

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bac.unirooms.data.model.Listing
import com.bac.unirooms.data.repository.FirebaseManager
import com.bac.unirooms.databinding.ActivityListingDetailBinding
import com.bac.unirooms.utils.ReceiptGenerator
import com.bac.unirooms.utils.SessionManager

class ListingDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListingDetailBinding
    private lateinit var session: SessionManager
    private var listingId: String = ""
    private var currentListing: Listing? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListingDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        listingId = intent.getStringExtra("listingId") ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        loadListing()

        binding.btnReserve.setOnClickListener {
            currentListing?.let { listing ->
                if (listing.status == "RESERVED") {
                    Toast.makeText(this, "This room is already reserved", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val intent = Intent(this, PaymentActivity::class.java)
                intent.putExtra("listingId", listing.id)
                intent.putExtra("listingTitle", listing.title)
                intent.putExtra("listingAddress", listing.address)
                intent.putExtra("depositAmount", listing.depositAmount)
                intent.putExtra("providerId", listing.providerId)
                startActivity(intent)
            }
        }

        binding.btnFavorite.setOnClickListener {
            toggleFavorite()
        }

        binding.btnViewMap.setOnClickListener {
            currentListing?.let { listing ->
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra("listingLat", listing.latitude)
                intent.putExtra("listingLng", listing.longitude)
                intent.putExtra("listingId", listing.id)
                startActivity(intent)
            }
        }

        binding.btnChat.setOnClickListener {
            currentListing?.let { listing ->
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("otherUserId", listing.providerId)
                intent.putExtra("listingId", listing.id)
                intent.putExtra("otherUserName", listing.providerName.ifEmpty { "Landlord" })
                startActivity(intent)
            }
        }
    }

    private fun loadListing() {
        FirebaseManager.getListingById(listingId) { listing ->
            runOnUiThread {
                if (listing == null) {
                    Toast.makeText(this, "Listing not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@runOnUiThread
                }
                currentListing = listing

                binding.tvDetailTitle.text = listing.title
                binding.tvDetailLocation.text = "${listing.location} - ${listing.address}"
                binding.tvDetailPrice.text = "BWP ${String.format("%.0f", listing.price)}"
                binding.tvDetailDeposit.text = "BWP ${String.format("%.0f", listing.depositAmount)}"
                binding.tvDetailDescription.text = listing.description
                binding.tvDetailType.text = listing.type
                binding.tvDetailSharing.text = if (listing.sharingAllowed) "Allowed" else "Not Allowed"
                binding.tvDetailAvailability.text = ReceiptGenerator.formatDateShort(listing.availabilityDate)
                binding.tvDetailAmenities.text = listing.amenities.replace(",", "\n")

                val isReserved = listing.status == "RESERVED"
                binding.tvDetailStatus.text = if (isReserved) "Reserved" else "Available"
                if (isReserved) {
                    binding.tvDetailStatus.setBackgroundResource(com.bac.unirooms.R.drawable.bg_status_reserved)
                    binding.tvDetailStatus.setTextColor(getColor(com.bac.unirooms.R.color.colorReserved))
                    binding.btnReserve.isEnabled = false
                    binding.btnReserve.text = "Reserved"
                }

                checkFavoriteStatus()
            }
        }
    }

    private fun checkFavoriteStatus() {
        FirebaseManager.isFavorite(session.getUserId(), listingId) { isFav ->
            runOnUiThread {
                binding.btnFavorite.text = if (isFav) "Saved" else "Save"
            }
        }
    }

    private fun toggleFavorite() {
        FirebaseManager.isFavorite(session.getUserId(), listingId) { isFav ->
            runOnUiThread {
                if (isFav) {
                    FirebaseManager.removeFavorite(session.getUserId(), listingId)
                    binding.btnFavorite.text = "Save"
                    Toast.makeText(this, "Removed from saved listings", Toast.LENGTH_SHORT).show()
                } else {
                    FirebaseManager.addFavorite(session.getUserId(), listingId)
                    binding.btnFavorite.text = "Saved"
                    Toast.makeText(this, "Added to saved listings", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}