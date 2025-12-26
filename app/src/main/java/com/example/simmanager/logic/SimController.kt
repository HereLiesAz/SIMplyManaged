package com.example.simmanager.logic

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SubscriptionManager
import android.telephony.SubscriptionInfo
import androidx.core.app.ActivityCompat
import com.example.simmanager.data.SimEntry
import java.util.concurrent.Executors

/**
 * Handles interactions with the Android SubscriptionManager.
 * Note: Changing default subscriptions programmatically is restricted on modern Android.
 * It typically requires the app to be a system app or have Carrier Privileges.
 * For a standard app, we might only be able to prompt the user or show a notification.
 * This class attempts to use the APIs as if permissions are granted.
 */
class SimController(private val context: Context) {

    private val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    private val executor = Executors.newSingleThreadExecutor()

    fun getAvailableSims(): List<SimEntry> {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return emptyList()
        }

        val subs: List<SubscriptionInfo> = subscriptionManager.activeSubscriptionInfoList ?: emptyList()
        return subs.map { sub ->
            SimEntry(
                subscriptionId = sub.subscriptionId,
                slotIndex = sub.simSlotIndex,
                displayName = sub.displayName.toString(),
                carrierName = sub.carrierName.toString(),
                number = sub.number ?: "",
                isEmbedded = sub.isEmbedded,
                mcc = sub.mccString,
                mnc = sub.mncString
            )
        }
    }

    fun setDefaultDataSim(subscriptionId: Int) {
        // First try Reflection (System App approach)
        try {
            val method = subscriptionManager.javaClass.getMethod("setDefaultDataSubId", Int::class.javaPrimitiveType)
            method.invoke(subscriptionManager, subscriptionId)
        } catch (e: Exception) {
            // If failed, try Root command
            if (RootHelper.isRootAvailable()) {
                // 'cmd phone' is available on Android 10+.
                // 'settings put global multi_sim_data_call <subId>' is older method.
                // We'll try the 'cmd' approach first as it's cleaner for modern Android.
                val success = RootHelper.execute("cmd phone data set-default-sub-id $subscriptionId")
                if (!success) {
                    // Fallback to settings put
                    RootHelper.execute("settings put global multi_sim_data_call $subscriptionId")
                }
            } else {
                e.printStackTrace()
            }
        }
    }

    fun setDefaultVoiceSim(subscriptionId: Int) {
        try {
            val method = subscriptionManager.javaClass.getMethod("setDefaultVoiceSubId", Int::class.javaPrimitiveType)
            method.invoke(subscriptionManager, subscriptionId)
        } catch (e: Exception) {
             if (RootHelper.isRootAvailable()) {
                 // Try settings put fallback
                 RootHelper.execute("settings put global multi_sim_voice_call $subscriptionId")
             }
        }
    }

    fun setDefaultSmsSim(subscriptionId: Int) {
        try {
            val method = subscriptionManager.javaClass.getMethod("setDefaultSmsSubId", Int::class.javaPrimitiveType)
            method.invoke(subscriptionManager, subscriptionId)
        } catch (e: Exception) {
             if (RootHelper.isRootAvailable()) {
                 RootHelper.execute("settings put global multi_sim_sms $subscriptionId")
             }
        }
    }
}
