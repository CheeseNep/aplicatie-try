package com.example.harta23try
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass.Device
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import androidx.core.app.ActivityCompat
import com.example.harta23try.MainActivity
import com.example.harta23try.R

class DeviceListActivity : AppCompatActivity() {
    private val leDeviceListAdapter = MainActivity.LeDeviceListAdapter()
    private lateinit var deviceListView: ListView
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val TAG = "DeviceListActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize BluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()


        deviceListView = findViewById(R.id.deviceListView)
        deviceListView.adapter = leDeviceListAdapter


        // Initialize BluetoothScanner and start scanning
        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                if (ActivityCompat.checkSelfPermission(this@DeviceListActivity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                Log.d(TAG, "Found Bluetooth device: ${device.name} (${device.address})")
                leDeviceListAdapter.addDevice(device)
                leDeviceListAdapter.notifyDataSetChanged()
            }
        }

        // Start scanning
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val scanFilters = listOf(ScanFilter.Builder().build())

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            bluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback)
        }

        // Set item click listener
            //--->deviceListView.setOnItemClickListener { _, _, position, _ ->

            // Handle the selected device (e.g., connect to it)

    }
}
