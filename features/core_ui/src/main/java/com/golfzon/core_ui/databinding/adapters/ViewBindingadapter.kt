package com.golfzon.core_ui.databinding.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.bumptech.glide.RequestManager
import com.golfzon.core_ui.ImageUploadUtil
import com.golfzon.core_ui.ImageUploadUtil.loadImageFromFirebaseStorage
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.google.android.material.imageview.ShapeableImageView
import java.text.NumberFormat
import java.util.Locale
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

@BindingAdapter(value = ["imageUId", "imageType", "requestManager"], requireAll = false)
fun ImageView.loadImage(imageUId: String? = "", imageType: ImageUploadUtil.ImageType, requestManager: RequestManager) {
    requestManager.loadImageFromFirebaseStorage(
        imageUId = imageUId ?: "",
        imageType = imageType,
        size = this.width,
        imageView = this
    )
}

@BindingAdapter(value = ["imageUId", "imageType", "requestManager"], requireAll = false)
fun ShapeableImageView.loadImage(imageUId: String? = "", imageType: ImageUploadUtil.ImageType, requestManager: RequestManager) {
    requestManager.loadImageFromFirebaseStorage(
        imageUId = imageUId ?: "",
        imageType = imageType,
        size = this.width,
        imageView = this
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
@BindingAdapter("moneyFormattedText")
fun setMoneyFormattedText(view: EditText, value: String?) {
    val cleanString = value?.replace("[,]".toRegex(), "") ?: ""
    val formatted = if (cleanString.isNotEmpty()) {
        val parsed = cleanString.toDouble()
        NumberFormat.getNumberInstance(Locale.KOREA).format(parsed)
    } else { "" }

    if (view.text.toString() != formatted) {
        view.setText(formatted)
        view.setSelection(formatted.length)
    }
}

@InverseBindingAdapter(attribute = "moneyFormattedText", event = "moneyFormattedTextAttrChanged")
fun getMoneyFormattedText(view: EditText): String {
    return view.text.toString().replace("[,]".toRegex(), "")
}

@BindingAdapter("moneyFormattedTextAttrChanged")
fun setMoneyFormattedTextWatcher(view: EditText, attrChange: InverseBindingListener) {
    view.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) { attrChange.onChange() }
    })
}