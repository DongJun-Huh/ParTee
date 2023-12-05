package com.golfzon.matching

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.golfzon.core_ui.DialogUtil
import com.golfzon.core_ui.DialogUtil.resizeDialogFragment
import com.golfzon.core_ui.autoCleared
import com.golfzon.matching.databinding.FragmentMatchingFilteringDialogBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchingFilteringDialogFragment : DialogFragment() {
    private var binding by autoCleared<FragmentMatchingFilteringDialogBinding>()
    private val matchingViewModel by activityViewModels<MatchingViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMatchingFilteringDialogBinding.inflate(inflater, container, false)
        DialogUtil.setDialogRadius(dialog!!)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        resizeDialogFragment(requireContext(), dialog!!)
    }

    private fun setDataBindingVariables() {
        binding.apply {
            vm = matchingViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }
}