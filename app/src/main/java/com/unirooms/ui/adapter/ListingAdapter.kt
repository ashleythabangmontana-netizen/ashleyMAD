package com.bac.unirooms.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bac.unirooms.R
import com.bac.unirooms.data.model.Listing
import com.bac.unirooms.databinding.ItemListingBinding
import com.bac.unirooms.utils.ReceiptGenerator
import com.bumptech.glide.Glide

class ListingAdapter(
    private var listings: List<Listing>,
    private val studentId: String,
    private val onItemClick: (Listing) -> Unit,
    private val onFavoriteClick: (Listing) -> Unit
) : RecyclerView.Adapter<ListingAdapter.ListingViewHolder>() {

    inner class ListingViewHolder(val binding: ItemListingBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val binding = ItemListingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        val listing = listings[position]
        val context = holder.itemView.context

        holder.binding.tvTitle.text = listing.title
        holder.binding.tvLocation.text = listing.location
        holder.binding.tvType.text = listing.type
        holder.binding.tvPrice.text = "BWP ${String.format("%.0f", listing.price)}/month"
        holder.binding.tvAvailability.text =
            "Available: ${ReceiptGenerator.formatDateShort(listing.availabilityDate)}"
        holder.binding.tvSharing.text =
            if (listing.sharingAllowed) "Sharing OK" else "No Sharing"
        holder.binding.tvSharing.setTextColor(
            if (listing.sharingAllowed) context.getColor(R.color.colorSuccess)
            else context.getColor(R.color.colorTextSecondary)
        )

        // ── Status badge (Reserved / Available) ──────────────────
        val isReserved = listing.status == "RESERVED"
        holder.binding.tvStatus.text = if (isReserved) "Reserved" else "Available"
        holder.binding.tvStatus.setBackgroundResource(
            if (isReserved) R.drawable.bg_status_reserved else R.drawable.bg_status_available
        )
        holder.binding.tvStatus.setTextColor(
            if (isReserved) context.getColor(R.color.colorReserved)
            else context.getColor(R.color.colorAvailable)
        )

        // ── Load listing image from Firebase Storage via Glide ────
        if (listing.photoPath.isNotEmpty()) {
            Glide.with(context)
                .load(listing.photoPath)
                .placeholder(R.drawable.ic_house_placeholder)
                .error(R.drawable.ic_house_placeholder)
                .centerCrop()
                .into(holder.binding.ivListingImage)
        } else {
            holder.binding.ivListingImage.setImageResource(R.drawable.ic_house_placeholder)
        }

        holder.itemView.setOnClickListener { onItemClick(listing) }
        holder.binding.ivFavorite.setOnClickListener { onFavoriteClick(listing) }
    }

    override fun getItemCount(): Int = listings.size

    fun updateList(newListings: List<Listing>) {
        listings = newListings
        notifyDataSetChanged()
    }
}