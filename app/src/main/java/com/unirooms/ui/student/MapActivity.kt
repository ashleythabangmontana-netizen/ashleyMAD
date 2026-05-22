package com.bac.unirooms.ui.student

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.bac.unirooms.R
import com.bac.unirooms.databinding.ActivityMapBinding
import com.bac.unirooms.utils.SessionManager
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var session: SessionManager

    private var listingLat: Double = -24.6561
    private var listingLng: Double = 25.9144
    private var selectedCampusLat: Double = -24.6528
    private var selectedCampusLng: Double = 25.9124

    // BAC is first, all coordinates are accurate for Gaborone
    private val campusMap = linkedMapOf(
        "Botswana Accountancy College (BAC)" to Pair(-24.6528, 25.9124),
        "University of Botswana (UB)" to Pair(-24.6561, 25.9144),
        "Botho University" to Pair(-24.6548, 25.9002),
        "BAISAGO University" to Pair(-24.6431, 25.9086),
        "Limkokwing University" to Pair(-24.6607, 25.9182),
        "Botswana Open University (BOU)" to Pair(-24.6590, 25.9070)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)

        listingLat = intent.getDoubleExtra("listingLat", -24.6561)
        listingLng = intent.getDoubleExtra("listingLng", 25.9144)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val campusNames = campusMap.keys.toList()
        val campusAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            campusNames
        )
        binding.spinnerCampus.adapter = campusAdapter

        val preferredCampus = session.getPreferredCampus()
        val prefIndex = campusNames.indexOfFirst { it == preferredCampus }
        binding.spinnerCampus.setSelection(if (prefIndex >= 0) prefIndex else 0)

        val initialCoords = campusMap[campusNames[binding.spinnerCampus.selectedItemPosition]]
        if (initialCoords != null) {
            selectedCampusLat = initialCoords.first
            selectedCampusLng = initialCoords.second
        }

        binding.spinnerCampus.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                position: Int,
                id: Long
            ) {
                val coords = campusMap[campusNames[position]] ?: return
                selectedCampusLat = coords.first
                selectedCampusLng = coords.second
                updateDistanceDisplay()
                if (::googleMap.isInitialized) updateMapMarkers()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnNavigate.setOnClickListener {
            openNavigationIntent()
        }
    }

    private fun updateDistanceDisplay() {
        val distKm = haversineDistance(
            listingLat, listingLng,
            selectedCampusLat, selectedCampusLng
        )
        binding.tvDistance.text = String.format("%.1f km from campus", distKm)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
        updateMapMarkers()
    }

    private fun updateMapMarkers() {
        googleMap.clear()

        val listingPosition = LatLng(listingLat, listingLng)
        val campusPosition = LatLng(selectedCampusLat, selectedCampusLng)

        googleMap.addMarker(
            MarkerOptions()
                .position(listingPosition)
                .title("This Listing")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        )

        googleMap.addMarker(
            MarkerOptions()
                .position(campusPosition)
                .title("Campus")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        googleMap.addPolyline(
            PolylineOptions()
                .add(listingPosition, campusPosition)
                .width(5f)
                .color(0xFF1E3A5F.toInt())
        )

        val bounds = LatLngBounds.Builder()
            .include(listingPosition)
            .include(campusPosition)
            .build()

        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150))
        updateDistanceDisplay()
    }

    private fun openNavigationIntent() {
        val uri = Uri.parse("google.navigation:q=$listingLat,$listingLng&mode=d")
        val mapIntent = Intent(Intent.ACTION_VIEW, uri)
        mapIntent.setPackage("com.google.android.apps.maps")
        if (mapIntent.resolveActivity(packageManager) != null) {
            startActivity(mapIntent)
        } else {
            val browserUri = Uri.parse(
                "https://www.google.com/maps/dir/?api=1&destination=$listingLat,$listingLng"
            )
            startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        }
    }

    private fun haversineDistance(
        lat1: Double, lng1: Double,
        lat2: Double, lng2: Double
    ): Double {
        val earthRadius = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2)
        val c = 2 * asin(sqrt(a))
        return earthRadius * c
    }
}