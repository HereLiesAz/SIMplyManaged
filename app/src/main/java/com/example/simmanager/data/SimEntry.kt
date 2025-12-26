package com.example.simmanager.data

/**
 * Represents a SIM card or eSIM profile available on the device.
 */
data class SimEntry(
    val subscriptionId: Int,
    val slotIndex: Int,
    val displayName: String,
    val carrierName: String,
    val number: String,
    val isEmbedded: Boolean, // True if eSIM
    val mcc: String?,
    val mnc: String?
)
