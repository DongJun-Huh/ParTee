package com.golfzon.matching

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.core.view.isVisible
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
        displayReactionStamp()
        setReactionClickListener()
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
            with(curTeam) {
                if (this != null) {
                    binding.let {
                        it.curTeamDetail = this.cardTop
                        it.nextTeamDetail = this.cardBottom
                        glideRequestManager?.loadImageFromFirebaseStorage(
                            imageUId = this.cardTop.teamImageUrl,
                            imageType = ImageUploadUtil.ImageType.TEAM,
                            placeholder = it.ivBackgroundCardTwo.drawable,
                            size = it.ivMatchingBackground.width,
                            imageView = it.ivMatchingBackground
                        )
                        glideRequestManager?.loadImageFromFirebaseStorage(
                            imageUId = this.cardBottom.teamImageUrl,
                            imageType = ImageUploadUtil.ImageType.TEAM,
                            size = it.ivBackgroundCardTwo.width,
                            imageView = it.ivBackgroundCardTwo
                        )
                    }
                    matchingViewModel.getCandidateTeamMembersInfo(this.cardTop.membersUId)
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
        candidateTeamMemberAdapter =
            CandidateTeamMemberAdapter(requestManager = glideRequestManager!!)
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
        binding.layoutMatchingNotExist.isVisible = isEnd

        if (isEnd) {
            binding.motionLayoutRoot.transitionToState(R.id.notExist)
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

    private fun displayReactionStamp() {
        val combinedTransitionObserver = object : TransitionAdapter() {
            override fun onTransitionChange(
                motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float
            ) {
                super.onTransitionChange(motionLayout, startId, endId, progress)
                displayReactionStamp(endId, progress)
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
                super.onTransitionCompleted(motionLayout, currentId)
                reactionToCurrentTeam(motionLayout, currentId)
            }

            private fun displayReactionStamp(endId: Int, progress: Float) {
                with(binding) {
                    when (endId) {
                        R.id.like -> {
                            ivMatchingStampLike.alpha = if (progress >= 0) progress * 2 else 0F
                            btnMatchingCurrentReactionLike.alpha = if (progress >= 0) 1F else 0F
                            btnMatchingCurrentReactionDislike.alpha = 0F
                        }

                        R.id.unlike -> {
                            ivMatchingStampDislike.alpha = if (progress >= 0) progress * 2 else 0F
                            btnMatchingCurrentReactionDislike.alpha = if (progress >= 0) 1F else 0F
                            btnMatchingCurrentReactionLike.alpha = 0F
                        }

                        else -> {
                            btnMatchingCurrentReactionDislike.alpha = 0F
                            btnMatchingCurrentReactionLike.alpha = 0F

                            ivMatchingStampLike.alpha = 0f
                            ivMatchingStampDislike.alpha = 0f
                        }
                    }
                }
            }

            private fun reactionToCurrentTeam(motionLayout: MotionLayout, currentId: Int) {
                when (currentId) {
                    R.id.offScreenUnlike, R.id.offScreenLike -> {
                        matchingViewModel.reactionsToCandidateTeam(currentId != R.id.offScreenUnlike)
                        with(binding) {
                            ivMatchingBackground.setImageDrawable(binding.ivBackgroundCardTwo.drawable)
                            curTeamDetail = binding.nextTeamDetail
                            layoutMatchingCandidateTeam.isVisible = true
                            layoutMatchingCandidateUser.isVisible = false
                        }
                        resetMotionLayout(motionLayout)
                    }
                }
            }

            private fun resetMotionLayout(motionLayout: MotionLayout) {
                with(motionLayout) {
                    progress = 0f
                    setTransition(R.id.start, R.id.like)
                }
            }
        }

        binding.motionLayoutRoot.setTransitionListener(combinedTransitionObserver)
    }

    private fun setReactionClickListener() {
        with(binding) {
            btnMatchingReactionsLike.setOnDebounceClickListener {
                motionLayoutRoot.transitionToState(R.id.like)
            }
            btnMatchingReactionsDislike.setOnDebounceClickListener {
                motionLayoutRoot.transitionToState(R.id.unlike)
            }
        }
    }
}
