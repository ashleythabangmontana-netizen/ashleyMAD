package com.bac.unirooms.ui.student

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bac.unirooms.data.model.Listing
import com.bac.unirooms.data.repository.FirebaseManager
import com.bac.unirooms.databinding.ActivityFavoritesBinding
import com.bac.unirooms.ui.adapter.ListingAdapter
import com.bac.unirooms.utils.SessionManager

class FavoritesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var session: SessionManager
    private lateinit var adapter: ListingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        val studentId = session.getUserId()

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = ListingAdapter(
            emptyList(),
            studentId,
            onItemClick = { listing ->
                val intent = Intent(this, ListingDetailActivity::class.java)
                intent.putExtra("listingId", listing.id)
                startActivity(intent)
            },
            onFavoriteClick = { listing ->
                FirebaseManager.removeFavorite(studentId, listing.id)
            }
        )

        binding.rvFavorites.layoutManager = LinearLayoutManager(this)
        binding.rvFavorites.adapter = adapter

        FirebaseManager.getFavoriteListings(studentId).observe(this) { listings ->
            adapter.updateList(listings)
            binding.tvNoFavorites.visibility = if (listings.isEmpty()) View.VISIBLE else View.GONE
            binding.rvFavorites.visibility = if (listings.isEmpty()) View.GONE else View.VISIBLE
        }
    }
}