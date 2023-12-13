package com.golfzon.group

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.golfzon.core_ui.GridSpacingItemDecoration
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.domain.model.Group
import com.golfzon.group.databinding.FragmentGroupHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupHomeFragment : Fragment() {
    private var binding by autoCleared<FragmentGroupHomeBinding>{ onDestroyBindingView() }
    private val groupViewModel by activityViewModels<GroupViewModel>()
    private var groupAdapter: GroupAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupHomeBinding.inflate(inflater, container, false)
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
        groupAdapter = GroupAdapter()
        with(binding.rvGroupHomeGroups) {
            adapter = groupAdapter
            addItemDecoration(GridSpacingItemDecoration(1, 12.dp))
        }

        groupAdapter?.setOnItemClickListener(object :
            GroupAdapter.OnItemClickListener {
            override fun onItemClick(view: View, group: Group) {
                findNavController().navigate(GroupHomeFragmentDirections.actionGroupHomeFragmentToGroupDetailFragment(groupUId = group.groupUId))
            }
        })
    }

    private fun observeGroups() {
        groupViewModel.groups.observe(viewLifecycleOwner) { groups ->
            groupAdapter?.submitList(groups)
        }
    }
}