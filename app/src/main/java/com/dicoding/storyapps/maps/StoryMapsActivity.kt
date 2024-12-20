package com.dicoding.storyapps.maps

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.dicoding.storyapps.R
import com.dicoding.storyapps.databinding.ActivityStoryMapsBinding
import com.dicoding.storyapps.uti.Result
import com.dicoding.storyapps.uti.ViewModelFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions

class StoryMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val binding: ActivityStoryMapsBinding by lazy {
        ActivityStoryMapsBinding.inflate(layoutInflater)
    }

    private val storyMapsViewModel: StoryMapsViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    private val boundsBuilder = LatLngBounds.Builder()

    private lateinit var mMap: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupView()

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isIndoorLevelPickerEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }

//        setMapsViewStyle()
        getMapsViewLocation()
        getMapsStoryLocation()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.fragment_maps) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setMapsViewStyle() {
        try {
            val success =
                mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                    this,
                    R.raw.maps_style
                ))
            if (!success) {
                Log.e("MAPS_STYLE", "Fetching failed")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("MAPS_STYLE", "Maps style not found: ", e)
        }
    }

    private fun getMapsViewLocation() {
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            mMap.isMyLocationEnabled = true
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getMapsStoryLocation() {
        storyMapsViewModel.getUserToken().observe(
            this@StoryMapsActivity
        ) {
            if (it.token != "") {
                getStoryList(it.token)
            }
        }
    }

    private fun getStoryList(token: String) {
        storyMapsViewModel.getStoriesWithLocation(token).observe(
            this@StoryMapsActivity
        ) { it ->
            if (it != null) {
                when (it) {

                    is Result.Loading -> {
                        showLoading(true)
                    }

                    is Result.Success -> {
                        showLoading(false)
                        it.data.forEach {
                            if (it.lat != null && it.lon != null) {
                                val latLng = LatLng(it.lat, it.lon)
                                mMap.addMarker(
                                    MarkerOptions()
                                        .position(latLng)
                                        .title(it.name)
                                        .snippet(it.description)
                                )
                                boundsBuilder.include(latLng)
                            }
                        }
                        val bounds: LatLngBounds = boundsBuilder.build()
                        mMap.animateCamera(
                            CameraUpdateFactory.newLatLngBounds(
                                bounds,
                                resources.displayMetrics.widthPixels,
                                resources.displayMetrics.heightPixels,
                                200
                            )
                        )
                    }

                    is Result.Error -> {
                        showLoading(false)
                        Toast.makeText(
                            this@StoryMapsActivity,
                            "onError: ${it.error}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                getMapsViewLocation()
            }
        }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}