package com.golfzon.matching

import android.os.Bundle
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
        observeCurrentCandidateTeamIsExist()
        observeCurrentCandidateTeam()
        observeCurrentCandidateTeamMembers()
        observeMatchingSuccess()
        observeCandidateTeamIsEnd()
    }

    private fun onDestroyBindingView() {
        candidateTeamMemberAdapter = null
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

    private fun observeCurrentCandidateTeamIsExist() {
        matchingViewModel.isCurCandidateTeamExist.observe(viewLifecycleOwner) { isExist ->
            with(isExist.getContentIfNotHandled()) {
                displayCandidateTeamIsOver(this == false)

                if (this != null) {
                    binding.layoutMatchingSearching.visibility = View.GONE
                }
            }
        }
    }

    private fun observeCurrentCandidateTeam() {
        matchingViewModel.curCandidateTeam.observe(viewLifecycleOwner) { curTeam ->
            if (curTeam.getContentIfNotHandled() != null) {
                binding.curTeamDetail = curTeam.peekContent()
                matchingViewModel.getCandidateTeamMembersInfo(curTeam.peekContent()!!.membersUId)
            } else {
                // 최초 로딩 이후 2번째 화면 실행부터 화면 실행하자마자 실행되는 곳
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
            displayCandidateTeamIsOver(isEnd.getContentIfNotHandled() ?: false)
        }
    }

    private fun displayCandidateTeamIsOver(isEnd: Boolean) {
        setReactionButtonsEnabled(isEnabled = !isEnd)
        binding.layoutMatchingNotExist.visibility = if (isEnd) View.VISIBLE else View.GONE

        if (isEnd) {
            DefaultToast.createToast(
                context = requireContext(),
                message = getString(R.string.matching_candidate_team_is_not_exist_toast_message),
                isError = true
            )?.show()
        }
    }

    private fun setReactionButtonsEnabled(isEnabled: Boolean) {
        with(binding) {
            btnMatchingReactionsLike.isEnabled = isEnabled
            btnMatchingReactionsDislike.isEnabled = isEnabled
        }
    }
}
