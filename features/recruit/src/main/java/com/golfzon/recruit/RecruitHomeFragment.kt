package com.golfzon.recruit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.golfzon.core_ui.autoCleared
import com.golfzon.recruit.databinding.FragmentRecruitHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecruitHomeFragment : Fragment() {
    private var binding by autoCleared<FragmentRecruitHomeBinding>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecruitHomeBinding.inflate(inflater, container, false)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBottomNavigationView()
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun setBottomNavigationView() {
        with(binding.bottomNavigationRecruitHome) {
            setupWithNavController(findNavController())
            selectedItemId = R.id.RecruitHomeFragment
            itemIconTintList = null
        }
        with(binding.bottomNavigationRecruitHome.menu) {
            findItem(com.golfzon.core_ui.R.id.MatchingHomeFragment).setOnMenuItemClickListener {
                (requireActivity() as RecruitActivity).navigateToMatching()
                true
            }
            findItem(com.golfzon.core_ui.R.id.GroupHomeFragment).setOnMenuItemClickListener {
                (requireActivity() as RecruitActivity).navigateToGroup()
                true
            }
        }
    }
}