package com.example.simmanager.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter

object RuleRepository {
    private val rules = mutableListOf<Rule>()
    private const val PREFS_NAME = "sim_manager_prefs"
    private const val KEY_RULES = "rules_json"

    private val gson = GsonBuilder()
        .registerTypeAdapter(LocalTime::class.java, object : TypeAdapter<LocalTime>() {
            override fun write(out: JsonWriter, value: LocalTime?) {
                out.value(value?.format(DateTimeFormatter.ISO_LOCAL_TIME))
            }
            override fun read(input: JsonReader): LocalTime {
                return LocalTime.parse(input.nextString(), DateTimeFormatter.ISO_LOCAL_TIME)
            }
        })
        .create()

    // Load initial sample if empty, but try to load from prefs first in a real app init.
    // For simplicity in this structure, we load lazily or on demand.

    fun getAllRules(): List<Rule> {
        return rules.toList()
    }

    fun loadRules(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_RULES, null)
        if (json != null) {
            val type = object : TypeToken<List<Rule>>() {}.type
            val savedRules: List<Rule> = gson.fromJson(json, type)
            rules.clear()
            rules.addAll(savedRules)
        } else {
             // Add sample defaults only if nothing saved
             if (rules.isEmpty()) {
                 rules.add(
                    Rule(
                        name = "Work Hours Data",
                        type = RuleType.TIME_BASED,
                        condition = RuleCondition.TimeCondition(
                            startTime = java.time.LocalTime.of(9, 0),
                            endTime = java.time.LocalTime.of(17, 0),
                            days = java.time.DayOfWeek.values().toSet()
                        ),
                        action = Action.SetDefaultData(1)
                    )
                )
             }
        }
    }

    fun addRule(context: Context, rule: Rule) {
        rules.add(rule)
        saveRules(context)
    }

    private fun saveRules(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(rules)
        prefs.edit().putString(KEY_RULES, json).apply()
    }
}
