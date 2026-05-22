package com.bac.unirooms.ui.student

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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
            val query = binding.etSearch.text.toString().trim()
            if (query.isEmpty()) {
                showListings(listings)
            }
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim().lowercase()
                if (query.isEmpty()) {
                    showListings(allListings)
                } else {
                    val filtered = allListings.filter { listing ->
                        listing.title.lowercase().contains(query) ||
                                listing.location.lowercase().contains(query) ||
                                listing.type.lowercase().contains(query) ||
                                listing.amenities.lowercase().contains(query) ||
                                listing.address.lowercase().contains(query)
                    }
                    showListings(filtered)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnFilter.setOnClickListener {
            startActivity(Intent(this, FilterActivity::class.java))
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

    private fun showListings(listings: List<Listing>) {
        adapter.updateList(listings)
        binding.tvNoListings.visibility = if (listings.isEmpty()) View.VISIBLE else View.GONE
        binding.rvListings.visibility = if (listings.isEmpty()) View.GONE else View.VISIBLE
    }
}