package com.golfzon.team

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.golfzon.core_ui.GridSpacingItemDecoration
import com.golfzon.core_ui.KeyBoardUtil.showKeyboard
import com.golfzon.core_ui.adapter.TeamUserAdapter
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.core_ui.extension.setOnDebounceClickListener
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
        setAddMemberClickListener()
        setTeamLocationSetClickListener()
        setTeamInfoSetClickListener()
        setTeamInfoChangeCancelClickListener()
        setTeamInfoChangeNameClickListener()
        setTeamInfoChangeImageClickListener()
        observeTeamInfoSave()
        setTeamInfoSetImageLayout()
        setBackClickListener()
        observeDeleteTeamStatus()
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
            teamViewModel.clearUserInfo()
            teamInfo.membersUId.map { UId ->
                teamViewModel.getTeamMemberInfo(UId, teamInfo.leaderUId)
            }
        }

        teamViewModel.teamUsers.observe(viewLifecycleOwner) { users ->
            teamUserAdapter?.submitList(users)
        }
    }

    private fun setAddMemberClickListener() {
        binding.btnTeamInfoActionAddUser.setOnDebounceClickListener {
            findNavController().navigate(TeamInfoFragmentDirections.actionTeamInfoFragmentToTeamMemberAddFragment())
        }
    }

    private fun setTeamLocationSetClickListener() {
        binding.btnTeamInfoActionChangeLocation.setOnDebounceClickListener {
            findNavController().navigate(TeamInfoFragmentDirections.actionTeamInfoFragmentToTeamLocationSetFragment())
        }
    }

    private fun setTeamInfoSetLayoutOnAndOff() {
        with(binding) {
            layoutTeamInfoUsers.toggleHideAndShow()
            btnTeamInfoSave.toggleHideAndShow()
            btnTeamInfoBack.toggleHideAndShow()
            btnTeamInfoBreak.toggleHideAndShow()

            btnTeamInfoSetCancel.toggleHideAndShow()
            btnTeamInfoSetSave.toggleHideAndShow()
            tvTeamInfoSetDim.toggleHideAndShow()
            ivTeamInfoSetNickname.toggleHideAndShow()
            layoutTeamInfoSetImage.toggleHideAndShow()

            etTeamInfoSetName.isEnabled = !etTeamInfoSetName.isEnabled
            btnTeamInfoActionAddUser.isEnabled = !btnTeamInfoActionAddUser.isEnabled
            btnTeamInfoActionChangeLocation.isEnabled = !btnTeamInfoActionChangeLocation.isEnabled
            btnTeamInfoActionChangeInfo.isEnabled = !btnTeamInfoActionChangeInfo.isEnabled
        }
    }

    private fun setTeamInfoSetClickListener() {
        binding.btnTeamInfoActionChangeInfo.setOnDebounceClickListener {
            setTeamInfoSetLayoutOnAndOff()
        }
    }

    private fun setTeamInfoChangeCancelClickListener() {
        binding.btnTeamInfoSetCancel.setOnDebounceClickListener {
            binding.etTeamInfoSetName.setText(teamViewModel.newTeam.value?.teamName)
            setTeamInfoSetLayoutOnAndOff()
        }
    }

    private fun setTeamInfoChangeNameClickListener() {
        binding.btnTeamInfoSetSave.setOnDebounceClickListener {
            teamViewModel.changeTeamName(binding.etTeamInfoSetName.text.toString())
            setTeamInfoSetLayoutOnAndOff()
        }
    }

    private fun setTeamInfoChangeImageClickListener() {
        with(binding) {
            layoutTeamInfoName.setOnDebounceClickListener {
                with(etTeamInfoSetName) {
                    if (isEnabled) {
                        requestFocus()
                        this.showKeyboard(requireContext())
                    }
                }
            }
        }
    }

    private fun observeTeamInfoSave() {
        teamViewModel.isTeamOrganizeSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess.getContentIfNotHandled() == true) {
                (requireActivity() as TeamActivity).navigateToMatching()
            }
        }
    }

    private fun setTeamInfoSetImageLayout() {
        // TODO Image Setting 기능 추가
//        val flexboxLayoutManager = FlexboxLayoutManager(requireContext())
//        flexboxLayoutManager.flexWrap = FlexWrap.WRAP
//        binding.rvTeamInfoSetImages.layoutManager = flexboxLayoutManager

        binding.ivTeamInfoSetImage.setOnDebounceClickListener {
            // TODO 임시 이미지 설정 기능 추가
        }
    }

    private fun setBackClickListener() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (requireActivity() as TeamActivity).navigateToMatching()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )

        binding.btnTeamInfoBack.setOnDebounceClickListener {
            (requireActivity() as TeamActivity).navigateToMatching()
        }
    }

    private fun observeDeleteTeamStatus() {
        teamViewModel.isTeamDeleteSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess.getContentIfNotHandled() == true) {
                (requireActivity() as TeamActivity).navigateToMatching()
            }
        }
    }

    private fun View.toggleHideAndShow() {
        visibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE
        isEnabled = visibility == View.VISIBLE
    }
}