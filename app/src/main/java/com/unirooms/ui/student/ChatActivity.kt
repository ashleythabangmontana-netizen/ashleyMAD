package com.bac.unirooms.ui.student

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bac.unirooms.data.model.Message
import com.bac.unirooms.data.repository.FirebaseManager
import com.bac.unirooms.databinding.ActivityChatBinding
import com.bac.unirooms.ui.adapter.MessageAdapter
import com.bac.unirooms.utils.SessionManager

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var session: SessionManager
    private lateinit var adapter: MessageAdapter

    private var otherUserId: String = ""
    private var listingId: String = ""
    private var otherUserName: String = "Landlord"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        otherUserId = intent.getStringExtra("otherUserId") ?: ""
        listingId = intent.getStringExtra("listingId") ?: ""
        otherUserName = intent.getStringExtra("otherUserName") ?: "Landlord"

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = otherUserName
        binding.toolbar.setNavigationOnClickListener { finish() }

        val currentUserId = session.getUserId()
        val currentUserName = session.getUserName()

        adapter = MessageAdapter(emptyList(), currentUserId, otherUserName)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        binding.rvMessages.layoutManager = layoutManager
        binding.rvMessages.adapter = adapter

        FirebaseManager.getMessages(currentUserId, otherUserId, listingId).observe(this) { messages ->
            adapter.updateMessages(messages)
            binding.tvNoMessages.visibility = if (messages.isEmpty()) View.VISIBLE else View.GONE
            binding.rvMessages.visibility = if (messages.isEmpty()) View.GONE else View.VISIBLE
            if (messages.isNotEmpty()) {
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                val message = Message(
                    senderId = currentUserId,
                    senderName = currentUserName,
                    receiverId = otherUserId,
                    listingId = listingId,
                    content = text
                )
                FirebaseManager.sendMessage(message)
                binding.etMessage.text?.clear()
            }
        }
    }
}