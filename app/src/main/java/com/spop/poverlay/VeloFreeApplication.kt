package com.spop.poverlay

import com.spop.poverlay.BLE.BleHeartRateManager
import com.spop.poverlay.DataBase.DBHelper
import com.spop.poverlay.DataBase.GlobalVariables
import com.spop.poverlay.util.IsRunningOnPeloton
import timber.log.Timber
import timber.log.Timber.*

class VeloFreeApplication : android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG || IsRunningOnPeloton) {
            Timber.plant(DebugTree())
            dbHelper = DBHelper(this)
            bleHeartRateManager = BleHeartRateManager(this)
            globalVariables = GlobalVariables(this, dbHelper)

        }
    }

    companion object {
        // Lateinit property to hold the single instance of the DBHelper
        private lateinit var dbHelper: DBHelper
        private lateinit var bleHeartRateManager: BleHeartRateManager

private  lateinit var globalVariables: GlobalVariables
        fun getDbHelper(): DBHelper {
            return dbHelper

            val heartRate = bleHeartRateManager.heartRate
        }

        fun getHeartRateManager(): BleHeartRateManager {
            return bleHeartRateManager
        }

        fun getGlobalVariables(): GlobalVariables
        {
            return globalVariables
        }

    }

}