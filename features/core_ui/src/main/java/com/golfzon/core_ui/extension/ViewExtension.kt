package com.golfzon.core_ui.extension

import android.view.View
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView

fun View.setOnDebounceClickListener(interval: Long = 1000L, action: (View) -> Unit) {
    val debounceClickListener = object : View.OnClickListener {
        private var lastClickedMillis = 0L

        override fun onClick(view: View) {
            val now = System.currentTimeMillis()
            if (now - lastClickedMillis < interval) {
                return
            }
            lastClickedMillis = now
            action.invoke(view)
        }
    }
    setOnClickListener(debounceClickListener)
}

fun View.addRecyclerViewLastItemMarginBottom(bottomMargin: Int) {
    this.updateLayoutParams<RecyclerView.LayoutParams> {
        this.bottomMargin = bottomMargin
    }
}