package com.golfzon.core_ui.databinding.adapters

import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.golfzon.core_ui.extension.setOnDebounceClickListener

@BindingAdapter("onDebounceClick")
fun setOnDebounceClickListener(view: View, clickListener: View.OnClickListener) {
    view.setOnDebounceClickListener {
        clickListener.onClick(it)
    }
}

@BindingAdapter("imageUrl", "placeholder")
fun loadImage(imageView: ImageView, url: String, placeholder: Drawable) {
    Glide.with(imageView.context)
        .load(url)
        .placeholder(placeholder)
        .error(placeholder)
        .into(imageView)
}