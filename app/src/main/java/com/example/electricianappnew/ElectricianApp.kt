package com.example.electricianappnew

import android.app.Application
import android.util.Log // Add Log import
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ElectricianApp : Application() {
    private val TAG = "AppStartup" // Define log tag

    override fun onCreate() {
        Log.d(TAG, "ElectricianApp onCreate: Start")
        super.onCreate()
        // Application level setup can go here if needed later
        Log.d(TAG, "ElectricianApp onCreate: End")
    }
}
