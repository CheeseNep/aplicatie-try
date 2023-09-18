package com.example.harta23try

import java.util.UUID

object SampleGattAttributes {
    val HEART_RATE_MEASUREMENT: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")
    val CLIENT_CHARACTERISTIC_CONFIG: String = "00002902-0000-1000-8000-00805f9b34fb" // Replace with the correct UUID as a string
    // Add other UUIDs as needed
    private val attributes: HashMap<UUID, String> = HashMap()

    init {
        // Populate the attributes HashMap with UUID-to-name mappings
        attributes[HEART_RATE_MEASUREMENT] = "Heart Rate Measurement"
        // Add other mappings here
    }

    fun lookup(uuid: UUID, defaultName: String): String {
        val name = attributes[uuid]
        return name ?: defaultName
    }
}