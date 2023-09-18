package com.example.harta23try

import android.app.Service
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Intent
import android.os.IBinder

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

            private const val STATE_DISCONNECTED = 0
            private const val STATE_CONNECTED = 2

        }

    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}