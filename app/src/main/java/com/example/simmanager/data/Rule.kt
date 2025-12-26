package com.example.simmanager.data

import java.time.DayOfWeek
import java.time.LocalTime
import java.util.UUID

/**
 * A rule that dictates when to switch SIM settings.
 */
data class Rule(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: RuleType,
    val condition: RuleCondition,
    val action: Action,
    var isEnabled: Boolean = true
)

enum class RuleType {
    TIME_BASED,
    USAGE_BASED,
    WIFI_BASED
}

sealed class RuleCondition {
    data class TimeCondition(
        val startTime: LocalTime,
        val endTime: LocalTime,
        val days: Set<DayOfWeek>
    ) : RuleCondition()

    data class UsageCondition(
        val metric: UsageMetric,
        val threshold: Long, // e.g., bytes, count
        val subscriptionId: Int
    ) : RuleCondition()

    data class WifiCondition(
        val ssid: String,
        val isConnected: Boolean // Trigger when connected or disconnected
    ) : RuleCondition()
}

enum class UsageMetric {
    DATA_LIMIT,
    CALL_COUNT,
    SMS_COUNT
}
