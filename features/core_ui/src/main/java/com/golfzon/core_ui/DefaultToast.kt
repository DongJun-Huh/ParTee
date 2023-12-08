package com.golfzon.core_ui

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.golfzon.core_ui.databinding.ItemDefaultToastBinding

object DefaultToast {
    fun createToast(
        context: Context,
        message: String,
        bottomOffset: Int = 44,
        isError: Boolean = false
    ): Toast? {
        val inflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<ItemDefaultToastBinding>(
            inflater,
            R.layout.item_default_toast,
            null,
            false
        )
        binding.toastMessage = message
        if (isError)
            binding.ivToastIcon.setImageDrawable(context.getDrawable(R.drawable.ic_x_circle))

        return Toast(context).apply {
            setGravity(Gravity.FILL_HORIZONTAL or Gravity.BOTTOM, 0, bottomOffset.Px)
            duration = Toast.LENGTH_SHORT
            view = binding.root
        }
    }
}