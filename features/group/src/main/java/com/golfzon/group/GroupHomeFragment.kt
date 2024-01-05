package com.golfzon.group

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.golfzon.core_ui.adapter.itemDecoration.VerticalMarginItemDecoration
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.core_ui.extension.addRecyclerViewLastItemMarginBottom
import com.golfzon.domain.model.Group
import com.golfzon.group.databinding.FragmentGroupHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupHomeFragment : Fragment() {
    private var binding by autoCleared<FragmentGroupHomeBinding> { onDestroyBindingView() }
    private val groupViewModel by activityViewModels<GroupViewModel>()
    private var groupAdapter: GroupAdapter? = null
    private var glideRequestManager: RequestManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupHomeBinding.inflate(inflater, container, false)
        glideRequestManager = Glide.with(this@GroupHomeFragment)
        getGroups()
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBottomNavigationView()
        setGroupsAdapter()
        observeGroups()
    }

    private fun onDestroyBindingView() {
        groupAdapter = null
        glideRequestManager = null
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun setBottomNavigationView() {
        with(binding.bottomNavigationGroupHome) {
            setupWithNavController(findNavController())
            selectedItemId = com.golfzon.core_ui.R.id.GroupHomeFragment
            itemIconTintList = null
        }
        with(binding.bottomNavigationGroupHome.menu) {
            findItem(com.golfzon.core_ui.R.id.MatchingHomeFragment).setOnMenuItemClickListener {
                (requireActivity() as GroupActivity).navigateToMatching()
                true
            }
            findItem(com.golfzon.core_ui.R.id.RecruitHomeFragment).setOnMenuItemClickListener {
                (requireActivity() as GroupActivity).navigateToRecruit()
                true
            }
        }
    }

    private fun getGroups() {
        groupViewModel.getGroups()
    }

    private fun setGroupsAdapter() {
        groupAdapter = GroupAdapter(requestManager = this@GroupHomeFragment.glideRequestManager!!)
        with(binding.rvGroupHomeGroups) {
            adapter = groupAdapter
            addItemDecoration(VerticalMarginItemDecoration(12))
        }

        groupAdapter?.setOnItemClickListener(object :
            GroupAdapter.OnItemClickListener {
            override fun onItemClick(view: View, group: Group) {
                findNavController().navigate(
                    GroupHomeFragmentDirections.actionGroupHomeFragmentToGroupDetailFragment(
                        groupUId = group.groupUId
                    )
                )
            }

            override fun onRecruitItemClick(view: View, group: Group) {
                findNavController().navigate(
                    GroupHomeFragmentDirections.actionGroupHomeFragmentToGwroupDetailRecruitFragment(
                        groupUId = group.groupUId
                    )
                )
            }
        })


        binding.rvGroupHomeGroups.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                // 최초 RecyclerView 로딩 시 동적 Margin 추가
                with(binding.rvGroupHomeGroups) {
                    if (getChildAdapterPosition(view) + 1 == adapter?.itemCount) {
                        view.addRecyclerViewLastItemMarginBottom(
                            12.dp + binding.bottomNavigationGroupHome.height + 12.dp
                        )
                    }
                }
            }

            override fun onChildViewDetachedFromWindow(view: View) {}
        })
    }

    private fun observeGroups() {
        groupViewModel.groups.observe(viewLifecycleOwner) { groups ->
            groupAdapter?.submitList(groups)
            // 이미 한번 Attach 되었던 경우 동적 Margin 추가
            with(binding.rvGroupHomeGroups) {
                if (childCount > 0) {
                    getChildAt(childCount - 1).addRecyclerViewLastItemMarginBottom(
                        12.dp + binding.bottomNavigationGroupHome.height + 12.dp
                    )
                }
            }
        }
    }
}