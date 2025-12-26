package com.example.simmanager.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.simmanager.R
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.simmanager.service.RuleWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Schedule the background worker to run every 15 minutes
        val ruleWorkRequest = PeriodicWorkRequestBuilder<RuleWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(this).enqueue(ruleWorkRequest)

        // In a real app, we would load the default fragment here
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DashboardFragment())
                .commit()
        }

        findViewById<android.widget.Button>(R.id.nav_dashboard).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DashboardFragment())
                .commit()
        }

        findViewById<android.widget.Button>(R.id.nav_rules).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RulesFragment())
                .commit()
        }

        findViewById<android.widget.Button>(R.id.nav_esim).setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, EsimBackupFragment())
                .commit()
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        // 1. Request Runtime Permissions
        val permissions = arrayOf(
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.READ_CALL_LOG,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.ACCESS_FINE_LOCATION // For Wifi SSID
        )

        val missingPermissions = permissions.filter {
            androidx.core.content.ContextCompat.checkSelfPermission(this, it) != android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            androidx.core.app.ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), 100)
        }

        // 2. Check Special Permission: PACKAGE_USAGE_STATS
        val appOps = getSystemService(android.content.Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        val mode = appOps.checkOpNoThrow(
            android.app.AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            packageName
        )

        if (mode != android.app.AppOpsManager.MODE_ALLOWED) {
            // Permission is not granted.
            // In a real app, we would show a dialog explaining why we need it,
            // and then direct the user to Settings via Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).
            android.util.Log.w("SimManager", "PACKAGE_USAGE_STATS permission not granted. Usage tracking will fail.")

            // Optionally prompt user to settings
            // startActivity(android.content.Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }
}
