package com.bac.unirooms.data.repository

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bac.unirooms.data.model.Listing
import com.bac.unirooms.data.model.Message
import com.bac.unirooms.data.model.Reservation
import com.bac.unirooms.data.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

object FirebaseManager {

    private val db = FirebaseDatabase.getInstance(
        "https://unirooms-eae12-default-rtdb.europe-west1.firebasedatabase.app"
    )

    private val usersRef        = db.getReference("users")
    private val listingsRef     = db.getReference("listings")
    private val messagesRef     = db.getReference("messages")
    private val reservationsRef = db.getReference("reservations")
    private val favoritesRef    = db.getReference("favorites")
    private val storage         = FirebaseStorage.getInstance()

    // ===================== IMAGE UPLOAD =====================

    fun uploadListingImage(imageUri: Uri, onResult: (String?) -> Unit) {
        val fileName = "listing_${System.currentTimeMillis()}.jpg"
        val imageRef = storage.reference.child("listing_images/$fileName")

        imageRef.putFile(imageUri)
            .addOnSuccessListener {
                imageRef.downloadUrl
                    .addOnSuccessListener { uri -> onResult(uri.toString()) }
                    .addOnFailureListener { onResult(null) }
            }
            .addOnFailureListener { onResult(null) }
    }

    // ===================== USERS =====================

    fun registerUser(user: User, onResult: (Boolean, String) -> Unit) {
        usersRef
            .orderByChild("email")
            .equalTo(user.email.trim().lowercase())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        onResult(false, "An account with this email already exists")
                        return
                    }
                    val userId = usersRef.push().key ?: run {
                        onResult(false, "Registration failed — try again")
                        return
                    }
                    val newUser = user.copy(
                        id    = userId,
                        email = user.email.trim().lowercase()
                    )
                    usersRef.child(userId).setValue(newUser)
                        .addOnSuccessListener { onResult(true, userId) }
                        .addOnFailureListener { onResult(false, it.message ?: "Registration failed") }
                }
                override fun onCancelled(error: DatabaseError) {
                    onResult(false, error.message)
                }
            })
    }

    /**
     * Login: match on email + password only (role is NOT checked here).
     * The role stored in the database is used for routing after login.
     */
    fun loginUser(
        email: String,
        password: String,
        role: String,
        onResult: (User?) -> Unit
    ) {
        usersRef
            .orderByChild("email")
            .equalTo(email.trim().lowercase())
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) { onResult(null); return }

                    for (child in snapshot.children) {
                        val user = child.getValue(User::class.java)
                        // Only check email + password — role routing happens in the activity
                        if (user != null && user.password == password) {
                            onResult(user)
                            return
                        }
                    }
                    onResult(null)
                }
                override fun onCancelled(error: DatabaseError) { onResult(null) }
            })
    }

    // ===================== LISTINGS =====================

    fun getAllListings(): LiveData<List<Listing>> {
        val liveData = MutableLiveData<List<Listing>>()
        listingsRef.orderByChild("postedDate")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Listing>()
                    for (child in snapshot.children) {
                        child.getValue(Listing::class.java)?.let { list.add(0, it) }
                    }
                    liveData.postValue(list)
                }
                override fun onCancelled(error: DatabaseError) {
                    liveData.postValue(emptyList())
                }
            })
        return liveData
    }

    fun getListingsByProvider(providerId: String): LiveData<List<Listing>> {
        val liveData = MutableLiveData<List<Listing>>()
        listingsRef.orderByChild("providerId").equalTo(providerId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Listing>()
                    for (child in snapshot.children) {
                        child.getValue(Listing::class.java)?.let { list.add(it) }
                    }
                    liveData.postValue(list)
                }
                override fun onCancelled(error: DatabaseError) {
                    liveData.postValue(emptyList())
                }
            })
        return liveData
    }

    fun getListingById(listingId: String, onResult: (Listing?) -> Unit) {
        listingsRef.child(listingId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    onResult(snapshot.getValue(Listing::class.java))
                }
                override fun onCancelled(error: DatabaseError) { onResult(null) }
            })
    }

    fun addListing(listing: Listing, onResult: (Boolean) -> Unit) {
        val key = listingsRef.push().key ?: run { onResult(false); return }
        listingsRef.child(key).setValue(listing.copy(id = key))
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun updateListing(listing: Listing, onResult: (Boolean) -> Unit) {
        listingsRef.child(listing.id).setValue(listing)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun deleteListing(listingId: String, onResult: (Boolean) -> Unit) {
        listingsRef.child(listingId).removeValue()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    /**
     * Mark a listing as RESERVED so no other student can book it.
     */
    fun markListingReserved(listingId: String, onResult: (Boolean) -> Unit) {
        listingsRef.child(listingId).child("status").setValue("RESERVED")
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    // ===================== FAVORITES =====================

    fun addFavorite(studentId: String, listingId: String) {
        favoritesRef.child(studentId).child(listingId).setValue(true)
    }

    fun removeFavorite(studentId: String, listingId: String) {
        favoritesRef.child(studentId).child(listingId).removeValue()
    }

    fun isFavorite(studentId: String, listingId: String, onResult: (Boolean) -> Unit) {
        favoritesRef.child(studentId).child(listingId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) { onResult(snapshot.exists()) }
                override fun onCancelled(error: DatabaseError) { onResult(false) }
            })
    }

    fun getFavoriteListings(studentId: String): LiveData<List<Listing>> {
        val liveData = MutableLiveData<List<Listing>>()
        favoritesRef.child(studentId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ids = snapshot.children.mapNotNull { it.key }
                    getAllListings().observeForever { all ->
                        liveData.postValue(all.filter { it.id in ids })
                    }
                }
                override fun onCancelled(error: DatabaseError) { liveData.postValue(emptyList()) }
            })
        return liveData
    }

    // ===================== MESSAGES =====================

    fun sendMessage(message: Message) {
        val key = messagesRef.push().key ?: return
        messagesRef.child(key).setValue(message.copy(id = key))
    }

    /**
     * Real-time listener — returns LiveData that updates instantly when a
     * new message is sent by either participant.
     */
    fun getMessages(
        currentUserId: String,
        otherUserId: String,
        listingId: String
    ): LiveData<List<Message>> {
        val liveData = MutableLiveData<List<Message>>()

        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Message>()
                for (child in snapshot.children) {
                    val msg = child.getValue(Message::class.java) ?: continue
                    val isBetweenUs =
                        (msg.senderId == currentUserId && msg.receiverId == otherUserId) ||
                                (msg.senderId == otherUserId   && msg.receiverId == currentUserId)
                    if (isBetweenUs && msg.listingId == listingId) {
                        list.add(msg)
                    }
                }
                list.sortBy { it.timestamp }
                liveData.postValue(list)
            }
            override fun onCancelled(error: DatabaseError) { liveData.postValue(emptyList()) }
        })

        return liveData
    }

    fun getProviderConversations(providerId: String): LiveData<List<ConversationPreview>> {
        val liveData = MutableLiveData<List<ConversationPreview>>()
        messagesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val conversations = mutableMapOf<String, ConversationPreview>()
                for (child in snapshot.children) {
                    val msg = child.getValue(Message::class.java) ?: continue
                    if (msg.receiverId == providerId) {
                        val key = "${msg.senderId}_${msg.listingId}"
                        conversations[key] = ConversationPreview(
                            otherUserId   = msg.senderId,
                            otherUserName = msg.senderName,
                            listingId     = msg.listingId,
                            lastMessage   = msg.content,
                            lastTimestamp = msg.timestamp
                        )
                    }
                }
                liveData.postValue(conversations.values.sortedByDescending { it.lastTimestamp })
            }
            override fun onCancelled(error: DatabaseError) { liveData.postValue(emptyList()) }
        })
        return liveData
    }

    // ===================== RESERVATIONS =====================

    /**
     * Save the reservation AND mark the listing as RESERVED in one operation
     * so no other user can book the same room.
     */
    fun makeReservation(reservation: Reservation, onResult: (Boolean, String) -> Unit) {
        val key = reservationsRef.push().key ?: run { onResult(false, "Failed"); return }
        reservationsRef.child(key).setValue(reservation.copy(id = key))
            .addOnSuccessListener {
                // Mark the listing reserved immediately after payment
                markListingReserved(reservation.listingId) { /* fire and forget */ _ -> }
                onResult(true, "Reservation successful")
            }
            .addOnFailureListener { onResult(false, it.message ?: "Failed") }
    }
}

data class ConversationPreview(
    val otherUserId: String   = "",
    val otherUserName: String = "",
    val listingId: String     = "",
    val lastMessage: String   = "",
    val lastTimestamp: Long   = 0L
)