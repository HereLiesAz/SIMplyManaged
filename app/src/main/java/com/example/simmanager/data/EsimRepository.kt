package com.example.simmanager.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object EsimRepository {
    private val profiles = mutableListOf<EsimProfile>()
    private const val PREFS_NAME = "sim_manager_esim_prefs"
    private const val KEY_PROFILES = "esim_profiles_json"
    private val gson = Gson()

    fun getAllProfiles(): List<EsimProfile> {
        return profiles.toList()
    }

    fun loadProfiles(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_PROFILES, null)
        if (json != null) {
            val type = object : TypeToken<List<EsimProfile>>() {}.type
            val savedProfiles: List<EsimProfile> = gson.fromJson(json, type)
            profiles.clear()
            profiles.addAll(savedProfiles)
        }
    }

    fun addProfile(context: Context, profile: EsimProfile) {
        profiles.add(profile)
        saveProfiles(context)
    }

    fun removeProfile(context: Context, profile: EsimProfile) {
        profiles.remove(profile)
        saveProfiles(context)
    }

    private fun saveProfiles(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = gson.toJson(profiles)
        prefs.edit().putString(KEY_PROFILES, json).apply()
    }
}
