package com.golfzon.core_ui.extension

import android.app.Activity
import android.widget.Toast

fun Activity.toast(msg: String, isShort: Boolean = true) {
    Toast.makeText(this, msg, if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
}