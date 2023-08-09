package com.example.harta23try

import com.example.harta23try.DeviceListActivity
import android.Manifest
import com.example.harta23try.SampleGattAttributes
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.STATE_DISCONNECTED
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.nfc.NfcAdapter.EXTRA_DATA
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.harta23try.MainActivity.BluetoothService.Companion.ACTION_DATA_AVAILABLE
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.UUID
import java.util.Vector
import kotlin.random.Random

///  data 8 /14 /23 lucrez doar pe mapa

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val REQUEST_ENABLE_BT = 1 // Define this constant
    private lateinit var scanCallback: ScanCallback
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val discoveredDevices: MutableList<BluetoothDevice> = mutableListOf()
    private lateinit var deviceListAdapter: ArrayAdapter<String>
    private val REQUEST_BT_CONNECT_PERMISSION = 1 // You can choose any value

    private val leDeviceListAdapter = LeDeviceListAdapter()
    private lateinit var deviceListView: ListView




    private val TAG = "BluetoothLeServices" ///trebuie sa fie const val


    private var mGoogleMap: GoogleMap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        deviceListView = findViewById(R.id.deviceListView)
        deviceListView.adapter = leDeviceListAdapter




        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        // Create an instance of your LeDeviceListAdapter

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, request both permissions from the user
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                REQUEST_BT_CONNECT_PERMISSION
            )
        }





        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }




        if (!bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Please enable Bluetooth and retry", Toast.LENGTH_SHORT).show()
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            return
        }



        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        val mapFragment = supportFragmentManager
            .findFragmentById((R.id.mapFragment)) as SupportMapFragment
        mapFragment.getMapAsync(this)
        val myButtonBluetooth = findViewById<Button>(R.id.CheckForBluetooth)
        val myButtonMap = findViewById<Button>(R.id.CenterMap)  ///Actualy does BLE list

        ///BUTTON FOR BLUETOOTH
        myButtonBluetooth.setOnClickListener {
            // Example: Show a toast message
//            Toast.makeText(this, "Button Clicked!", Toast.LENGTH_SHORT).show()
            var isworking = isBluetoothLeSupported(this)
            if (isworking==true) {
                Toast.makeText(this, "BLE works", Toast.LENGTH_SHORT).show()
                if(enableBluetooth(this)==1){
                    ///startBLEdiscovery

                }

            }
            else
                Toast.makeText(this, "BLE doesnt work", Toast.LENGTH_SHORT).show()
            // Example: Navigate to another activity
            // val intent = Intent(this, AnotherActivity::class.java)
            // startActivity(intent)
        }
        ///Butun care da center pe pozitia actuala
        myButtonMap.setOnClickListener{
            val intent = Intent(this, DeviceListActivity::class.java)
            startActivity(intent)
        }



    }
    ///cere permisiuni pentru folosire Bluetooth (conectare)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BT_CONNECT_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with Bluetooth scanning
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
                Toast.makeText(
                    this,
                    "Bluetooth permission denied. Cannot scan for devices.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    ///parte legate de mapa (functioneaza bine)
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


    ///////
    ///BLUETOOTH SECTION

    private fun isBluetoothLeSupported(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    private fun enableBluetooth(activity: Activity):Int {


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

        }
        return 0
    }

    ///BLE SECTION:::

    class LeDeviceListAdapter : BaseAdapter() {
        private val devices: MutableList<BluetoothDevice> = mutableListOf()

        fun addDevice(device: BluetoothDevice) {
            if (!devices.contains(device)) {
                devices.add(device)
            }
        }

        override fun getCount(): Int {
            return devices.size
        }

        override fun getItem(position: Int): Any {
            return devices[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val device = getItem(position) as BluetoothDevice
            val view = convertView ?: LayoutInflater.from(parent?.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)

            val textView = view.findViewById<TextView>(android.R.id.text1)

            val context = parent?.context // Get the context from parent
            if (context != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return view
                }
            }

            textView.text = device.name ?: "Unknown Device"

            return view
        }
    }
    class BluetoothService : Service() {  ///clasa asta poate sa fie pusa in main?!
        private fun broadcastUpdate(action: String) {
            val intent = Intent(action)
            sendBroadcast(intent)
        }

        private var connectionState = STATE_DISCONNECTED
        private val bluetoothGattCallback: BluetoothGattCallback =
            object : BluetoothGattCallback() {
                override fun onConnectionStateChange(
                    gatt: BluetoothGatt,
                    status: Int,
                    newState: Int
                ) {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        // successfully connected to the GATT Server
                        connectionState = STATE_CONNECTED
                        broadcastUpdate(ACTION_GATT_CONNECTED)
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        // disconnected from the GATT Server
                        connectionState = STATE_DISCONNECTED
                        broadcastUpdate(ACTION_GATT_DISCONNECTED)
                    }
                }
            }
        companion object {
            const val ACTION_GATT_CONNECTED =
                "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
            const val ACTION_GATT_DISCONNECTED =
                "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
            const val TAG = "BluetoothService" // Define TAG here
            private const val STATE_DISCONNECTED = 0
            private const val STATE_CONNECTED = 2
            const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE" // Define ACTION_DATA_AVAILABLE here
        }
        override fun onBind(p0: Intent?): IBinder? {
            TODO("Not yet implemented")
        }
    }

    private var scanning = false
    private val handler = Handler()
    var bluetoothGatt: BluetoothGatt? = null

    private val SCAN_PERIOD: Long = 10000
    private fun scanLeDevice() {
        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        if (!scanning) {
            handler.postDelayed({
                scanning = false
                val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    return@postDelayed
                }
                bluetoothLeScanner.stopScan(scanCallback)
                // Update UI or perform other actions as needed
            }, SCAN_PERIOD)
            scanning = true
            val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
            bluetoothLeScanner.startScan(scanCallback)
            // Update UI or perform other actions as needed
        }
    }



    ///var bluetoothGatt: BluetoothGatt? = null???
    class BluetoothLeService : Service() {
        private val binder = LocalBinder()
        private var bluetoothAdapter: BluetoothAdapter? = null
        private var bluetoothGatt: BluetoothGatt? = null
        ///pe moment este cod exemplu
        private fun broadcastUpdateWithData(action: String, characteristic: BluetoothGattCharacteristic) {
            val intent = Intent(action)
            // This is special handling for the Heart Rate Measurement profile. Data
            // parsing is carried out as per profile specifications.
            when (characteristic.uuid) {
                SampleGattAttributes.HEART_RATE_MEASUREMENT -> {
                    val flag = characteristic.properties
                    val format = when (flag and 0x01) {
                        0x01 -> {
                            Log.d(TAG, "Heart rate format UINT16.")
                            BluetoothGattCharacteristic.FORMAT_UINT16
                        }
                        else -> {
                            Log.d(TAG, "Heart rate format UINT8.")
                            BluetoothGattCharacteristic.FORMAT_UINT8
                        }
                    }
                    val heartRate = characteristic.getIntValue(format, 1)
                    Log.d(TAG, String.format("Received heart rate: %d", heartRate))
                    intent.putExtra(EXTRA_DATA, (heartRate).toString())
                }
                else -> {
                    // For all other profiles, writes the data formatted in HEX.
                    val data: ByteArray? = characteristic.value
                    if (data?.isNotEmpty() == true) {
                        val hexString: String = data.joinToString(separator = " ") {
                            String.format("%02X", it)
                        }
                        intent.putExtra(EXTRA_DATA, "$data\n$hexString")
                    }
                }
            }
            sendBroadcast(intent)
        }

        fun initialize(): Boolean {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null) {
                Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
                return false
            }
            return true
        }
        override fun onBind(intent: Intent): IBinder? {
            return binder
        }
        inner class LocalBinder : Binder() {
            fun getService() : BluetoothLeService {
                return this@BluetoothLeService
            }
        }
        fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
            bluetoothGatt?.let { gatt ->
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                gatt.readCharacteristic(characteristic)
            } ?: run {
                Log.w(TAG, "BluetoothGatt not initialized")
                return
            }
        }
        fun connect(address: String, context: Context): Boolean {
            bluetoothAdapter?.let { adapter ->
                try {
                    val device = adapter.getRemoteDevice(address)
                    // connect to the GATT server on the device
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return false
                    }
                    bluetoothGatt = device.connectGatt(context, false, bluetoothGattCallback)
                    return true
                } catch (exception: IllegalArgumentException) {
                    Log.w(TAG, "Device not found with the provided address. Unable to connect.")
                    return false
                }
            } ?: run {
                Log.w(TAG, "BluetoothAdapter not initialized")
                return false
            }
        }


        private var connectionState = STATE_DISCONNECTED  ///VERIFICARE IMPORT LA STATE_DISCONNECTED
        private fun broadcastUpdate(action: String) {
            val intent = Intent(action)
            sendBroadcast(intent)
        }
        private val bluetoothGattCallback = object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // successfully connected to the GATT Server
                    broadcastUpdate(ACTION_GATT_CONNECTED)
                    connectionState = STATE_CONNECTED
                    // Capture the context from the outer class
                    val context = applicationContext
                    // Attempts to discover services after successful connection.
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return
                    }

                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return
                    }
                    bluetoothGatt?.discoverServices()
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // disconnected from the GATT Server
                    broadcastUpdate(ACTION_GATT_DISCONNECTED)
                    connectionState = STATE_DISCONNECTED
                }
            }



            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
                } else {
                    Log.w(BluetoothService.TAG, "onServicesDiscovered received: $status")

                }
            }

            override fun onCharacteristicRead(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    broadcastUpdateWithData(BluetoothService.ACTION_DATA_AVAILABLE, characteristic)
                }
            }
            override fun onCharacteristicChanged(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic
            ) {
                broadcastUpdateWithData(ACTION_DATA_AVAILABLE, characteristic)
            }
        }

        fun getSupportedGattServices(): List<BluetoothGattService?>? {
            return bluetoothGatt?.services
        }


        override fun onUnbind(intent: Intent?): Boolean {
            close()
            return super.onUnbind(intent)
        }

        private fun close() {
            bluetoothGatt?.let { gatt ->
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                gatt.close()
                bluetoothGatt = null
            }
        }
        companion object {
            const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
            const val ACTION_GATT_DISCONNECTED =
                "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
            const val ACTION_GATT_SERVICES_DISCOVERED =
                "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"

            private const val STATE_DISCONNECTED = 0
            private const val STATE_CONNECTED = 2
        }
        fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic, enabled: Boolean) {
            bluetoothGatt?.let { gatt ->
                gatt.setCharacteristicNotification(characteristic, enabled)

                // This is specific to Heart Rate Measurement.
                if (SampleGattAttributes.HEART_RATE_MEASUREMENT == characteristic.uuid) {
                    val descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG))
                    descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return
                    }
                    gatt.writeDescriptor(descriptor)
                }
            } ?: run {
                Log.w(BluetoothService.TAG, "BluetoothGatt not initialized")
            }
        }


    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////



    class DeviceControlActivity : AppCompatActivity() {
        private var mGattCharacteristics: MutableList<BluetoothGattCharacteristic>? = null
        private var bluetoothService : BluetoothLeService? = null
        companion object {
            private const val LIST_NAME = "Name"
            private const val LIST_UUID = "UUID_KEY"
        }


        // Define TAG constant for logging


        // Code to manage Service lifecycle.
        private val serviceConnection: ServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(
                componentName: ComponentName,
                service: IBinder
            ) {
                bluetoothService = (service as BluetoothLeService.LocalBinder).getService()
                bluetoothService?.let { bluetooth ->
                    if (!bluetooth.initialize()) {
                        Log.e(TAG, "Unable to initialize Bluetooth")
                        finish()
                    }
                    // call functions on service to check connection and connect to devices
                }
                // perform device connection
            }
            override fun onServiceDisconnected(componentName: ComponentName) {
                bluetoothService = null
            }



        }
        private fun updateConnectionState(resourceId: Int) {
                val connectionStateText = when (resourceId) {
                    R.string.connected -> "Connected"
                    R.string.disconnected -> "Disconnected"
                    else -> "Unknown"
                }
                // You can use connectionStateText as needed, for example, to update UI elements.
                // Here, we'll just log it.
                Log.d(TAG, "Connection State: $connectionStateText")
        }

        private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                var connected = false
                when (intent.action) {
                    BluetoothLeService.ACTION_GATT_CONNECTED -> {
                        connected = true
                        updateConnectionState(R.string.connected)
                    }
                    BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                        connected = false
                        updateConnectionState(R.string.disconnected)
                    }
                    BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED -> {
                        val supportedGattServices = bluetoothService?.getSupportedGattServices()
                        if (supportedGattServices != null) {
                            val nonNullableGattServices = supportedGattServices.filterNotNull()
                            displayGattServices(nonNullableGattServices)
                        }
                    }
                }
            }
        }



        private val deviceAddress: String = "00:11:22:33:44:55" // Replace with the actual Bluetooth device address

        override fun onResume() {
            super.onResume()
            registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
            if (bluetoothService != null) {
                val result = bluetoothService!!.connect(deviceAddress, this)
                Log.d(TAG, "Connect request result=$result")
            }
        }

        override fun onPause() {
            super.onPause()
            unregisterReceiver(gattUpdateReceiver)
        }
        private fun makeGattUpdateIntentFilter(): IntentFilter? {
            return IntentFilter().apply {
                addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
                addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
            }
        }



        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)///ar putea fi aici gatt_services_characteristics
            ///nu functioneaza MainActivity deoarece suntem in MainActivity_Backup

            val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
            bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }

        private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
            if (gattServices == null) return
            var uuid: String?
            val unknownServiceString: String = resources.getString(R.string.unknown_service)
            val unknownCharaString: String = resources.getString(R.string.unknown_characteristic)
            val gattServiceData: MutableList<HashMap<String, String>> = mutableListOf()
            val gattCharacteristicData: MutableList<ArrayList<HashMap<String, String>>> =
                mutableListOf()
            mGattCharacteristics = mutableListOf()

            // Loops through available GATT Services.
            gattServices.forEach { gattService ->
                val currentServiceData = HashMap<String, String>()
                uuid = gattService.uuid.toString()
                val serviceUUID = UUID.fromString(uuid) // Convert the string to UUID
                currentServiceData[LIST_NAME] = SampleGattAttributes.lookup(serviceUUID, unknownServiceString)
                currentServiceData[LIST_UUID] = uuid ?: "Unknown UUID"
                gattServiceData += currentServiceData

                val gattCharacteristicGroupData: ArrayList<HashMap<String, String>> = arrayListOf()
                val gattCharacteristics = gattService.characteristics
                val charas: MutableList<BluetoothGattCharacteristic> = mutableListOf()

                // Loops through available Characteristics.
                gattCharacteristics.forEach { gattCharacteristic ->
                    charas += gattCharacteristic
                    val currentCharaData: HashMap<String, String> = hashMapOf()
                    uuid = gattCharacteristic.uuid.toString()
                    val charaUUID = UUID.fromString(uuid) // Convert the string to UUID
                    currentCharaData[LIST_NAME] = SampleGattAttributes.lookup(charaUUID, unknownCharaString)
                    currentCharaData[LIST_UUID] = uuid ?: "Unknown UUID"
                    gattCharacteristicGroupData += currentCharaData
                }
                val tempGattCharacteristics = mGattCharacteristics?.toMutableList() ?: mutableListOf()
                tempGattCharacteristics.addAll(charas)
                mGattCharacteristics = tempGattCharacteristics
                gattCharacteristicData += gattCharacteristicGroupData
            }

        }




    }
    fun connect(address: String): Boolean {
        if (bluetoothAdapter != null) {
            try {
                val device = bluetoothAdapter!!.getRemoteDevice(address)
                // Connect to the GATT server on the device
                return true
            } catch (exception: IllegalArgumentException) {
                Log.w(TAG, "Device not found with provided address.")
                return false
            }
        } else {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return false
        }
    }











}