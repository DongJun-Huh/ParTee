package com.golfzon.group

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.group.databinding.FragmentGroupHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupHomeFragment : Fragment() {
    private var binding by autoCleared<FragmentGroupHomeBinding>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupHomeBinding.inflate(inflater, container, false)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBottomNavigationView()
        tmpNavigate()
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun setBottomNavigationView() {
        with(binding.bottomNavigationGroupHome) {
            setupWithNavController(findNavController())
            selectedItemId = com.golfzon.core_ui.R.id.GroupFragment
            itemIconTintList = null
        }
        binding.bottomNavigationGroupHome.menu.findItem(com.golfzon.core_ui.R.id.MatchingHomeFragment).setOnMenuItemClickListener {
            (requireActivity() as GroupActivity).navigateToGroup()
            true
        }
    }

    // TODO 기능 구현 후 삭제
    private fun tmpNavigate() {
        binding.tempListComponent1.root.setOnDebounceClickListener {
            findNavController().navigate(GroupHomeFragmentDirections.actionGroupHomeFragmentToGroupDetailFragment())
        }
        binding.tempListComponent2.root.setOnDebounceClickListener {
            findNavController().navigate(GroupHomeFragmentDirections.actionGroupHomeFragmentToGroupDetailFragment())
        }
        binding.tempListComponent3.root.setOnDebounceClickListener {
            findNavController().navigate(GroupHomeFragmentDirections.actionGroupHomeFragmentToGroupDetailFragment())
        }
        binding.tempListComponent4.root.setOnDebounceClickListener {
            findNavController().navigate(GroupHomeFragmentDirections.actionGroupHomeFragmentToGroupDetailFragment())
        }
    }
}