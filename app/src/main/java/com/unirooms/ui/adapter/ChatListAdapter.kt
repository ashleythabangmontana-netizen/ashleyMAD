package com.bac.unirooms.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bac.unirooms.databinding.ItemChatListBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatListAdapter(
    private val conversations: List<ConversationItem>,
    private val onItemClick: (ConversationItem) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    data class ConversationItem(
        val otherUserId: String,
        val otherUserName: String,
        val listingId: String,
        val listingTitle: String,
        val lastMessage: String,
        val lastTimestamp: Long
    )

    inner class ChatViewHolder(val binding: ItemChatListBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatListBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = conversations[position]
        holder.binding.tvChatName.text = item.otherUserName
        holder.binding.tvChatListing.text = "Re: ${item.listingTitle}"
        holder.binding.tvChatTime.text = SimpleDateFormat(
            "HH:mm", Locale.getDefault()
        ).format(Date(item.lastTimestamp))
        holder.itemView.setOnClickListener { onItemClick(item) }
    }

    override fun getItemCount(): Int = conversations.size
}