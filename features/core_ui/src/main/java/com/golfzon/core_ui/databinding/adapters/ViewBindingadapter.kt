package com.golfzon.core_ui.databinding.adapters

import android.view.View
import androidx.databinding.BindingAdapter
import com.golfzon.core_ui.extension.setOnDebounceClickListener

@BindingAdapter("onDebounceClick")
fun setOnDebounceClickListener(view: View, clickListener: View.OnClickListener) {
    view.setOnDebounceClickListener {
        clickListener.onClick(it)
    }
}