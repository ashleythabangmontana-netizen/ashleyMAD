package com.bac.unirooms.ui.provider

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bac.unirooms.data.repository.FirebaseManager
import com.bac.unirooms.databinding.ActivityProviderChatListBinding
import com.bac.unirooms.ui.adapter.ChatListAdapter
import com.bac.unirooms.ui.student.ChatActivity
import com.bac.unirooms.utils.SessionManager

class ProviderChatListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProviderChatListBinding
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProviderChatListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val providerId = session.getUserId()

        binding.rvChatList.layoutManager = LinearLayoutManager(this)

        FirebaseManager.getProviderConversations(providerId).observe(this) { conversations ->
            if (conversations.isEmpty()) {
                binding.tvNoChats.visibility = View.VISIBLE
                binding.rvChatList.visibility = View.GONE
            } else {
                binding.tvNoChats.visibility = View.GONE
                binding.rvChatList.visibility = View.VISIBLE

                val items = conversations.map { conv ->
                    ChatListAdapter.ConversationItem(
                        otherUserId = conv.otherUserId,
                        otherUserName = conv.otherUserName.ifEmpty { "Student" },
                        listingId = conv.listingId,
                        listingTitle = "Listing enquiry",
                        lastMessage = conv.lastMessage,
                        lastTimestamp = conv.lastTimestamp
                    )
                }

                binding.rvChatList.adapter = ChatListAdapter(items) { item ->
                    val intent = Intent(this, ChatActivity::class.java)
                    intent.putExtra("otherUserId", item.otherUserId)
                    intent.putExtra("listingId", item.listingId)
                    intent.putExtra("otherUserName", item.otherUserName)
                    startActivity(intent)
                }
            }
        }
    }
}