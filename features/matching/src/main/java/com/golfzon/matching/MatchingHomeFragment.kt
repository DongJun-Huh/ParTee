package com.golfzon.matching

import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.golfzon.core_ui.adapter.TeamUserAdapter
import com.golfzon.core_ui.adapter.itemDecoration.VerticalMarginItemDecoration
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.core_ui.extension.toast
import com.golfzon.matching.databinding.FragmentMatchingHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchingHomeFragment : Fragment() {
    private var binding by autoCleared<FragmentMatchingHomeBinding> { onDestroyBindingView() }
    private val matchingViewModel by activityViewModels<MatchingViewModel>()
    private var teamUserAdapter: TeamUserAdapter? = null
    private var glideRequestManager: RequestManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMatchingHomeBinding.inflate(inflater, container, false)
        glideRequestManager = Glide.with(this@MatchingHomeFragment)
        getBaseInfo()
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTeamUserAdapter()
        initializeTeamInfo()
        navigateToTeamInfo()
        initBottomSheet()
        setMatchingStartGuide()
    }

    private fun onDestroyBindingView() {
        teamUserAdapter = null
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = matchingViewModel
        }
    }

    private fun setTeamUserAdapter() {
        teamUserAdapter =
            TeamUserAdapter(true, requestManager = this@MatchingHomeFragment.glideRequestManager!!)
        with(binding.rvMatchingHomeTeamUsers) {
            adapter = teamUserAdapter
            addItemDecoration(VerticalMarginItemDecoration(12))
        }
    }

    private fun getBaseInfo() {
        matchingViewModel.getCurrentUserInfo()
        matchingViewModel.getTeamInfo()
    }

    private fun initializeTeamInfo() {
        matchingViewModel.teamInfoDetail.observe(viewLifecycleOwner) { teamInfo ->
            matchingViewModel.clearUserInfo()
            if (teamInfo != null) {
                matchingViewModel.getTeamMembersInfo(teamInfo.membersUId, teamInfo.leaderUId)
            }
            setStartMatchingClickListener(isEnable = teamInfo != null)
        }

        matchingViewModel.teamUsers.observe(viewLifecycleOwner) { users ->
            // TeamInfoDetail을 얻은 후 받아오는 정보
            teamUserAdapter?.submitList(users)
            val curUserInfo = users.filter { it.second }
            if (curUserInfo.isNotEmpty()) {
                if (curUserInfo[0].first.userUId != curUserInfo[0].third) {
                    binding.btnMatchingHomeTeamSetting.isVisible = false
                    with(binding.btnMatchingHomeStart) {
                        isEnabled = false
                        text = getString(R.string.matching_start_fail_not_leader)
                    }
                } else {
                    binding.btnMatchingHomeTeamSetting.isVisible = true
                    with(binding.btnMatchingHomeStart) {
                        isEnabled = true
                        text = getString(R.string.matching_start)
                    }
                }
            }
        }
    }

    private fun navigateToTeamInfo() {
        binding.btnMatchingHomeTeamSetting.setOnDebounceClickListener {
            (requireActivity() as MatchingActivity).navigateToTeam()
        }
    }

    private fun setStartMatchingClickListener(isEnable: Boolean) {
        binding.btnMatchingHomeStart.setOnDebounceClickListener {
            if (isEnable) {
                findNavController().navigate(MatchingHomeFragmentDirections.actionMatchingHomeFragmentToMatchingFilteringDialogFragment())
            } else {
                this@MatchingHomeFragment.toast(
                    message = getString(R.string.home_team_matching_start_fail_not_exist_team),
                    isError = true
                )
            }
        }
    }

    private fun setMatchingStartGuide() {
        val guideSpan = SpannableString(getString(R.string.matching_start_guide))
        guideSpan.setSpan(
            ForegroundColorSpan(
                resources.getColor(
                    com.golfzon.core_ui.R.color.primary_A4EF69,
                    null
                )
            ),
            guideSpan.indexOf(getString(com.golfzon.core_ui.R.string.partee)),
            guideSpan.indexOf(getString(com.golfzon.core_ui.R.string.partee)) + getString(com.golfzon.core_ui.R.string.partee).length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.itemTextviewMatchingHomeGuide.tvSpeechBubble.text = guideSpan
    }

    private fun initBottomSheet() {
        val bottomSheetBehavior = BottomSheetBehavior.from(binding.viewMatchingHomeTeamBottomSheet)

        val textBubbleHeightExpanded = 0.dp
        val textBubbleDelta = 82.dp
        val textBubbleParams = binding.itemTextviewMatchingHomeGuide.layoutTextBubble.layoutParams

        val btnMatchStartParams = binding.btnMatchingHomeStart.layoutParams
        val heightDelta = 56.dp - 48.dp
        val heightExpanded = 48.dp

        bottomSheetBehavior.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                btnMatchStartParams.height =
                    (heightExpanded + heightDelta * (1 - slideOffset)).toInt()
                binding.btnMatchingHomeStart.layoutParams = btnMatchStartParams

                textBubbleParams.height =
                    (textBubbleHeightExpanded + textBubbleDelta * (1 - slideOffset)).toInt()
                binding.itemTextviewMatchingHomeGuide.viewSpeechBubbleTriangle.isVisible =
                    textBubbleParams.height >= 68.dp
                binding.itemTextviewMatchingHomeGuide.layoutTextBubble.layoutParams =
                    textBubbleParams
                binding.itemTextviewMatchingHomeGuide.layoutTextBubble.alpha = 1 - slideOffset
            }
        })
    }
}