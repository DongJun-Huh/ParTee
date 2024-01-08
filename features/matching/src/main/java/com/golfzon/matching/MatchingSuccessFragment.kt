package com.golfzon.matching

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.golfzon.core_ui.DialogUtil.setFullSizeDialogFragment
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.matching.databinding.FragmentMatchingSuccessBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchingSuccessFragment : Fragment() {
    private var binding by autoCleared<FragmentMatchingSuccessBinding> { onDestroyBindingView() }
    private val matchingViewModel by activityViewModels<MatchingViewModel>()
    private val args by navArgs<MatchingSuccessFragmentArgs>()
    private var glideRequestManager: RequestManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMatchingSuccessBinding.inflate(inflater, container, false)
        glideRequestManager = Glide.with(this@MatchingSuccessFragment)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setChatClickListener()
        setCloseClickListener()
        observeSuccessTeamInfo()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun onDestroyBindingView() {
        glideRequestManager = null
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = matchingViewModel
            requestManager = glideRequestManager
        }
    }

    private fun setChatClickListener() {
        binding.btnMatchingSuccessGroupChat.setOnDebounceClickListener {
            (requireActivity() as MatchingActivity).navigateToChat(args.groupUId)
        }
    }

    private fun setCloseClickListener() {
        binding.btnMatchingSuccessClose.setOnDebounceClickListener {
            findNavController().navigateUp()
        }
        binding.btnMatchingSuccessCloseX.setOnDebounceClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeSuccessTeamInfo() {
        matchingViewModel.successTeamInfo.observe(viewLifecycleOwner) {
            binding.commonLocation = it.searchingLocations.joinToString(separator = ", ")
        }
    }
}