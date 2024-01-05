package com.golfzon.core_ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import com.golfzon.core_ui.DeviceSizeUtil.getDeviceWidthSize

object DialogUtil {
    fun setDialogRadius(dialog: Dialog, dialogGravity: Int = Gravity.CENTER) {
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog!!.window?.requestFeature(Window.FEATURE_NO_TITLE)
        dialog!!.window?.setGravity(dialogGravity)
    }

    fun resizeDialogFragment(context: Context, dialog: Dialog, dialogSizeRatio: Float = 0.9f) {
        val params: ViewGroup.LayoutParams? = dialog?.window?.attributes
        val deviceWidth = getDeviceWidthSize(context)
        params?.width = (deviceWidth * dialogSizeRatio).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

}