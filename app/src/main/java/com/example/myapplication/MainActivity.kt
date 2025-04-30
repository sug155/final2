package com.example.myapplication

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest

// geocoder
import android.location.Geocoder
import android.os.Build
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import java.util.Locale

// sms
import android.Manifest

// notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {
    // menu
    private lateinit var contextMenuTextView: TextView

    // dialog
    private lateinit var showProgressBtn: Button
    private lateinit var progressBar: ProgressBar
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var textShown: TextView
    private lateinit var dateNeed: CalendarView
    private lateinit var btnShowDialog: Button

    // SharedPreferences variables
    private lateinit var editTextName: EditText
    private lateinit var btnSavePrefs: Button
    private lateinit var textViewSavedName: TextView
    private val PREFS_NAME = "MyAppPrefs"
    private val KEY_NAME = "username"

    // SharedPreferences Example 2 variables
    private lateinit var editId: EditText
    private lateinit var editName: EditText
    private lateinit var textViewShowId: TextView
    private lateinit var textViewShowName: TextView
    private lateinit var btnSave: Button
    private lateinit var btnView: Button
    private lateinit var btnClear: Button
    private val sharedPrefFile = "kotlinsharedpreference"
    //Map
    private lateinit var map: Button

    // Location variables
    private lateinit var locationProvideClient: FusedLocationProviderClient
    private lateinit var showLocation: Button

    // SMS variables
    private lateinit var phoneNumber: EditText
    private lateinit var message: EditText
    private lateinit var sendSmsButton: Button
    private lateinit var statusText: TextView
    private val SMS_PERMISSION_CODE = 102

    // Notification constants
    private val CHANNEL_ID = "notification_channel"
    private val NOTIFICATION_ID = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Map
        map=findViewById<Button>(R.id.map)
        map.setOnClickListener {
            startActivity(Intent(this, LocationActivity::class.java))
        }
        // Implicit Intent (Visit URL)
        val urlEditText: EditText = findViewById(R.id.urlEditText)
        val visitUrlButton: Button = findViewById(R.id.visitUrlButton)

        visitUrlButton.setOnClickListener {
            val url = urlEditText.text.toString().trim()

            if (url.isNotBlank()) {
                var finalUrl = url
                if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
                    finalUrl = "http://$finalUrl"
                }

                val uri = Uri.parse(finalUrl)
                val intent = Intent(Intent.ACTION_VIEW, uri)

                try {
                    val chooser = Intent.createChooser(intent, "Open link with")
                    startActivity(chooser)
                } catch (e: Exception) {
                    Toast.makeText(this, "No browser available to open the URL", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show()
            }
        }

        // Explicit Intent (Go to Second Activity)
        val goToSecondActivityButton: Button = findViewById(R.id.goToSecondActivityButton)
        goToSecondActivityButton.setOnClickListener {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }

        // menu
        // Context Menu
        contextMenuTextView = findViewById(R.id.contextMenuTextView)
        registerForContextMenu(contextMenuTextView)

        // Popup Menu
        val popupMenuButton: Button = findViewById(R.id.popupMenuButton)
        popupMenuButton.setOnClickListener {
            val popupMenu = PopupMenu(this, popupMenuButton)
            popupMenu.menuInflater.inflate(R.menu.popupmenu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_call -> {
                        Toast.makeText(this, "You Clicked : ${item.title}", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_web -> {
                        Toast.makeText(this, "You Clicked : ${item.title}", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.action_join -> {
                        Toast.makeText(this, "You Clicked : ${item.title}", Toast.LENGTH_SHORT).show()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        // dialogue
        //progress bar
        showProgressBtn = findViewById(R.id.showProgressBtn)
        progressBar = findViewById(R.id.progressBar)
        showProgressBtn.setOnClickListener {
            incrementProgress()
        }

        //date picker
        textShown = findViewById(R.id.textShown)
        dateNeed = findViewById(R.id.calenview)
        dateNeed.setOnDateChangeListener { _, year, month, dayOfMonth ->
            showDatePicker(year, month, dayOfMonth)
        }

        // Alert Dialog
        btnShowDialog = findViewById(R.id.btnShowDialog)
        btnShowDialog.setOnClickListener {
            showAlertDialog()
        }

        // SharedPreferences
        editTextName = findViewById(R.id.editTextName)
        btnSavePrefs = findViewById(R.id.btnSavePrefs)
        textViewSavedName = findViewById(R.id.textViewSavedName)

        // Load saved data from SharedPreferences
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val savedName = sharedPreferences.getString(KEY_NAME, "No name saved")
        textViewSavedName.text = "Saved Name: $savedName"

        // Set up save button click listener
        btnSavePrefs.setOnClickListener {
            val name = editTextName.text.toString()
            if (name.isNotEmpty()) {
                // Save to SharedPreferences
                val editor = sharedPreferences.edit()
                editor.putString(KEY_NAME, name)
                editor.apply()

                // Update the text view
                textViewSavedName.text = "Saved Name: $name"
                Toast.makeText(this, "Name saved successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show()
            }
        }

        // SharedPreferences Example 2
        editId = findViewById(R.id.editId)
        editName = findViewById(R.id.editName)
        textViewShowId = findViewById(R.id.textViewShowId)
        textViewShowName = findViewById(R.id.textViewShowName)
        btnSave = findViewById(R.id.save)
        btnView = findViewById(R.id.view)
        btnClear = findViewById(R.id.clear)

        val sharedPreferencesEx2: SharedPreferences = this.getSharedPreferences(sharedPrefFile, MODE_PRIVATE)

        val goDatabaseButton: Button = findViewById(R.id.goDatabaseButton)
        goDatabaseButton.setOnClickListener {
            val intent = Intent(this, DatabaseActivity::class.java)
            startActivity(intent)
        }

        btnSave.setOnClickListener {
            if (editId.text.toString().isEmpty()) {
                Toast.makeText(this, "Please enter an ID", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val id: Int = Integer.parseInt(editId.text.toString())
            val name: String = editName.text.toString()
            val editor: SharedPreferences.Editor = sharedPreferencesEx2.edit()
            editor.putInt("id_key", id)
            editor.putString("name_key", name)
            editor.apply()
            editor.commit()

            Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
        }

        btnView.setOnClickListener {
            val sharedIdValue = sharedPreferencesEx2.getInt("id_key", 0)
            val sharedNameValue = sharedPreferencesEx2.getString("name_key", "defaultname")

            if (sharedIdValue == 0 && sharedNameValue == "defaultname") {
                textViewShowId.text = "Default ID: $sharedIdValue"
                textViewShowName.text = "Default Name: $sharedNameValue"
            } else {
                textViewShowId.text = "Your ID: $sharedIdValue"
                textViewShowName.text = "Your Name: $sharedNameValue"
            }
        }

        btnClear.setOnClickListener {
            val editor = sharedPreferencesEx2.edit()
            editor.clear()
            editor.apply()

            textViewShowId.text = "Your ID: "
            textViewShowName.text = "Your Name: "
            editId.text.clear()
            editName.text.clear()

            Toast.makeText(this, "Data cleared", Toast.LENGTH_SHORT).show()
        }

        // Location setup
        // Initialize location provider client
        locationProvideClient = LocationServices.getFusedLocationProviderClient(this)

        // Bind location button
        showLocation = findViewById(R.id.show_my_location)
        showLocation.setOnClickListener {
            getYourCurrentLocation()
        }

        // SMS setup
        phoneNumber = findViewById(R.id.phoneNumber)
        message = findViewById(R.id.message)
        sendSmsButton = findViewById(R.id.sendSmsButton)
        statusText = findViewById(R.id.statusText)

        // Request SMS permission at runtime
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.SEND_SMS),
                SMS_PERMISSION_CODE
            )
        }

        sendSmsButton.setOnClickListener {
            val phone = phoneNumber.text.toString().trim()
            val msg = message.text.toString().trim()
            if (phone.isNotEmpty() && msg.isNotEmpty()) {
                sendSms(phone, msg)
            } else {
                Toast.makeText(this, "Please enter phone number and message",
                    Toast.LENGTH_SHORT).show()
            }
        }

        // Notification setup
        val notifyButton: Button = findViewById(R.id.btnNotify)

        // Request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }

        // Create notification channel
        createNotificationChannel()

        // Set click listener for notification button
        notifyButton.setOnClickListener {
            showNotification()
        }
    }

    // menu
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(this, "You clicked on setting", Toast.LENGTH_LONG).show()
                true
            }
            R.id.action_share -> {
                Toast.makeText(this, "You clicked on share", Toast.LENGTH_LONG).show()
                true
            }
            R.id.action_exit -> {
                Toast.makeText(this, "You clicked on exit", Toast.LENGTH_LONG).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // Context Menu
    override fun onCreateContextMenu(
        menu: ContextMenu?,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        if (v?.id == R.id.contextMenuTextView) {
            menuInflater.inflate(R.menu.context_menu_colour1, menu)
            menu?.setHeaderTitle("Choose a color")
        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.color_red -> {
                contextMenuTextView.setTextColor(Color.RED)
                true
            }
            R.id.color_green -> {
                contextMenuTextView.setTextColor(Color.GREEN)
                true
            }
            R.id.color_blue -> {
                contextMenuTextView.setTextColor(Color.BLUE)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    // dialogue
    private fun incrementProgress() {
        Thread {
            for (i in 1..100) {
                Thread.sleep(50)
                handler.post {
                    progressBar.progress = i
                }
            }
        }.start()
    }

    private fun showDatePicker(year: Int, month: Int, day: Int) {
        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                textShown.text = "Date: $selectedDay/${selectedMonth + 1}/$selectedYear"
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Alert")
            .setMessage("This is a simple alert dialog.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // location
    private fun getYourCurrentLocation() {
        // Check if we have permission
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                Companion.LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Check if location settings are enabled
        checkLocationSettings()
    }

    private fun checkLocationSettings() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
        }

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)

        val client = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            // Location settings are satisfied, we can get location
            fetchLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, show dialog to enable
                try {
                    exception.startResolutionForResult(this, Companion.REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error
                }
            }
        }
    }

    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // First try getting last location
        locationProvideClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    Toast.makeText(
                        this,
                        "Latitude: $latitude\nLongitude: $longitude",
                        Toast.LENGTH_LONG
                    ).show()

                    // Get address from coordinates
                    val addressTextView: TextView = findViewById(R.id.addressTextView)
                    addressTextView.text = "Looking up address..."

                    // Use a background thread for geocoding
                    Thread {
                        val address = getAddressFromLatLng(latitude, longitude)
                        runOnUiThread {
                            addressTextView.text = address
                        }
                    }.start()
                } else {
                    // If last location is null, request fresh location
                    Toast.makeText(
                        this,
                        "Getting your current location...",
                        Toast.LENGTH_SHORT
                    ).show()
                    requestNewLocationData()
                }
            }
    }

    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 10000
            fastestInterval = 5000
            numUpdates = 1 // Get just one location update
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude

                    Toast.makeText(
                        this@MainActivity,
                        "Latitude: $latitude\nLongitude: $longitude",
                        Toast.LENGTH_LONG
                    ).show()

                    // Get address from coordinates
                    val addressTextView: TextView = findViewById(R.id.addressTextView)
                    addressTextView.text = "Looking up address..."

                    // Use a background thread for geocoding
                    Thread {
                        val address = getAddressFromLatLng(latitude, longitude)
                        runOnUiThread {
                            addressTextView.text = address
                        }
                    }.start()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Cannot get your location. Please try again later.",
                        Toast.LENGTH_SHORT
                    ).show()

                    val addressTextView: TextView = findViewById(R.id.addressTextView)
                    addressTextView.text = "Address unavailable"
                }

                // Remove location updates to conserve battery
                locationProvideClient.removeLocationUpdates(this)
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationProvideClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Companion.REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                // User enabled location settings
                fetchLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location settings are not enabled",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getAddressFromLatLng(lat: Double, lng: Double): String? {
        return try {
            val geocoder = Geocoder(this, Locale.getDefault())

            // For SDK 33+ (Android 13+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(lat, lng, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val address = addresses[0]
                        val addressLine = address.getAddressLine(0) ?: ""
                        val locality = address.locality ?: ""
                        val countryName = address.countryName ?: ""

                        runOnUiThread {
                            val addressTextView: TextView = findViewById(R.id.addressTextView)
                            addressTextView.text = "$addressLine, $locality, $countryName"
                        }
                    }
                }
                "Fetching address..."
            } else {
                // For SDK < 33
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val addressLine = address.getAddressLine(0) ?: ""
                    val locality = address.locality ?: ""
                    val countryName = address.countryName ?: ""
                    "$addressLine, $locality, $countryName"
                } else {
                    "No address found"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            "Error fetching address: ${e.message}"
        }
    }

    // sms
    private fun sendSms(phone: String, msg: String) {
        try {
            val smsManager: SmsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phone, null, msg, null, null)
            statusText.text = "SMS Sent Successfully!"
            Toast.makeText(this, "SMS Sent!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            statusText.text = "SMS Failed to Send!"
            Toast.makeText(this, "SMS Failed!", Toast.LENGTH_SHORT).show()
        }
    }

    // notification
    private fun showNotification() {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)  // Using a system icon
            .setContentTitle("Simple Notification")
            .setContentText("This is a status bar notification.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification Channel"
            val descriptionText = "Channel for status notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Handle permission request result
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            // location
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted, proceed with location
                    checkLocationSettings()
                } else {
                    Toast.makeText(
                        this,
                        "Location permission denied",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            // sms
            SMS_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show()
                    statusText.text = "Cannot send SMS: Permission denied"
                }
            }
            // notification
            NOTIFICATION_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CHECK_SETTINGS = 123
        private const val LOCATION_PERMISSION_REQUEST_CODE = 909
        private const val NOTIFICATION_PERMISSION_CODE = 103
    }
}