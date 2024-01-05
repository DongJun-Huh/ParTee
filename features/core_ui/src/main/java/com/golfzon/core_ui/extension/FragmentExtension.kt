package com.golfzon.core_ui.extension

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.golfzon.core_ui.Px
import com.golfzon.core_ui.R
import com.golfzon.core_ui.databinding.ItemDefaultToastBinding

fun Fragment.toast(msg: String, isShort: Boolean = false) {
    Toast.makeText(context, msg, if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
}

fun Fragment.toast(message: String, bottomOffset: Int = 44, isError: Boolean = false) {
    val inflater = LayoutInflater.from(requireContext())
    val binding = DataBindingUtil.inflate<ItemDefaultToastBinding>(
        inflater,
        R.layout.item_default_toast,
        null,
        false
    )
    binding.toastMessage = message
    if (isError) {
        with(binding.ivToastIcon) {
            setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_x_circle)
            )
            imageTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.red_error_60)
        }
    } else {
        with(binding.ivToastIcon) {
            setImageDrawable(
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_check_circle)
            )
            imageTintList =
                ContextCompat.getColorStateList(requireContext(), R.color.primary_A4EF69)
        }
    }
    Toast(requireContext()).apply {
        setGravity(Gravity.FILL_HORIZONTAL or Gravity.BOTTOM, 0, bottomOffset.Px)
        duration = Toast.LENGTH_SHORT
        view = binding.root
    }.show()
}