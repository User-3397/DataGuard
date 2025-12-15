package com.example.user_3301

import android.content.Context
import android.os.BatteryManager
import android.content.Intent
import android.content.IntentFilter

object BatteryHelper {
    fun getBatteryLevel(context: Context): Int {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }
}
