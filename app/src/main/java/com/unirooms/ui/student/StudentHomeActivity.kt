package com.bac.unirooms.ui.student

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bac.unirooms.data.model.Listing
import com.bac.unirooms.data.repository.FirebaseManager
import com.bac.unirooms.databinding.ActivityStudentHomeBinding
import com.bac.unirooms.ui.adapter.ListingAdapter
import com.bac.unirooms.utils.SessionManager

class StudentHomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentHomeBinding
    private lateinit var session: SessionManager
    private lateinit var adapter: ListingAdapter
    private var allListings: List<Listing> = emptyList()

    // Active filters
    private var filterMinPrice: Double = 0.0
    private var filterMaxPrice: Double = Double.MAX_VALUE
    private var filterLocation: String = ""
    private var filterDate: Long = Long.MAX_VALUE

    // Filter launcher — receives results back from FilterActivity
    private val filterLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            filterMinPrice = data?.getDoubleExtra("minPrice", 0.0) ?: 0.0
            filterMaxPrice = data?.getDoubleExtra("maxPrice", Double.MAX_VALUE) ?: Double.MAX_VALUE
            filterLocation = data?.getStringExtra("location") ?: ""
            filterDate = data?.getLongExtra("date", Long.MAX_VALUE) ?: Long.MAX_VALUE
            applyFilters()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        val studentId = session.getUserId()

        adapter = ListingAdapter(
            emptyList(),
            studentId,
            onItemClick = { listing ->
                val intent = Intent(this, ListingDetailActivity::class.java)
                intent.putExtra("listingId", listing.id)
                startActivity(intent)
            },
            onFavoriteClick = { listing ->
                FirebaseManager.isFavorite(studentId, listing.id) { isFav ->
                    if (isFav) {
                        FirebaseManager.removeFavorite(studentId, listing.id)
                    } else {
                        FirebaseManager.addFavorite(studentId, listing.id)
                    }
                }
            }
        )

        binding.rvListings.layoutManager = LinearLayoutManager(this)
        binding.rvListings.adapter = adapter

        FirebaseManager.getAllListings().observe(this) { listings ->
            allListings = listings
            applyFilters()
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilters()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Use filterLauncher instead of startActivity
        binding.btnFilter.setOnClickListener {
            filterLauncher.launch(Intent(this, FilterActivity::class.java))
        }

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                com.bac.unirooms.R.id.nav_home -> true
                com.bac.unirooms.R.id.nav_favorites -> {
                    startActivity(Intent(this, FavoritesActivity::class.java))
                    true
                }
                com.bac.unirooms.R.id.nav_map -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    true
                }
                com.bac.unirooms.R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun applyFilters() {
        val query = binding.etSearch.text.toString().trim().lowercase()

        val filtered = allListings.filter { listing ->
            // Search filter
            val matchesSearch = if (query.isEmpty()) true else {
                listing.title.lowercase().contains(query) ||
                        listing.location.lowercase().contains(query) ||
                        listing.type.lowercase().contains(query) ||
                        listing.amenities.lowercase().contains(query) ||
                        listing.address.lowercase().contains(query)
            }

            // Price filter
            val matchesPrice = listing.price >= filterMinPrice && listing.price <= filterMaxPrice

            // Location filter
            val matchesLocation = filterLocation.isEmpty() ||
                    listing.location.equals(filterLocation, ignoreCase = true)

            // Date filter
            val matchesDate = listing.availabilityDate <= filterDate

            matchesSearch && matchesPrice && matchesLocation && matchesDate
        }

        showListings(filtered)
    }

    private fun showListings(listings: List<Listing>) {
        adapter.updateList(listings)
        binding.tvNoListings.visibility = if (listings.isEmpty()) View.VISIBLE else View.GONE
        binding.rvListings.visibility = if (listings.isEmpty()) View.GONE else View.VISIBLE
    }
}