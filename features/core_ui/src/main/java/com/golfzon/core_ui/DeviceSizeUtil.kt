package com.golfzon.core_ui

import android.content.Context
import android.graphics.Point
import android.os.Build
import android.view.WindowManager

object DeviceSizeUtil {
    fun getDeviceWidthSize(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT < 30) {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            return size.x
        } else {
            val rect = windowManager.currentWindowMetrics.bounds
            return rect.width()
        }
    }
    fun getDeviceHeightSize(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        if (Build.VERSION.SDK_INT < 30) {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)
            return size.y
        } else {
            val rect = windowManager.currentWindowMetrics.bounds
            return rect.height()
        }
    }
}