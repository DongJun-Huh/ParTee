package com.golfzon.core_ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.annotation.LayoutRes
import com.golfzon.core_ui.databinding.ItemDeafultSpinnerBinding

class SpinnerAdapter(
    context: Context,
    @LayoutRes private val resId: Int,
    private val values: MutableList<String>
) : ArrayAdapter<String>(context, resId, values) {
    override fun getCount(): Int = values.size

    override fun getItem(position: Int): String? = values[position]

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding =
            ItemDeafultSpinnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val model = values[position]
        try {
            with(binding) {
                ivSpinnerArrow.visibility = View.VISIBLE
                with(tvSpinnerSort) {
                    text = model
//                    setTextColor(ContextCompat.getColor(context, R.color.black_1C1D22))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding =
            ItemDeafultSpinnerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val model = values[position]
        try {
            binding.tvSpinnerSort.text = model
            if (position == 0) {
                binding.ivSpinnerArrow.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return binding.root
    }
}