package com.example.simmanager.data

/**
 * The action to take when a rule is triggered.
 */
sealed class Action {
    data class SetDefaultVoice(val subscriptionId: Int) : Action()
    data class SetDefaultSms(val subscriptionId: Int) : Action()
    data class SetDefaultData(val subscriptionId: Int) : Action()
    data class EnableHotspot(val enable: Boolean) : Action()

    // Composite action to set multiple things at once
    data class SwitchProfile(
        val voiceSubId: Int?,
        val smsSubId: Int?,
        val dataSubId: Int?
    ) : Action()
}
