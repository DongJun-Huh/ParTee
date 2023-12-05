package com.golfzon.matching

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.golfzon.core_ui.GridSpacingItemDecoration
import com.golfzon.core_ui.adapter.TeamUserAdapter
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.matching.databinding.FragmentMatchingHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchingHomeFragment : Fragment() {
    private var binding by autoCleared<FragmentMatchingHomeBinding> { onDestroyBindingView() }
    private val matchingViewModel by activityViewModels<MatchingViewModel>()
    private var teamUserAdapter: TeamUserAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMatchingHomeBinding.inflate(inflater, container, false)
        getBaseInfo()
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setTeamUserAdapter()
        initializeUserInfo()
        initializeTeamInfo()
        navigateToTeamInfo()
        setStartMatchingClickListener()
        setBottomNavigationView()
    }

    private fun onDestroyBindingView() {
        teamUserAdapter = null
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun setBottomNavigationView() {
        with(binding.bottomNavigationMatchingHome) {
            selectedItemId = R.id.MatchingHomeFragment
            itemIconTintList = null;
        }
    }

    private fun setTeamUserAdapter() {
        teamUserAdapter = TeamUserAdapter(true)
        with(binding.rvMatchingHomeTeamUsers) {
            adapter = teamUserAdapter
            addItemDecoration(GridSpacingItemDecoration(1, 12.dp))
        }
    }

    private fun getBaseInfo() {
        matchingViewModel.getCurrentUserInfo()
        matchingViewModel.getTeamInfo()
    }

    private fun initializeUserInfo() {
        matchingViewModel.currentUserBasicInfo.observe(viewLifecycleOwner) { userDetail ->
            binding.userDetail = userDetail

        }
    }

    private fun initializeTeamInfo() {
        matchingViewModel.teamInfoDetail.observe(viewLifecycleOwner) { teamInfo ->
            matchingViewModel.clearUserInfo()
            teamInfo.membersUId.map { UId ->
                matchingViewModel.getTeamMemberInfo(UId, teamInfo.leaderUId)
            }
            binding.teamDetail = teamInfo
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

    private fun setStartMatchingClickListener() {
        binding.btnMatchingHomeStart.setOnDebounceClickListener {
            findNavController().navigate(MatchingHomeFragmentDirections.actionMatchingHomeFragmentToMatchingFilteringDialogFragment())
        }
    }
}