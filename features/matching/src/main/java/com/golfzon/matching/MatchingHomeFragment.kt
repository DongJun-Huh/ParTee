package com.golfzon.matching

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.golfzon.core_ui.adapter.itemDecoration.VerticalMarginItemDecoration
import com.golfzon.core_ui.adapter.TeamUserAdapter
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.core_ui.extension.toast
import com.golfzon.matching.databinding.FragmentMatchingHomeBinding
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
        setBottomNavigationView()
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

    private fun setBottomNavigationView() {
        with(binding.bottomNavigationMatchingHome) {
            setupWithNavController(findNavController())
            selectedItemId = R.id.MatchingHomeFragment
            itemIconTintList = null
        }
        with(binding.bottomNavigationMatchingHome.menu) {
            findItem(com.golfzon.core_ui.R.id.GroupHomeFragment).setOnMenuItemClickListener {
                (requireActivity() as MatchingActivity).navigateToGroup()
                true
            }
            findItem(com.golfzon.core_ui.R.id.RecruitHomeFragment).setOnMenuItemClickListener {
                (requireActivity() as MatchingActivity).navigateToRecruit()
                true
            }
        }
    }

    private fun setTeamUserAdapter() {
        teamUserAdapter = TeamUserAdapter(true, requestManager = this@MatchingHomeFragment.glideRequestManager!!)
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
            teamUserAdapter?.submitList(users)
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
}