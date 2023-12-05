package com.golfzon.matching

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.golfzon.core_ui.autoCleared
import com.golfzon.matching.databinding.FragmentMatchingHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchingHomeFragment : Fragment() {
    private var binding by autoCleared<FragmentMatchingHomeBinding>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMatchingHomeBinding.inflate(inflater, container, false)
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