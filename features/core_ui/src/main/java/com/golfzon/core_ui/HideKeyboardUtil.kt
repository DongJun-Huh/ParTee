package com.golfzon.core_ui

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

object KeyBoardUtil {
    fun EditText.showKeyboard(context: Context) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, 0)
    }

    fun EditText.hideKeyboard(context: Context) {
        this.clearFocus()
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
    }
}