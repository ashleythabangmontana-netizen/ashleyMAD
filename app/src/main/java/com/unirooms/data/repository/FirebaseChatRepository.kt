package com.bac.unirooms.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

data class FirebaseMessage(
    val id: String = "",
    val senderId: Int = 0,
    val receiverId: Int = 0,
    val listingId: Int = 0,
    val content: String = "",
    val timestamp: Long = 0L,
    val senderName: String = ""
)

class FirebaseChatRepository {

    private val database = FirebaseDatabase.getInstance()

    private fun getChatRef(userId1: Int, userId2: Int, listingId: Int) =
        database.getReference("chats")
            .child("listing_$listingId")
            .child("${minOf(userId1, userId2)}_${maxOf(userId1, userId2)}")

    fun sendMessage(
        senderId: Int,
        receiverId: Int,
        listingId: Int,
        content: String,
        senderName: String
    ) {
        val ref = getChatRef(senderId, receiverId, listingId)
        val messageId = ref.push().key ?: return
        val message = FirebaseMessage(
            id = messageId,
            senderId = senderId,
            receiverId = receiverId,
            listingId = listingId,
            content = content,
            timestamp = System.currentTimeMillis(),
            senderName = senderName
        )
        ref.child(messageId).setValue(message)
    }

    fun getMessages(
        userId1: Int,
        userId2: Int,
        listingId: Int
    ): LiveData<List<FirebaseMessage>> {
        val liveData = MutableLiveData<List<FirebaseMessage>>()
        val ref = getChatRef(userId1, userId2, listingId)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<FirebaseMessage>()
                for (child in snapshot.children) {
                    val msg = child.getValue(FirebaseMessage::class.java)
                    if (msg != null) messages.add(msg)
                }
                messages.sortBy { it.timestamp }
                liveData.postValue(messages)
            }

            override fun onCancelled(error: DatabaseError) {
                liveData.postValue(emptyList())
            }
        })

        return liveData
    }

    fun getUnreadCount(userId: Int): LiveData<Int> {
        val liveData = MutableLiveData<Int>(0)
        return liveData
    }
}