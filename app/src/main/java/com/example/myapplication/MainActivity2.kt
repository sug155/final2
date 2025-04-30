package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity2 : AppCompatActivity() {

    private lateinit var videoView: VideoView
    private lateinit var imageView: ImageView
    private var mediaPlayer: MediaPlayer? = null
    private val CAMERA_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_others)
        val fire=findViewById<Button>(R.id.fire)
        val btnWifi = findViewById<Button>(R.id.btnWifi)
        val btnBluetooth = findViewById<Button>(R.id.btnBluetooth)
        val btnCamera = findViewById<Button>(R.id.btnCamera)
        val btnAudio = findViewById<Button>(R.id.btnAudio)
        val btnVideo = findViewById<Button>(R.id.btnVideo)
        val btnAnimate = findViewById<Button>(R.id.btnAnimate)
        videoView = findViewById(R.id.videoView)
        imageView = findViewById(R.id.imageView)
        fire.setOnClickListener{
            startActivity((Intent(this,ResultActivity::class.java)))
        }
        btnWifi.setOnClickListener {
            val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            val enabled = wifiManager.isWifiEnabled
            wifiManager.isWifiEnabled = !enabled
            Toast.makeText(this, "WiFi ${if (!enabled) "Enabled" else "Disabled"}", Toast.LENGTH_SHORT).show()
        }

        btnBluetooth.setOnClickListener {
            val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter != null) {
                if (bluetoothAdapter.isEnabled) {
                    // Check permissions before trying to disable Bluetooth
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        // Request Bluetooth permissions if not granted
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 1002)
                        return@setOnClickListener
                    }
                    bluetoothAdapter.disable()
                    Toast.makeText(this, "Bluetooth Disabled", Toast.LENGTH_SHORT).show()
                } else {
                    bluetoothAdapter.enable()
                    Toast.makeText(this, "Bluetooth Enabled", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show()
            }
        }

        btnCamera.setOnClickListener {
            checkCameraPermissionAndOpen()
        }

        btnAudio.setOnClickListener {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, R.raw.sample_audio)
            mediaPlayer?.start()
        }

        btnVideo.setOnClickListener {
            val uri = Uri.parse("android.resource://${packageName}/${R.raw.sample_video}")
            videoView.setVideoURI(uri)
            videoView.start()
        }

        btnAnimate.setOnClickListener {
            val anim = AnimationUtils.loadAnimation(this, R.anim.bounce)
            imageView.startAnimation(anim)
        }
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
            1002 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Bluetooth permission granted, continue with Bluetooth operation
                    val bluetoothAdapter = android.bluetooth.BluetoothAdapter.getDefaultAdapter()
                    if (bluetoothAdapter != null && bluetoothAdapter.isEnabled) {
                        bluetoothAdapter.disable()
                        Toast.makeText(this, "Bluetooth Disabled", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Bluetooth Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}
