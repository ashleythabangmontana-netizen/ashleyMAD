package com.bac.unirooms.ui.provider

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bac.unirooms.R
import com.bac.unirooms.data.model.Listing
import com.bac.unirooms.databinding.ItemProviderListingBinding

class ProviderListingAdapter(
    private var listings: List<Listing>,
    private val onEdit: (Listing) -> Unit,
    private val onDelete: (Listing) -> Unit
) : RecyclerView.Adapter<ProviderListingAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemProviderListingBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProviderListingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listing = listings[position]
        val context = holder.itemView.context

        holder.binding.tvProviderListingTitle.text = listing.title
        holder.binding.tvProviderListingPrice.text = "BWP ${String.format("%.0f", listing.price)}/mo"
        holder.binding.tvProviderListingLocation.text = listing.location

        val isReserved = listing.status == "RESERVED"
        holder.binding.tvProviderListingStatus.text = if (isReserved) "Reserved" else "Available"
        holder.binding.tvProviderListingStatus.setBackgroundResource(
            if (isReserved) R.drawable.bg_status_reserved else R.drawable.bg_status_available
        )
        holder.binding.tvProviderListingStatus.setTextColor(
            if (isReserved) context.getColor(R.color.colorReserved) else context.getColor(R.color.colorAvailable)
        )

        holder.binding.btnEditListing.setOnClickListener { onEdit(listing) }
        holder.binding.btnDeleteListing.setOnClickListener { onDelete(listing) }
    }

    override fun getItemCount(): Int = listings.size

    fun updateList(newListings: List<Listing>) {
        listings = newListings
        notifyDataSetChanged()
    }
}