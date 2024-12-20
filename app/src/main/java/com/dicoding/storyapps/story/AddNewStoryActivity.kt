package com.dicoding.storyapps.story

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.dicoding.storyapps.databinding.ActivityAddNewStoryBinding
import com.dicoding.storyapps.main.MainActivity
import com.dicoding.storyapps.uti.Utils.createCustomTempFile
import com.dicoding.storyapps.uti.Utils.reduceFileImage
import com.dicoding.storyapps.uti.Utils.rotateBitmap
import com.dicoding.storyapps.uti.Utils.uriToFile
import com.dicoding.storyapps.uti.ViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.dicoding.storyapps.uti.Result
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

@Suppress("DEPRECATION")
class AddNewStoryActivity : AppCompatActivity() {

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val REQUEST_CODE_PERMISSIONS = 10
    }

    private val binding: ActivityAddNewStoryBinding by lazy {
        ActivityAddNewStoryBinding.inflate(layoutInflater)
    }

    private val addNewStoryViewModel: AddNewStoryViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }

    private lateinit var currentPhotoPath: String

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var getFile: File? = null

    private var userLocation: Location? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(
                this@AddNewStoryActivity,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }


        setupView()
        setupAction()
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
        supportActionBar?.title = "Tambah Story"
    }

    private fun setupAction() {

        binding.buttonCamera.setOnClickListener {
            startCamera()
        }

        binding.buttonGalery.setOnClickListener {
            startGallery()
        }

        binding.buttonUpload.setOnClickListener {
            uploadImage()
        }

        binding.locationSwitch.setOnCheckedChangeListener { _, isCheked ->
            if (isCheked) {
                getMyLastLocation()
            } else {
                userLocation = null
            }
        }

    }

    private fun startCamera() {
        val intent = Intent(
            MediaStore.ACTION_IMAGE_CAPTURE
        )
        intent.resolveActivity(packageManager)

        createCustomTempFile(application).also {
            val photoUri: Uri = FileProvider.getUriForFile(
                this@AddNewStoryActivity,
                "com.dicoding.storyapps",
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            launcherIntentCamera.launch(intent)
        }
    }

    private fun startGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(
            intent,
            "Pilih gambar"
        )
        launcherIntentGallery.launch(chooser)
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
//            val isBackCamera = it.data?.getBooleanExtra("isBackCamera", true) as Boolean
            getFile = myFile
            val result = rotateBitmap(
                BitmapFactory.decodeFile(
                    getFile?.path,
                )
            )
            binding.viewPreviewImage.setImageBitmap(result)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val selectedImage: Uri = it.data?.data as Uri
            val myFile = uriToFile(
                selectedImage,
                this@AddNewStoryActivity
            )
            getFile = myFile

            binding.viewPreviewImage.setImageURI(selectedImage)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false -> {
                getMyLastLocation()
            }

            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false -> {
                getMyLastLocation()
            }

            else -> {
                binding.locationSwitch.isChecked = false
            }
        }
    }

    private fun uploadImage() {
        if (getFile != null) {
            val file = reduceFileImage(getFile as File)
            val description = binding.edDescriptionImage.text
                .toString()
                .trim()
                .toRequestBody("text/plain".toMediaType())
            val lat = userLocation?.latitude?.toString()?.toRequestBody("text/plain".toMediaType())
            val lon = userLocation?.longitude?.toString()?.toRequestBody("text/plain".toMediaType())
            val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
            val imageMultipartBody: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                file.name,
                requestImageFile
            )
            uploadStory(imageMultipartBody, description, lat, lon)
        } else {
            Toast.makeText(
                this@AddNewStoryActivity,
                "Masukkan gambarnya dulu ya!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun uploadStory(
        imageMultipartBody: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody?,
        lon: RequestBody?
    ) {
        addNewStoryViewModel.getUserToken().observe(
            this@AddNewStoryActivity
        ) { it ->
            if (it.token != "") {
                addNewStoryViewModel.uploadImage(it.token, imageMultipartBody, description, lat, lon).observe(
                    this@AddNewStoryActivity
                ) {
                    if (it != null) {
                        when (it) {

                            is Result.Loading -> {
                                showLoading(true)
                            }

                            is Result.Success -> {
                                showLoading(false)
                                AlertDialog.Builder(this).apply {
                                    setTitle("Yes!")
                                    setMessage("Story berhasil diunggah!!.")
                                    setPositiveButton("Lanjut") { _, _ ->
                                        val intent = Intent(
                                            this@AddNewStoryActivity,
                                            MainActivity::class.java
                                        )
                                        startActivity(intent)
                                        finish()
                                    }
                                    create()
                                    show()
                                }
                            }

                            is Result.Error -> {
                                showLoading(false)
                                Toast.makeText(
                                    this@AddNewStoryActivity,
                                    it.error,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(
                    this@AddNewStoryActivity,
                    "Maaf ya, Kamu tidak ada izin akses.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this@AddNewStoryActivity,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getMyLastLocation() {
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) && checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                if (it != null) {
                    userLocation = it
                } else {
                    binding.locationSwitch.isChecked = false
                }
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

}