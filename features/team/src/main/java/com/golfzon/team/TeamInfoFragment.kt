package com.golfzon.team

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.golfzon.core_ui.GridSpacingItemDecoration
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.team.databinding.FragmentTeamInfoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamInfoFragment : Fragment() {
    private var binding by autoCleared<FragmentTeamInfoBinding>() { onDestroyBindingView() }
    private val teamViewModel by activityViewModels<TeamViewModel>()
    private var teamUserAdapter: TeamUserAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeamInfoBinding.inflate(inflater, container, false)
        setDataBindingVariables()
        getTeamInfo()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeTeamInfo()
        setTeamUserAdapter()
    }

    private fun onDestroyBindingView() {
        teamUserAdapter = null
    }

    private fun setTeamUserAdapter() {
        teamUserAdapter = TeamUserAdapter()
        with(binding.rvTeamInfoUsers) {
            adapter = teamUserAdapter
            addItemDecoration(GridSpacingItemDecoration(1, 12.dp))
        }
    }


    private fun setDataBindingVariables() {
        binding.apply {
            vm = teamViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun getTeamInfo() {
        teamViewModel.getNewTeamInfo()
    }

    private fun initializeTeamInfo() {
        teamViewModel.newTeam.observe(viewLifecycleOwner) { teamInfo ->
            if (teamInfo == null) {
                // TODO: TEAM이 존재하지 않는 경우
            } else {
                // TODO TEAM INFORMATION INITIALIZE
                teamViewModel.clearUserInfo()
                teamInfo.membersUId.map { UId ->
                    teamViewModel.getTeamMemberInfo(UId)
                }
            }
        }

        teamViewModel.teamUsers.observe(viewLifecycleOwner) { users ->
            teamUserAdapter?.submitList(users)
        }
    }
}