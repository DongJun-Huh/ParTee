package com.golfzon.matching

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.golfzon.core_ui.DialogUtil.resizeDialogFragment
import com.golfzon.core_ui.DialogUtil.setDialogRadius
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.matching.databinding.FragmentMatchingSuccessBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchingSuccessFragment : DialogFragment() {
    private var binding by autoCleared<FragmentMatchingSuccessBinding>()
    private val matchingViewModel by activityViewModels<MatchingViewModel>()
    private val args by navArgs<MatchingSuccessFragmentArgs>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMatchingSuccessBinding.inflate(inflater, container, false)
        setDialogRadius(dialog!!)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setReservationClickListener()
        observeSuccessTeamInfo()
    }

    override fun onResume() {
        super.onResume()
        resizeDialogFragment(requireContext(), dialog!!)
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = matchingViewModel
        }
    }

    private fun setReservationClickListener() {
        binding.btnMatchingSuccessGroupReservation.setOnDebounceClickListener {
            (requireActivity() as MatchingActivity).navigateToGroup(
                destination = getString(com.golfzon.core_ui.R.string.group_reservation_deeplink_url),
                groupUId = args.groupUId
            )
        }
    }

    private fun observeSuccessTeamInfo() {
        matchingViewModel.successTeamInfo.observe(viewLifecycleOwner) {
            binding.commonLocation = it.searchingLocations.joinToString(separator = ", ")
        }
    }
}