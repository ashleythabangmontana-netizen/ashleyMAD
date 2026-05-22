package com.bac.unirooms.data.repository

import android.net.Uri
import android.util.Log
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
        "https://unirooms-3674a-default-rtdb.europe-west1.firebasedatabase.app"
    )

    private val usersRef      = db.getReference("users")
    private val listingsRef   = db.getReference("listings")
    private val messagesRef   = db.getReference("messages")
    private val reservationsRef = db.getReference("reservations")
    private val favoritesRef  = db.getReference("favorites")
    private val storage       = FirebaseStorage.getInstance()

    // ================= IMAGE UPLOAD =================

    fun uploadListingImage(imageUri: Uri, onResult: (String?) -> Unit) {
        val fileName = "listing_images/listing_${System.currentTimeMillis()}.jpg"
        val imageRef = storage.reference.child(fileName)

        Log.d("FirebaseManager", "Starting upload for: $imageUri")

        imageRef.putFile(imageUri)
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                Log.d("FirebaseManager", "Upload progress: $progress%")
            }
            .addOnSuccessListener {
                Log.d("FirebaseManager", "Upload succeeded, getting download URL")
                imageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        Log.d("FirebaseManager", "Download URL: $uri")
                        onResult(uri.toString())
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirebaseManager", "Failed to get download URL: ${e.message}")
                        onResult(null)
                    }
            }
            .addOnFailureListener { e ->
                // This is the most common failure point — Firebase Storage rules
                // are blocking the upload. Check Logcat for the exact error.
                Log.e("FirebaseManager", "Upload FAILED: ${e.message}")
                onResult(null)
            }
    }

    // ================= USERS =================

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
                        onResult(false, "Registration failed")
                        return
                    }
                    val newUser = user.copy(
                        id    = userId,
                        email = user.email.trim().lowercase(),
                        // Store password trimmed so login always matches
                        password = user.password.trim()
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

    // FIX: Removed the `role` parameter completely.
    // Login now checks email + password only. The user's real role is always
    // read from their Firebase record — so logging in from either the Student
    // or Provider screen will always work correctly.
    fun loginUser(email: String, password: String, onResult: (User?) -> Unit) {
        val cleanEmail    = email.trim().lowercase()
        val cleanPassword = password.trim()  // trim whitespace that might cause mismatch

        Log.d("FirebaseManager", "Attempting login for: $cleanEmail")

        usersRef
            .orderByChild("email")
            .equalTo(cleanEmail)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Log.d("FirebaseManager", "No user found with email: $cleanEmail")
                        onResult(null)
                        return
                    }
                    for (child in snapshot.children) {
                        val user = child.getValue(User::class.java)
                        Log.d("FirebaseManager", "Found user: ${user?.email}, role: ${user?.role}")
                        // Compare trimmed passwords to avoid whitespace mismatches
                        if (user != null && user.password.trim() == cleanPassword) {
                            Log.d("FirebaseManager", "Login success for: ${user.email}")
                            onResult(user)
                            return
                        }
                    }
                    Log.d("FirebaseManager", "Password did not match for: $cleanEmail")
                    onResult(null)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseManager", "Login cancelled: ${error.message}")
                    onResult(null)
                }
            })
    }

    // ================= LISTINGS =================

    fun getAllListings(): LiveData<List<Listing>> {
        val liveData = MutableLiveData<List<Listing>>()
        listingsRef.orderByChild("postedDate")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<Listing>()
                    for (child in snapshot.children) {
                        val listing = child.getValue(Listing::class.java)
                        if (listing != null) list.add(0, listing)
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
                        val listing = child.getValue(Listing::class.java)
                        if (listing != null) list.add(listing)
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
                override fun onCancelled(error: DatabaseError) {
                    onResult(null)
                }
            })
    }

    fun addListing(listing: Listing, onResult: (Boolean) -> Unit) {
        val key = listingsRef.push().key ?: run { onResult(false); return }
        val newListing = listing.copy(id = key)
        listingsRef.child(key).setValue(newListing)
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

    // ================= FAVORITES =================

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
                override fun onCancelled(error: DatabaseError)    { onResult(false) }
            })
    }

    fun getFavoriteListings(studentId: String): LiveData<List<Listing>> {
        val liveData = MutableLiveData<List<Listing>>()
        favoritesRef.child(studentId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val favoriteIds = snapshot.children.mapNotNull { it.key }
                    getAllListings().observeForever { allListings ->
                        liveData.postValue(allListings.filter { it.id in favoriteIds })
                    }
                }
                override fun onCancelled(error: DatabaseError) { liveData.postValue(emptyList()) }
            })
        return liveData
    }

    // ================= MESSAGES =================

    fun sendMessage(message: Message) {
        val key = messagesRef.push().key ?: return
        messagesRef.child(key).setValue(message.copy(id = key))
    }

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
                    val message = child.getValue(Message::class.java) ?: continue
                    val isConversation =
                        (message.senderId == currentUserId && message.receiverId == otherUserId) ||
                                (message.senderId == otherUserId   && message.receiverId == currentUserId)
                    if (isConversation && message.listingId == listingId) list.add(message)
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
                    val message = child.getValue(Message::class.java) ?: continue
                    if (message.receiverId == providerId) {
                        val key = "${message.senderId}_${message.listingId}"
                        conversations[key] = ConversationPreview(
                            otherUserId   = message.senderId,
                            otherUserName = message.senderName,
                            listingId     = message.listingId,
                            lastMessage   = message.content,
                            lastTimestamp = message.timestamp
                        )
                    }
                }
                liveData.postValue(conversations.values.sortedByDescending { it.lastTimestamp })
            }
            override fun onCancelled(error: DatabaseError) { liveData.postValue(emptyList()) }
        })
        return liveData
    }

    // ================= RESERVATIONS =================

    fun makeReservation(reservation: Reservation, onResult: (Boolean, String) -> Unit) {
        val key = reservationsRef.push().key ?: run { onResult(false, "Failed"); return }
        reservationsRef.child(key).setValue(reservation.copy(id = key))
            .addOnSuccessListener { onResult(true, "Reservation successful") }
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