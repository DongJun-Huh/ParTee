package com.golfzon.core_ui.map

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.cardview.widget.CardView

class CustomWebViewContainer : CardView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var callback: WebViewTouchEventCallback? = null
    fun setTouchEventCallback(callback: WebViewTouchEventCallback?) {
        this.callback = callback
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (callback != null && event.action == KeyEvent.ACTION_UP) {
            callback!!.onEvent(event)
            return true
        }

        return true
    }
}