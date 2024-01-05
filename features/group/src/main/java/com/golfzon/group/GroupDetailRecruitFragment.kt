package com.golfzon.group

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.golfzon.core_ui.adapter.CandidateTeamMemberAdapter
import com.golfzon.core_ui.adapter.itemDecoration.VerticalMarginItemDecoration
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.domain.model.GroupScreenRoomInfo
import com.golfzon.domain.model.User
import com.golfzon.group.databinding.FragmentGroupDetailRecruitBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupDetailRecruitFragment : Fragment() {
    private var binding by autoCleared<FragmentGroupDetailRecruitBinding> { onDestroyBindingView() }
    private val groupViewModel by activityViewModels<GroupViewModel>()
    private val args by navArgs<GroupDetailRecruitFragmentArgs>()
    private var recruitMemberAdapter: CandidateTeamMemberAdapter? = null
    private var glideRequestManager: RequestManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupDetailRecruitBinding.inflate(inflater, container, false)
        glideRequestManager = Glide.with(this@GroupDetailRecruitFragment)
        setDataBindingVariables()
        getGroupDetails()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecruitMembersAdapter()
        setChatClickListener()
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = groupViewModel
            requestManager = glideRequestManager
        }
    }

    private fun onDestroyBindingView() {
        recruitMemberAdapter = null
        glideRequestManager = null
    }

    private fun getGroupDetails() {
        groupViewModel.getGroupDetail(args.groupUId)
    }

    private fun observeGroupDetail() {
        groupViewModel.curGroupDetail.observe(viewLifecycleOwner) { isGroupInitialized ->
            with(isGroupInitialized.getContentIfNotHandled()) {
                if (this != null) {
                    recruitMemberAdapter?.submitList(this.membersInfo?.toMutableList())
                    binding.let {
                        it.groupDetail = this

                        it.groupScreenRoomInfo = this.screenRoomInfo
                        setReservationClickListener(this.screenRoomInfo)
                        if (this.screenRoomInfo?.screenRoomPlaceName?.isNotEmpty() == true) {
                            binding.tvGroupDetailLocation.text =
                                this.screenRoomInfo.screenRoomPlaceName
                        }

                        this.membersInfo?.first()?.let { memberInfo ->
                            it.userDetail = memberInfo
                        }
                    }
                }
            }
        }
    }

    private fun setReservationClickListener(screenRoomInfo: GroupScreenRoomInfo) {
        with(binding.btnGroupDetailReservation) {
            this.isEnabled = screenRoomInfo.screenRoomPlaceName.isEmpty()
            if (screenRoomInfo.screenRoomPlaceName.isEmpty()) {
                this.setOnDebounceClickListener {
                    findNavController().navigate(
                        GroupDetailRecruitFragmentDirections.actionGroupDetailRecruitFragmentToGroupCreateRoomScreenFragment(
                            args.groupUId
                        )
                    )
                }
            } else {
                this.text = getString(R.string.group_create_room_complete)
            }
        }
    }

    private fun setRecruitMembersAdapter() {
        recruitMemberAdapter =
            CandidateTeamMemberAdapter(72.dp, requestManager = glideRequestManager!!)
        with(binding.rvGroupDetailRecruitMembers) {
            adapter = recruitMemberAdapter
            addItemDecoration(VerticalMarginItemDecoration(8))
        }

        recruitMemberAdapter?.setOnItemClickListener(object :
            CandidateTeamMemberAdapter.OnItemClickListener {
            override fun onItemClick(imageView: ImageView, user: User) {
                with(binding) {
                    userDetail = user
                }
            }
        })

        observeGroupDetail()
    }

    private fun setChatClickListener() {
        binding.btnGroupDetailChat.setOnDebounceClickListener {
            (requireActivity() as GroupActivity).navigateToChat(groupUId = args.groupUId)
        }
    }
}