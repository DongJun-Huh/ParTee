package com.golfzon.core_ui.databinding.adapters

import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.golfzon.core_ui.R
import com.golfzon.core_ui.extension.setOnDebounceClickListener

@BindingAdapter("onDebounceClick")
fun setOnDebounceClickListener(view: View, clickListener: View.OnClickListener) {
    view.setOnDebounceClickListener {
        clickListener.onClick(it)
    }
}

@BindingAdapter("onEditorEnterAction")
fun EditText.onEditorEnterAction(f: Function1<String, Unit>?) {
    if (f == null) setOnEditorActionListener(null)
    else setOnEditorActionListener { v, actionId, event ->

        val imeAction = when (actionId) {
            EditorInfo.IME_ACTION_SEARCH,
            EditorInfo.IME_ACTION_DONE,
            EditorInfo.IME_ACTION_SEND,
            EditorInfo.IME_ACTION_GO -> true
            else -> false
        }

        val keydownEvent = event?.keyCode == KeyEvent.KEYCODE_ENTER
                && event.action == KeyEvent.ACTION_DOWN

        if (imeAction or keydownEvent)
            true.also { f(v.editableText.toString()) }
        else false
    }
}

@BindingAdapter("divider", "displayListsToString")
fun TextView.displayListsToString(divider: String? = "", lists: List<String>?) {
    if (lists == null) return
    if (lists.size == 1) {
        this.text = lists[0]
        return
    }

    var result = ""
    for ((idx, curElement) in lists.withIndex()) {
        result += if (idx == 0) "${curElement}" else " ${divider} ${curElement}"
    }
    this.text = result
}
@BindingAdapter(value = ["imageUId"], requireAll = false)
fun ImageView.loadImage(imageUId: String? = "") {
    Glide.with(this.context)
        .load("https://firebasestorage.googleapis.com/v0/b/partee-1ba05.appspot.com/o/users%2F${imageUId}?alt=media")
        .placeholder(R.drawable.background_img_golf)
        .error(R.drawable.background_img_golf)
        .into(this)
}