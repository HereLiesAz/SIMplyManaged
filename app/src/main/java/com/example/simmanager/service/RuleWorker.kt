package com.example.simmanager.service

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.simmanager.data.Rule
import com.example.simmanager.data.RuleCondition
import com.example.simmanager.data.Action
import com.example.simmanager.logic.SimController
import com.example.simmanager.logic.UsageMonitor
import java.time.LocalTime
import java.time.DayOfWeek
import java.time.LocalDate

class RuleWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    private val simController = SimController(appContext)
    private val usageMonitor = UsageMonitor(appContext)

    override fun doWork(): Result {
        // Ensure rules are loaded
        com.example.simmanager.data.RuleRepository.loadRules(applicationContext)
        // Fetch rules from the repository
        val rules = com.example.simmanager.data.RuleRepository.getAllRules()

        for (rule in rules) {
            if (rule.isEnabled && evaluateCondition(rule.condition)) {
                executeAction(rule.action)
            }
        }

        return Result.success()
    }

    private fun evaluateCondition(condition: RuleCondition): Boolean {
        return when (condition) {
            is RuleCondition.TimeCondition -> {
                val now = LocalTime.now()
                val today = LocalDate.now().dayOfWeek
                condition.days.contains(today) && now.isAfter(condition.startTime) && now.isBefore(condition.endTime)
            }
            is RuleCondition.UsageCondition -> {
                // Check usage for the current month
                val now = java.util.Calendar.getInstance()
                val startOfMonth = java.util.Calendar.getInstance()
                startOfMonth.set(java.util.Calendar.DAY_OF_MONTH, 1)

                when (condition.metric) {
                    com.example.simmanager.data.UsageMetric.DATA_LIMIT -> {
                         val usage = usageMonitor.getDataUsage(condition.subscriptionId.toString(), startOfMonth.timeInMillis, now.timeInMillis)
                         usage > condition.threshold
                    }
                    com.example.simmanager.data.UsageMetric.CALL_COUNT -> {
                        val count = usageMonitor.getCallCount(startOfMonth.timeInMillis)
                        count > condition.threshold
                    }
                    com.example.simmanager.data.UsageMetric.SMS_COUNT -> {
                        val count = usageMonitor.getSmsCount(startOfMonth.timeInMillis)
                        count > condition.threshold
                    }
                }
            }
            is RuleCondition.WifiCondition -> {
                val currentSsid = usageMonitor.getCurrentWifiSsid()
                val isConnected = currentSsid == condition.ssid
                isConnected == condition.isConnected
            }
        }
    }

    private fun executeAction(action: Action) {
        when (action) {
            is Action.SetDefaultData -> simController.setDefaultDataSim(action.subscriptionId)
            is Action.SetDefaultVoice -> simController.setDefaultVoiceSim(action.subscriptionId)
            is Action.SetDefaultSms -> simController.setDefaultSmsSim(action.subscriptionId)
            is Action.EnableHotspot -> {
                com.example.simmanager.logic.HotspotManager(applicationContext).setHotspotEnabled(action.enable) { success ->
                    if (!success) {
                        // Log failure
                    }
                }
            }
            is Action.SwitchProfile -> {
                action.voiceSubId?.let { simController.setDefaultVoiceSim(it) }
                action.smsSubId?.let { simController.setDefaultSmsSim(it) }
                action.dataSubId?.let { simController.setDefaultDataSim(it) }
            }
        }
    }
}
