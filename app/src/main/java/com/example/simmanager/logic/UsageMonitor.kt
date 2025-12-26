package com.example.simmanager.logic

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager
import android.os.RemoteException
import java.util.Calendar

class UsageMonitor(private val context: Context) {

    private val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

    /**
     * Returns the number of bytes used by the given subscriber ID since the start of the billing cycle (assumed monthly).
     * @param subscriptionId The Android Subscription ID (integer) as a String.
     */
    fun getDataUsage(subscriptionId: String, startTime: Long, endTime: Long): Long {
        try {
            // We need the Subscriber Identity (IMSI), not the Subscription ID (int).
            // Since we have READ_PHONE_STATE, we can try to resolve it.
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            // Create a TelephonyManager pinned to the specific subscription
            val subTm = tm.createForSubscriptionId(subscriptionId.toInt())
            val imsi = subTm.subscriberId

            // Note: getSubscriberId() might be empty or require elevated permissions.
            // If empty, passing null sometimes queries the default/active data interface stats.
            val identity = if (!imsi.isNullOrEmpty()) imsi else null

            val bucket = networkStatsManager.querySummaryForDevice(
                ConnectivityManager.TYPE_MOBILE,
                identity,
                startTime,
                endTime
            )
            return bucket.rxBytes + bucket.txBytes
        } catch (e: Exception) { // RemoteException or SecurityException
            // This often fails if PACKAGE_USAGE_STATS permission is missing
            android.util.Log.e("UsageMonitor", "Failed to get data usage: ${e.message}")
            return 0
        }
    }

    // Note: Tracking CALL_COUNT and SMS_COUNT usually requires reading CallLog and Sms ContentProviders,
    // which requires READ_CALL_LOG and READ_SMS permissions.
    // For simplicity, we'll placeholder this.

    fun getCallCount(startTime: Long): Int {
        if (androidx.core.app.ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALL_LOG) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return 0
        }

        var count = 0
        val cursor = context.contentResolver.query(
            android.provider.CallLog.Calls.CONTENT_URI,
            arrayOf(android.provider.CallLog.Calls._ID),
            "${android.provider.CallLog.Calls.DATE} >= ?",
            arrayOf(startTime.toString()),
            null
        )

        cursor?.use {
            count = it.count
        }
        return count
    }

    fun getSmsCount(startTime: Long): Int {
        if (androidx.core.app.ActivityCompat.checkSelfPermission(context, android.Manifest.permission.READ_SMS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return 0
        }

        var count = 0
        val cursor = context.contentResolver.query(
            android.provider.Telephony.Sms.CONTENT_URI,
            arrayOf(android.provider.Telephony.Sms._ID),
            "${android.provider.Telephony.Sms.DATE} >= ?",
            arrayOf(startTime.toString()),
            null
        )

        cursor?.use {
            count = it.count
        }
        return count
    }

    /**
     * Gets the current Wifi SSID.
     * Note: Requires ACCESS_FINE_LOCATION or ACCESS_WIFI_STATE depending on Android version.
     */
    fun getCurrentWifiSsid(): String? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as android.net.wifi.WifiManager
        val info = wifiManager.connectionInfo
        return if (info != null && info.networkId != -1) {
            info.ssid.replace("\"", "")
        } else {
            null
        }
    }
}
