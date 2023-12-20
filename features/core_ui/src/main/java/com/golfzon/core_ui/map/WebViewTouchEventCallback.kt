package com.golfzon.core_ui.map

import android.view.MotionEvent

interface WebViewTouchEventCallback {
    fun onEvent(event: MotionEvent?)
}