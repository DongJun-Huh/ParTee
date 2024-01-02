package com.golfzon.group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.golfzon.core_ui.ImageUploadUtil
import com.golfzon.core_ui.ImageUploadUtil.loadImageFromFirebaseStorage
import com.golfzon.core_ui.adapter.itemDecoration.VerticalMarginItemDecoration
import com.golfzon.core_ui.adapter.CandidateTeamMemberAdapter
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.domain.model.GroupScreenRoomInfo
import com.golfzon.domain.model.User
import com.golfzon.group.databinding.FragmentGroupDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupDetailFragment : Fragment() {
    private var binding by autoCleared<FragmentGroupDetailBinding> { onDestroyBindingView() }
    private val groupViewModel by activityViewModels<GroupViewModel>()
    private val args by navArgs<GroupDetailFragmentArgs>()
    private var firstTeamMemberAdapter: CandidateTeamMemberAdapter? = null
    private var secondTeamMemberAdapter: CandidateTeamMemberAdapter? = null
    private var glideRequestManager: RequestManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupDetailBinding.inflate(inflater, container, false)
        glideRequestManager = Glide.with(this@GroupDetailFragment)
        setDataBindingVariables()
        getGroupDetails()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeGroupDetail()
        setFirstTeamMembersAdapter()
        setSecondTeamMembersAdapter()
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
        firstTeamMemberAdapter = null
        secondTeamMemberAdapter = null
        glideRequestManager = null
    }

    private fun getGroupDetails() {
        groupViewModel.getGroupDetail(groupUId = args.groupUId)
    }

    private fun observeGroupDetail() {
        groupViewModel.curGroupDetail.observe(viewLifecycleOwner) { isGroupInitialized ->
            with(isGroupInitialized.getContentIfNotHandled()) {
                if (this != null && this.originalTeamsInfo.size == 2) {
                    binding.let {
                        it.groupDetail = this
                        it.groupScreenRoomInfo = this.screenRoomInfo
                        setReservationClickListener(this.screenRoomInfo)
                        if (this.screenRoomInfo?.screenRoomPlaceName?.isNotEmpty() == true) {
                            binding.tvGroupDetailLocation.text =
                                this.screenRoomInfo.screenRoomPlaceName
                        }
                        it.firstTeamDetail = this.originalTeamsInfo[0]
                        it.secondTeamDetail = this.originalTeamsInfo[1]

                        glideRequestManager?.let { requestManager ->
                            requestManager.loadImageFromFirebaseStorage(
                                imageUId = this.originalTeamsInfo[0].teamImageUrl,
                                imageType = ImageUploadUtil.ImageType.TEAM,
                                width = it.ivGroupDetailTeamFirst.width,
                                height = it.ivGroupDetailTeamFirst.height,
                                imageView = it.ivGroupDetailTeamFirst
                            )

                            requestManager.loadImageFromFirebaseStorage(
                                imageUId = this.originalTeamsInfo[1].teamImageUrl,
                                imageType = ImageUploadUtil.ImageType.TEAM,
                                width = it.ivGroupDetailTeamSecond.width,
                                height = it.ivGroupDetailTeamSecond.height,
                                imageView = it.ivGroupDetailTeamSecond
                            )
                        }
                    }

                    groupViewModel.getTeamMembersInfo(
                        this.originalTeamsInfo[0].membersUId,
                        this.originalTeamsInfo[1].membersUId
                    )
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
                        GroupDetailFragmentDirections.actionGroupDetailFragmentToGroupCreateRoomScreenFragment(
                            args.groupUId
                        )
                    )
                }
            } else {
                this.text = getString(R.string.group_create_room_complete)
            }
        }
    }

    private fun setFirstTeamMembersAdapter() {
        firstTeamMemberAdapter = CandidateTeamMemberAdapter(72.dp, requestManager = glideRequestManager!!)
        with(binding.rvGroupDetailFirstTeamMembers) {
            adapter = firstTeamMemberAdapter
            addItemDecoration(VerticalMarginItemDecoration(8))
        }

        firstTeamMemberAdapter?.setOnItemClickListener(object :
            CandidateTeamMemberAdapter.OnItemClickListener {
            override fun onItemClick(imageView: ImageView, user: User) {
                with(binding) {
                    firstTeamUserDetail = user
                    layoutGroupDetailTeamFirstInfo.visibility = View.GONE
                    layoutGroupDetailTeamFirstMemberInfo.visibility = View.VISIBLE
                }
            }
        })

        groupViewModel.curFirstTeamMembers.observe(viewLifecycleOwner) {
            firstTeamMemberAdapter?.submitList(it)
        }
    }

    private fun setSecondTeamMembersAdapter() {
        secondTeamMemberAdapter = CandidateTeamMemberAdapter(72.dp, requestManager = glideRequestManager!!)
        with(binding.rvGroupDetailSecondTeamMembers) {
            adapter = secondTeamMemberAdapter
            addItemDecoration(VerticalMarginItemDecoration(8))
        }

        secondTeamMemberAdapter?.setOnItemClickListener(object :
            CandidateTeamMemberAdapter.OnItemClickListener {
            override fun onItemClick(imageView: ImageView, user: User) {
                with(binding) {
                    secondTeamUserDetail = user
                    layoutGroupDetailTeamSecondInfo.visibility = View.GONE
                    layoutGroupDetailTeamSecondMemberInfo.visibility = View.VISIBLE
                }
            }
        })

        groupViewModel.curSecondTeamMembers.observe(viewLifecycleOwner) {
            secondTeamMemberAdapter?.submitList(it)
        }
    }

    private fun setChatClickListener() {
        binding.btnGroupDetailChat.setOnDebounceClickListener {
            (requireActivity() as GroupActivity).navigateToChat(groupUId = args.groupUId)
        }
    }
}
