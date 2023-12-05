package com.golfzon.group

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.golfzon.core_ui.autoCleared
import com.golfzon.group.databinding.FragmentGroupHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupHomeFragment : Fragment() {
    private var binding by autoCleared<FragmentGroupHomeBinding>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupHomeBinding.inflate(inflater, container, false)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }
    }
}