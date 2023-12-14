package com.golfzon.recruit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.golfzon.core_ui.VerticalMarginItemDecoration
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.domain.model.Recruit
import com.golfzon.domain.model.User
import com.golfzon.recruit.databinding.FragmentRecruitHomeBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecruitHomeFragment : Fragment() {
    private var binding by autoCleared<FragmentRecruitHomeBinding> { onDestroyBindingView() }
    private val recruitViewModel by activityViewModels<RecruitViewModel>()
    private var recruitPostAdapter: RecruitPostAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecruitHomeBinding.inflate(inflater, container, false)
        setDataBindingVariables()
        getRecruits()
        getRecruitsMembers()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecruitPostAdapter()
        setBottomNavigationView()
        observeRecruitsMembers()
    }

    private fun onDestroyBindingView() {
        recruitPostAdapter = null
    }


    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = recruitViewModel
        }
    }

    private fun getRecruits() {
        recruitViewModel.getRecruits()
    }

    private fun getRecruitsMembers() {
        recruitViewModel.recruits.observe(viewLifecycleOwner) { recruits ->
            recruitViewModel.getRecruitsMembersInfo(
                recruits.map { it.membersUId }
            )
        }
    }

    private fun observeRecruitsMembers() {
        recruitViewModel.recruitsMembers.observe(viewLifecycleOwner) { recruitsMembers ->
            // TODO 값을 받아온 후, 다시 재가공하는 부분 ViewModel로 이동
            val recruitsDisplayInfo =
                recruitViewModel.recruits.value!!.mapIndexed { recruitIndex, recruit ->
                    Pair(
                        recruit,
                        recruitsMembers[recruitIndex].mapIndexed { index, pair ->
                            pair.first
                        }
                    )
                }.toMutableList()

            recruitPostAdapter?.submitList(recruitsDisplayInfo)
        }
    }

    private fun setRecruitPostAdapter() {
        recruitPostAdapter = RecruitPostAdapter()
        with(binding.rvRecruitPosts) {
            adapter = recruitPostAdapter
            addItemDecoration(VerticalMarginItemDecoration(20.dp))
        }

        recruitPostAdapter?.setOnItemClickListener(object :
            RecruitPostAdapter.OnItemClickListener {
            override fun onItemClick(view: View, recruitInfo: Pair<Recruit, List<User>>) {
                findNavController().navigate(
                    RecruitHomeFragmentDirections.actionRecruitHomeFragmentToRecruitDetailFragment(
                        recruitInfo.first.recruitUId
                    )
                )
            }
        })
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