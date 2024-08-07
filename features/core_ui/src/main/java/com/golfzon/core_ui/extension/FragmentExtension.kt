package com.golfzon.core_ui.extension

import android.widget.Toast
import androidx.fragment.app.Fragment
fun Fragment.toast(msg: String, isShort: Boolean = false) {
    Toast.makeText(context, msg, if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
}