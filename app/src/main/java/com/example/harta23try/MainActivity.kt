package com.example.harta23try

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlin.random.Random
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat

private const val REQUEST_ENABLE_BLUETOOTH = 1

class MainActivity : AppCompatActivity(),OnMapReadyCallback {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var scanCallback: ScanCallback

    private var mGoogleMap: GoogleMap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val filter = IntentFilter()
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(discoveryReceiver, filter)
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                // Process scan result here
                val device = result.device
                // Extract device information from 'result'
            }
        }


        val mapFragment = supportFragmentManager
            .findFragmentById((R.id.mapFragment)) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val myButton = findViewById<Button>(R.id.CheckForBluetooth)

        ///BUTTON FOR BLUETOOTH
        myButton.setOnClickListener {
            // Example: Show a toast message
//            Toast.makeText(this, "Button Clicked!", Toast.LENGTH_SHORT).show()
            var isworking = isBluetoothLeSupported(this)
            if (isworking==true) {
                Toast.makeText(this, "BLE works", Toast.LENGTH_SHORT).show()
                if(enableBluetooth(this)==1){
                    startBleDeviceDiscovery()

                }

            }
            else
                Toast.makeText(this,"BLE doesnt work",Toast.LENGTH_SHORT).show()
            // Example: Navigate to another activity
            // val intent = Intent(this, AnotherActivity::class.java)
            // startActivity(intent)
        }

    }

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        // Add the discovered device to the list
                        discoveredDevices.add(device)
                    }
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    // Scanning finished, you can process the discoveredDevices list here
                }
            }
        }
    }
    private val discoveredDevices: MutableList<BluetoothDevice> = mutableListOf()
    ///val Cluj = LatLng(46.770439,23.591423) ///work on zoom

    ///GENERATE NUMBER RANDOM FOR LAT AND LONG
    fun generateRandomLat(): Double {
        val minLat = -90.0
        val maxLat = 90.0

        val randomLat = Random.nextDouble(minLat, maxLat)
        return randomLat
    }

    fun generateRandomLong(): Double {
        val minLong = -180.0
        val maxLong = 180.0

        val randomLong = Random.nextDouble(minLong, maxLong)
        return randomLong
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap

        ///fix marker
        mGoogleMap?.addMarker(
            MarkerOptions()
                .position(LatLng(46.770439, 23.591423))
                .title("Marker")
        )
        val cluj = LatLng(46.770439, 23.591423)
        ///drag marker
        mGoogleMap?.addMarker(
            MarkerOptions()
                .position(LatLng(12.987, 14.345))
                .title("Draggable Marker")
                .draggable(true)
        )
        mGoogleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(cluj, 15f))

    }

//  /BLUETOOTH SECTION

        private fun isBluetoothLeSupported(context: Context): Boolean {
            return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
        }

        private fun enableBluetooth(activity: Activity):Int {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

            if (bluetoothAdapter == null) {
                // Device does not support Bluetooth
                Toast.makeText(this, "Device does  not support Bluetooth", Toast.LENGTH_SHORT)
                    .show()

            }

            if (!bluetoothAdapter.isEnabled) {
                Toast.makeText(this, "Please enable Bluetooth and retry", Toast.LENGTH_SHORT)
                    .show()
                // Bluetooth is not enabled, request the user to enable it
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

            }
            else {
                Toast.makeText(this, "Bluetooth is enable,ready to proceed!", Toast.LENGTH_SHORT)
                    .show()
                return 1
                ///DA CRESH DACA BLUETOOTH NU E ACTIVAT
                // Bluetooth is already enabled, proceed with your BLE functionality
            }
            return 0
        }
        private fun startBleDeviceDiscovery() {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            Toast.makeText (this,"Starting BLE discovery",Toast.LENGTH_SHORT).show()
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                // Bluetooth is not supported or not enabled
                return
            }

            // Clear the previous list of discovered devices before starting a new scan
            discoveredDevices.clear()

            // Start the BLE device discovery
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            bluetoothAdapter.startDiscovery()
            Toast.makeText (this,"",Toast.LENGTH_SHORT).show()
        }
        private fun stopBleDeviceDiscovery() {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                // Bluetooth is not supported or not enabled
                return
            }

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
        }
        override fun onDestroy() {
            super.onDestroy()
            unregisterReceiver(discoveryReceiver)
        }
        var bluetoothGatt: BluetoothGatt? = null


        private fun connectToDevice(device: BluetoothDevice) {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                // Bluetooth is not supported or not enabled
                return
            }

            // Connect to the device using the correct callback variable name
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED
            )
            {

                return
            }
            bluetoothGatt = device.connectGatt(this@MainActivity, false, gattCallback)
        }
        private val gattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        // Device connected, discover services (optional)
                        if (ActivityCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }
                        gatt?.discoverServices()
                    }
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        // Device disconnected, handle this event if needed
                    }
                }
            }
        }
}




