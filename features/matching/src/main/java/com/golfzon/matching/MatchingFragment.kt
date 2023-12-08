package com.golfzon.matching

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.golfzon.core_ui.DefaultToast
import com.golfzon.core_ui.HorizontalMarginItemDecoration
import com.golfzon.core_ui.adapter.CandidateTeamMemberAdapter
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.domain.model.User
import com.golfzon.matching.databinding.FragmentMatchingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class MatchingFragment : Fragment() {
    private var binding by autoCleared<FragmentMatchingBinding> { onDestroyBindingView() }
    private val matchingViewModel by activityViewModels<MatchingViewModel>()
    private var candidateTeamMemberAdapter: CandidateTeamMemberAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMatchingBinding.inflate(inflater, container, false)
        matchingViewModel.clearCandidateTeams()
        setDataBindingVariables()
        getCurUserTeam()
        getCandidateTeams()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBackClickListener()
        setSearchUserResultAdapter()
        observeCurrentUserTeam()
        observeCurrentCandidateTeam()
        observeCurrentCandidateTeamMembers()
        observeMatchingSuccess()
        observeCandidateTeamIsEnd()
    }

    private fun onDestroyBindingView() {
        candidateTeamMemberAdapter = null
        matchingViewModel.clearCandidateTeams()
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = matchingViewModel
        }
    }

    private fun setBackClickListener() {
        binding.btnMatchingAppbarBack.setOnDebounceClickListener {
            findNavController().navigateUp()
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
            if (curTeam != null) {
                binding.curTeamDetail = curTeam
                matchingViewModel.getCandidateTeamMembersInfo(curTeam.membersUId)

                binding.btnMatchingReactionsLike.isEnabled = true
                binding.btnMatchingReactionsDislike.isEnabled = true
            } else {
                // TODO 매칭 불가 별도 화면으로 표시
                DefaultToast.createToast(
                    context = requireContext(),
                    message = "더이상 매칭될 수 있는 팀이 없어요!",
                    isError = true
                )?.show()
                binding.btnMatchingReactionsLike.isEnabled = false
                binding.btnMatchingReactionsDislike.isEnabled = false
            }
        }
    }

    private fun observeCurrentCandidateTeamMembers() {
        matchingViewModel.curCandidateTeamMembers.observe(viewLifecycleOwner) { curMembers ->
            if (curMembers.isNotEmpty()) {
                with(binding) {
                    curTeamAvgAge = curMembers.map { it.age ?: 0 }.average().roundToInt()
                    curTeamAvgYearsPlaying =
                        curMembers.map { it.yearsPlaying ?: 0 }.average().roundToInt()
                    curTeamAvgAverage = curMembers.map { it.average ?: 0 }.average().roundToInt()
                }
            }
            candidateTeamMemberAdapter?.submitList(curMembers)
        }
    }

    private fun setSearchUserResultAdapter() {
        candidateTeamMemberAdapter = CandidateTeamMemberAdapter()
        with(binding.rvMatchingCandidateUsers) {
            adapter = candidateTeamMemberAdapter
            addItemDecoration(HorizontalMarginItemDecoration(12.dp))
        }

        candidateTeamMemberAdapter?.setOnItemClickListener(object :
            CandidateTeamMemberAdapter.OnItemClickListener {
            override fun onItemClick(imageView: ImageView, user: User) {
                with(binding) {
                    curUserDetail = user
                    layoutMatchingCandidateTeam.visibility = View.GONE
                    layoutMatchingCandidateUser.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun observeMatchingSuccess() {
        matchingViewModel.isSuccessMatching.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess.getContentIfNotHandled() == true) {
                findNavController().navigate(MatchingFragmentDirections.actionMatchingFragmentToMatchingSuccessFragment())
            }
        }
    }

    private fun observeCandidateTeamIsEnd() {
        matchingViewModel.isCandidateEnd.observe(viewLifecycleOwner) { isEnd ->
            if (isEnd.getContentIfNotHandled() == true) {
                // TODO 매칭 불가 별도 화면으로 표시
                DefaultToast.createToast(
                    context = requireContext(),
                    message = "더이상 매칭될 수 있는 팀이 없어요!",
                    isError = true
                )?.show()
                binding.btnMatchingReactionsLike.isEnabled = false
                binding.btnMatchingReactionsDislike.isEnabled = false
            } else {
                binding.btnMatchingReactionsLike.isEnabled = true
                binding.btnMatchingReactionsDislike.isEnabled = true
            }
        }
    }
}
