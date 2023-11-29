package com.golfzon.team

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.golfzon.core_ui.autoCleared
import com.golfzon.team.databinding.FragmentTeamInfoBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamInfoFragment : Fragment() {
    private var binding by autoCleared<FragmentTeamInfoBinding>()
    private val teamViewModel by activityViewModels<TeamViewModel>()

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
    }

    private fun setDataBindingVariables() {
        binding.apply {
            vm = teamViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun getTeamInfo() {
        teamViewModel.getTeamInfo()
    }

    private fun initializeTeamInfo() {
        teamViewModel.teamInfoDetail.observe(viewLifecycleOwner) { teamInfo ->
            if (teamInfo == null) {
                // TODO: TEAM이 존재하지 않는 경우
            } else {
                // TODO TEAM INFORMATION INITIALIZE
            }
        }
    }
}