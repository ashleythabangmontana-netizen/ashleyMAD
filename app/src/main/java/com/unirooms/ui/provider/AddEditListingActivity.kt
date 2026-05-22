package com.bac.unirooms.ui.provider

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bac.unirooms.data.model.Listing
import com.bac.unirooms.data.repository.FirebaseManager
import com.bac.unirooms.databinding.ActivityAddEditListingBinding
import com.bac.unirooms.utils.ReceiptGenerator
import com.bac.unirooms.utils.SessionManager
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class AddEditListingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditListingBinding
    private lateinit var session: SessionManager
    private var editListingId: String = ""
    private var selectedAvailabilityDate: Long = System.currentTimeMillis()
    private var selectedPhotoUri: Uri? = null
    private var uploadedPhotoUrl: String = ""

    // ── Photo picker launcher ──────────────────────────────────
    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = result.data?.data
            if (uri != null) {
                selectedPhotoUri = uri
                binding.ivListingPhoto.setImageURI(uri)
                binding.btnPickPhoto.text = "Uploading..."

                FirebaseManager.uploadListingImage(uri) { imageUrl ->
                    runOnUiThread {
                        if (imageUrl != null) {
                            uploadedPhotoUrl = imageUrl
                            binding.btnPickPhoto.text = "Change Photo"
                            Toast.makeText(this, "Photo uploaded successfully", Toast.LENGTH_SHORT).show()
                        } else {
                            binding.btnPickPhoto.text = "Pick Photo"
                            Toast.makeText(this, "Photo upload failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    // ── Runtime permission launcher ───────────────────────────
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchGalleryIntent()
        } else {
            Toast.makeText(this, "Gallery permission is needed to pick a photo", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditListingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        editListingId = intent.getStringExtra("listingId") ?: ""

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.title = if (editListingId.isEmpty()) "Add Listing" else "Edit Listing"

        val types = resources.getStringArray(com.bac.unirooms.R.array.house_types)
        binding.spinnerType.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, types
        )

        val locations = resources.getStringArray(com.bac.unirooms.R.array.gaborone_locations).drop(1)
        binding.spinnerLocation.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, locations
        )

        if (editListingId.isNotEmpty()) {
            loadExistingListing()
        }

        binding.btnPickPhoto.setOnClickListener { openPhotoPicker() }
        binding.ivListingPhoto.setOnClickListener { openPhotoPicker() }

        binding.btnPickAvailability.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    cal.set(year, month, day)
                    selectedAvailabilityDate = cal.timeInMillis
                    binding.tvAvailabilityDate.text =
                        ReceiptGenerator.formatDateShort(selectedAvailabilityDate)
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnSaveListing.setOnClickListener { saveListing() }
    }

    // ── Permission-aware photo picker ─────────────────────────
    private fun openPhotoPicker() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES      // Android 13+
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE  // Android 12 and below
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                // Already have permission — open gallery straight away
                launchGalleryIntent()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // User previously denied — explain why we need it, then ask again
                Toast.makeText(
                    this,
                    "Please allow gallery access so you can add photos to your listing",
                    Toast.LENGTH_LONG
                ).show()
                permissionLauncher.launch(permission)
            }
            else -> {
                // First time asking
                permissionLauncher.launch(permission)
            }
        }
    }

    private fun launchGalleryIntent() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        photoPickerLauncher.launch(intent)
    }

    private fun saveImageToInternalStorage(uri: Uri): String {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return ""
            val fileName = "listing_${System.currentTimeMillis()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun loadExistingListing() {
        FirebaseManager.getListingById(editListingId) { listing ->
            runOnUiThread {
                if (listing != null) {
                    binding.etTitle.setText(listing.title)
                    binding.etDescription.setText(listing.description)
                    binding.etPrice.setText(listing.price.toInt().toString())
                    binding.etDeposit.setText(listing.depositAmount.toInt().toString())
                    binding.etAddress.setText(listing.address)
                    binding.etAmenities.setText(listing.amenities)
                    binding.etLatitude.setText(listing.latitude.toString())
                    binding.etLongitude.setText(listing.longitude.toString())
                    binding.switchSharing.isChecked = listing.sharingAllowed
                    selectedAvailabilityDate = listing.availabilityDate
                    binding.tvAvailabilityDate.text =
                        ReceiptGenerator.formatDateShort(selectedAvailabilityDate)
                    uploadedPhotoUrl = listing.photoPath ?: ""

                    if (uploadedPhotoUrl.isNotEmpty()) {
                        binding.btnPickPhoto.text = "Change Photo"
                    }

                    val types = resources.getStringArray(com.bac.unirooms.R.array.house_types)
                    val typeIndex = types.indexOfFirst { it.equals(listing.type, ignoreCase = true) }
                    if (typeIndex >= 0) binding.spinnerType.setSelection(typeIndex)

                    val locations = resources.getStringArray(
                        com.bac.unirooms.R.array.gaborone_locations
                    ).drop(1)
                    val locIndex = locations.indexOfFirst { it.equals(listing.location, ignoreCase = true) }
                    if (locIndex >= 0) binding.spinnerLocation.setSelection(locIndex)
                }
            }
        }
    }

    private fun saveListing() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val priceText = binding.etPrice.text.toString()
        val depositText = binding.etDeposit.text.toString()
        val address = binding.etAddress.text.toString().trim()
        val amenities = binding.etAmenities.text.toString().trim()
        val latText = binding.etLatitude.text.toString()
        val lngText = binding.etLongitude.text.toString()

        if (title.isEmpty()) { Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show(); return }
        if (description.isEmpty()) { Toast.makeText(this, "Description is required", Toast.LENGTH_SHORT).show(); return }

        val price = priceText.toDoubleOrNull()
        if (price == null || price < 700 || price > 2000) {
            Toast.makeText(this, "Price must be between BWP 700 and BWP 2000", Toast.LENGTH_SHORT).show()
            return
        }
        val deposit = depositText.toDoubleOrNull()
        if (deposit == null || deposit <= 0) {
            Toast.makeText(this, "Enter a valid deposit amount", Toast.LENGTH_SHORT).show()
            return
        }
        if (address.isEmpty()) { Toast.makeText(this, "Address is required", Toast.LENGTH_SHORT).show(); return }
        if (amenities.isEmpty()) { Toast.makeText(this, "Amenities are required", Toast.LENGTH_SHORT).show(); return }

        val lat = latText.toDoubleOrNull() ?: -24.6561
        val lng = lngText.toDoubleOrNull() ?: 25.9144

        val types = resources.getStringArray(com.bac.unirooms.R.array.house_types)
        val type = types[binding.spinnerType.selectedItemPosition]

        val locations = resources.getStringArray(com.bac.unirooms.R.array.gaborone_locations).drop(1)
        val location = locations[binding.spinnerLocation.selectedItemPosition]

        binding.btnSaveListing.isEnabled = false
        binding.btnSaveListing.text = "Saving..."

        val listing = Listing(
            id = editListingId,
            providerId = session.getUserId(),
            providerName = session.getUserName(),
            title = title,
            description = description,
            price = price,
            depositAmount = deposit,
            location = location,
            address = address,
            type = type,
            amenities = amenities,
            availabilityDate = selectedAvailabilityDate,
            latitude = lat,
            longitude = lng,
            sharingAllowed = binding.switchSharing.isChecked,
            photoPath = uploadedPhotoUrl
        )

        if (editListingId.isEmpty()) {
            FirebaseManager.addListing(listing) { success ->
                runOnUiThread {
                    binding.btnSaveListing.isEnabled = true
                    binding.btnSaveListing.text = "Save Listing"
                    if (success) {
                        Toast.makeText(this, "Listing added successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to add listing. Check connection.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            FirebaseManager.updateListing(listing) { success ->
                runOnUiThread {
                    binding.btnSaveListing.isEnabled = true
                    binding.btnSaveListing.text = "Save Listing"
                    if (success) {
                        Toast.makeText(this, "Listing updated", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to update. Check connection.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}