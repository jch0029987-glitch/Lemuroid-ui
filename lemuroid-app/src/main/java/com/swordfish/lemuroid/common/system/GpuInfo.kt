package com.swordfish.lemuroid.common.system

import android.app.ActivityManager
import android.content.Context
import android.os.Build

object GpuInfo {

    fun getVendor(context: Context): String {
        return try {
            val renderer = getRenderer(context)
            when {
                renderer.contains("mali", ignoreCase = true) -> "ARM"
                renderer.contains("adreno", ignoreCase = true) -> "Qualcomm"
                renderer.contains("intel", ignoreCase = true) -> "Intel"
                else -> "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun getRenderer(context: Context): String {
        return try {
            val activityManager =
                context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val info = activityManager.deviceConfigurationInfo
            info.glEsVersion ?: "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun isVulkanSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }
}
