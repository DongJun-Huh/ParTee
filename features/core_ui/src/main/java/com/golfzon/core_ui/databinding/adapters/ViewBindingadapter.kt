package com.golfzon.core_ui.databinding.adapters

import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.golfzon.core_ui.ImageUploadUtil
import com.golfzon.core_ui.ImageUploadUtil.loadImageFromFirebaseStorage
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import kotlin.math.roundToInt

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
@BindingAdapter(value = ["imageUId", "imageType"], requireAll = false)
fun ImageView.loadImage(imageUId: String? = "", imageType: ImageUploadUtil.ImageType) {
    this.loadImageFromFirebaseStorage(
        imageUId = imageUId?: "",
        imageType = imageType
    )
}

@BindingAdapter(value = ["dividend", "divisor", "prefix", "postfix"], requireAll = false)
fun TextView.calculateAverageToInt(
    dividend: Int,
    divisor: Int,
    prefix: String = "",
    postfix: String = ""
) {
    if (divisor == 0 || dividend == 0) {
        this.text = "${prefix}0${postfix}"
        return
    }
    this.text = "${prefix}${(dividend.toDouble() / divisor.toDouble()).roundToInt()}${postfix}"
}