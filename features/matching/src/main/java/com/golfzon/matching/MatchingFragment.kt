package com.golfzon.matching

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.golfzon.core_ui.ImageUploadUtil
import com.golfzon.core_ui.adapter.itemDecoration.HorizontalMarginItemDecoration
import com.golfzon.core_ui.ImageUploadUtil.loadImageFromFirebaseStorage
import com.golfzon.core_ui.adapter.CandidateTeamMemberAdapter
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.core_ui.extension.toast
import com.golfzon.domain.model.User
import com.golfzon.matching.databinding.FragmentMatchingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class MatchingFragment : Fragment() {
    private var binding by autoCleared<FragmentMatchingBinding> { onDestroyBindingView() }
    private val matchingViewModel by activityViewModels<MatchingViewModel>()
    private var candidateTeamMemberAdapter: CandidateTeamMemberAdapter? = null
    private var glideRequestManager: RequestManager? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMatchingBinding.inflate(inflater, container, false)
        glideRequestManager = Glide.with(this@MatchingFragment)
        setDataBindingVariables()
        getCurUserTeam()
        getCandidateTeams()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFilteringConditionChangeClickListener()
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
        glideRequestManager = null
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = matchingViewModel
            requestManager = glideRequestManager
        }
    }

    private fun setFilteringConditionChangeClickListener() {
        binding.btnMatchingFilteringChange.setOnDebounceClickListener {
            findNavController().navigate(MatchingFragmentDirections.actionMatchingFragmentToMatchingFilteringDialogFragment())
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
                matchingViewModel.curCandidateTeamIndex.value = 0
                if (this != null) {
                    binding.layoutMatchingSearching.visibility = View.GONE
                }
            }
        }
    }

    private fun observeCurrentCandidateTeam() {
        matchingViewModel.curCandidateTeam.observe(viewLifecycleOwner) { curTeam ->
            with(curTeam.getContentIfNotHandled()) {
                if (this != null) {
                    binding.let {
                        it.curTeamDetail = this
                        glideRequestManager?.loadImageFromFirebaseStorage(
                            imageUId = this.teamImageUrl,
                            imageType = ImageUploadUtil.ImageType.TEAM,
                            size = it.ivMatchingBackground.width,
                            imageView = it.ivMatchingBackground
                        )
                    }
                    matchingViewModel.getCandidateTeamMembersInfo(this.membersUId)
                } else {
                    // 최초 로딩 이후 2번째 화면 실행부터 화면 실행하자마자 실행되는 곳
                }
            }
        }
    }

    private fun observeCurrentCandidateTeamMembers() {
        matchingViewModel.curCandidateTeamMembers.observe(viewLifecycleOwner) { curMembers ->
            candidateTeamMemberAdapter?.submitList(curMembers)
        }
    }

    private fun setSearchUserResultAdapter() {
        candidateTeamMemberAdapter = CandidateTeamMemberAdapter(requestManager = glideRequestManager!!)
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
        matchingViewModel.createdGroupId.observe(viewLifecycleOwner) { groupUId ->
            with(groupUId.getContentIfNotHandled()) {
                if (!this.isNullOrEmpty()) {
                    findNavController().navigate(
                        MatchingFragmentDirections.actionMatchingFragmentToMatchingSuccessFragment(
                            groupUId = this
                        )
                    )
                }
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
            this@MatchingFragment.toast(
                message = getString(R.string.matching_candidate_team_is_not_exist_toast_message),
                isError = true
            )
        }
    }

    private fun setReactionButtonsEnabled(isEnabled: Boolean) {
        with(binding) {
            btnMatchingReactionsLike.isEnabled = isEnabled
            btnMatchingReactionsDislike.isEnabled = isEnabled
        }
    }
}
