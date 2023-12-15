package com.golfzon.team

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.golfzon.core_ui.GridSpacingItemDecoration
import com.golfzon.core_ui.ImageUploadUtil
import com.golfzon.core_ui.ImageUploadUtil.toBitmap
import com.golfzon.core_ui.KeyBoardUtil.showKeyboard
import com.golfzon.core_ui.adapter.TeamUserAdapter
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.core_ui.extension.toast
import com.golfzon.team.databinding.FragmentTeamInfoBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class TeamInfoFragment : Fragment() {
    private var binding by autoCleared<FragmentTeamInfoBinding>() { onDestroyBindingView() }
    private val teamViewModel by activityViewModels<TeamViewModel>()
    private var teamUserAdapter: TeamUserAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getTeamInfo()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeamInfoBinding.inflate(inflater, container, false)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeTeamInfo()
        setTeamUserAdapter()
        setBackClickListener()
        setTopActionClickListeners()
        observeTeamInfoSave()
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
            teamViewModel.getTeamMembersInfo(teamInfo.membersUId, teamInfo.leaderUId)
            if (teamViewModel.newTeamImageBitmap.value == null) {
                val currentImage =
                    binding.ivTeamInfoImage.drawable // Glide가 로딩되는 동안 이전 이미지를 유지하도록 placeholder로 지정
                Glide.with(requireContext())
                    .load("https://firebasestorage.googleapis.com/v0/b/partee-1ba05.appspot.com/o/teams%2F${teamInfo.teamImageUrl}?alt=media")
                    .placeholder(currentImage)
                    .error(currentImage)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(binding.ivTeamInfoImage)
            }
        }

        teamViewModel.teamUsers.observe(viewLifecycleOwner) { users ->
            teamUserAdapter?.submitList(users)
        }
    }

    private fun setTopActionClickListeners() {
        setAddMemberClickListener()
        setTeamLocationSetClickListener()
        setTeamNameSetClickListener()
        setTeamImageSetClickListener()
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
            btnTeamInfoSave.toggleHideAndShow()
            btnTeamInfoBack.toggleHideAndShow()
            btnTeamInfoBreak.toggleHideAndShow()

            btnTeamInfoSetCancel.toggleHideAndShow()
            btnTeamInfoSetSave.toggleHideAndShow()
            tvTeamInfoSetDim.toggleHideAndShow()
            ivTeamInfoSetNickname.toggleHideAndShow()

            etTeamInfoSetName.isEnabled = !etTeamInfoSetName.isEnabled
            btnTeamInfoActionAddUser.isEnabled = !btnTeamInfoActionAddUser.isEnabled
            btnTeamInfoActionChangeLocation.isEnabled = !btnTeamInfoActionChangeLocation.isEnabled
            btnTeamInfoActionChangeInfo.isEnabled = !btnTeamInfoActionChangeInfo.isEnabled
            btnTeamInfoActionChangeImage.isEnabled = !btnTeamInfoActionChangeImage.isEnabled
        }
    }

    private fun setTeamNameSetClickListener() {
        binding.btnTeamInfoActionChangeInfo.setOnDebounceClickListener {
            setTeamInfoSetLayoutOnAndOff()
        }

        setTeamNameInputLayoutClickListener()
        setTeamNameChangeCancelClickListener()
        setTeamNameChangeSaveClickListener()
    }

    private fun setTeamNameChangeCancelClickListener() {
        binding.btnTeamInfoSetCancel.setOnDebounceClickListener {
            binding.etTeamInfoSetName.setText(teamViewModel.newTeam.value?.teamName)
            setTeamInfoSetLayoutOnAndOff()
        }
    }

    private fun setTeamNameChangeSaveClickListener() {
        binding.btnTeamInfoSetSave.setOnDebounceClickListener {
            teamViewModel.changeTeamName(binding.etTeamInfoSetName.text.toString())
            setTeamInfoSetLayoutOnAndOff()
        }
    }

    private fun setTeamNameInputLayoutClickListener() {
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

    private fun setTeamImageSetClickListener() {
        binding.btnTeamInfoActionChangeImage.setOnDebounceClickListener {
            findNavController().navigate(TeamInfoFragmentDirections.actionTeamInfoFragmentToTeamImageSetOptionFragment())
        }
        observeTeamImageChange()
    }

    private fun observeTeamImageChange() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("editedImagePath")
            ?.observe(viewLifecycleOwner) { editedImagePath ->
                val curBitmap = Uri.fromFile(File(editedImagePath))
                    .toBitmap(requireContext().contentResolver)
                with(teamViewModel) {
                    newTeamImageBitmap.postValue(curBitmap)
                    newTeamImgPath.postValue(
                        ImageUploadUtil.getTempImageFilePath(
                            newTeamImgExtension.value ?: "jpg",
                            requireContext()
                        )
                    )
                }

                Glide.with(requireContext())
                    .load(curBitmap.copy(Bitmap.Config.ARGB_8888, true))
                    .into(binding.ivTeamInfoImage)
            }
    }

    private fun observeTeamInfoSave() {
        teamViewModel.isTeamOrganizeSuccess.observe(viewLifecycleOwner) { isSuccess ->
            with(isSuccess.getContentIfNotHandled()) {
                if (this == true) {
                    (requireActivity() as TeamActivity).navigateToMatching()
                } else if (this == false) {
                    this@TeamInfoFragment.toast(
                        message = getString(R.string.team_organize_team_fail_not_checked_all),
                        isError = true
                    )
                }
            }
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