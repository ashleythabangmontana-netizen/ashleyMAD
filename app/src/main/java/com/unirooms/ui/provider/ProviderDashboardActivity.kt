package com.bac.unirooms.ui.provider

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bac.unirooms.data.model.Listing
import com.bac.unirooms.data.repository.FirebaseManager
import com.bac.unirooms.databinding.ActivityProviderDashboardBinding
import com.bac.unirooms.ui.auth.RoleSelectionActivity
import com.bac.unirooms.utils.SessionManager

class ProviderDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderDashboardBinding
    private lateinit var session: SessionManager
    private lateinit var adapter: ProviderListingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        val providerId = session.getUserId()

        setSupportActionBar(binding.toolbar)

        adapter = ProviderListingAdapter(
            emptyList(),
            onEdit = { listing ->
                val intent = Intent(this, AddEditListingActivity::class.java)
                intent.putExtra("listingId", listing.id)
                startActivity(intent)
            },
            onDelete = { listing ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Listing")
                    .setMessage("Are you sure you want to delete this listing?")
                    .setPositiveButton("Delete") { _, _ ->
                        FirebaseManager.deleteListing(listing.id) {}
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.rvProviderListings.layoutManager = LinearLayoutManager(this)
        binding.rvProviderListings.adapter = adapter

        FirebaseManager.getListingsByProvider(providerId).observe(this) { listings ->
            adapter.updateList(listings)
            binding.tvTotalListings.text = listings.size.toString()
            binding.tvAvailableListings.text = listings.count { it.status == "AVAILABLE" }.toString()
            binding.tvReservedListings.text = listings.count { it.status == "RESERVED" }.toString()
            binding.tvNoProviderListings.visibility = if (listings.isEmpty()) View.VISIBLE else View.GONE
            binding.rvProviderListings.visibility = if (listings.isEmpty()) View.GONE else View.VISIBLE
        }

        binding.btnAddListing.setOnClickListener {
            startActivity(Intent(this, AddEditListingActivity::class.java))
        }

        binding.btnProviderChat.setOnClickListener {
            startActivity(Intent(this, ProviderChatListActivity::class.java))
        }

        binding.btnProviderLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout") { _, _ ->
                    session.clearSession()
                    startActivity(Intent(this, RoleSelectionActivity::class.java))
                    finishAffinity()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
}