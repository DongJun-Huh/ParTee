package com.golfzon.matching

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.matching.databinding.FragmentMatchingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class MatchingFragment : Fragment() {
    private var binding by autoCleared<FragmentMatchingBinding>()
    private val matchingViewModel by activityViewModels<MatchingViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMatchingBinding.inflate(inflater, container, false)
        setDataBindingVariables()
        getCurUserTeam()
        getCandidateTeams()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tempUserImageClickListener()
        observeCurrentUserTeam()
        observeCurrentCandidateTeam()
        observeCurrentCandidateTeamInfoAverage()
        observeMatchingSuccess()
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = matchingViewModel
        }
    }

    private fun getCurUserTeam() {
        matchingViewModel.getTeamInfo()
    }

    private fun getCandidateTeams() {
        matchingViewModel.getFilteredCandidateTeams()
    }

    private fun observeCurrentUserTeam() {
        matchingViewModel.teamInfoDetail.observe(viewLifecycleOwner) { curUserTeam ->
            binding.curUserTeamDetail = curUserTeam
        }
    }

    private fun observeCurrentCandidateTeam() {
        matchingViewModel.curCandidateTeam.observe(viewLifecycleOwner) { curTeam ->
            binding.curTeamDetail = curTeam
            matchingViewModel.getCandidateTeamMembersInfo(curTeam.membersUId)
        }
    }

    private fun observeCurrentCandidateTeamInfoAverage() {
        matchingViewModel.curCandidateTeamMembers.observe(viewLifecycleOwner) { curMembers ->
            if (curMembers.isNotEmpty()) {
                with(binding) {
                    curTeamAvgAge = curMembers.map { it.age ?: 0 }.average().roundToInt()
                    curTeamAvgYearsPlaying =
                        curMembers.map { it.yearsPlaying ?: 0 }.average().roundToInt()
                    curTeamAvgAverage = curMembers.map { it.average ?: 0 }.average().roundToInt()
                }
            }
        }
    }

    private fun tempUserImageClickListener() {
        // TODO 기능 구현 후 삭제 메소드
        binding.ivMatchingCandidateUser1.setOnDebounceClickListener {
            binding.layoutMatchingCandidateTeam.visibility = View.GONE
            binding.layoutMatchingCandidateUser.visibility = View.VISIBLE
        }
        binding.ivMatchingCandidateUser2.setOnDebounceClickListener {
            binding.layoutMatchingCandidateTeam.visibility = View.GONE
            binding.layoutMatchingCandidateUser.visibility = View.VISIBLE
        }
        binding.ivMatchingCandidateUser3.setOnDebounceClickListener {
            binding.layoutMatchingCandidateTeam.visibility = View.GONE
            binding.layoutMatchingCandidateUser.visibility = View.VISIBLE
        }
    }

    private fun observeMatchingSuccess() {
        matchingViewModel.isSuccessMatching.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess.getContentIfNotHandled() == true) {
                findNavController().navigate(MatchingFragmentDirections.actionMatchingFragmentToMatchingSuccessFragment())
            }
        }
    }
}
