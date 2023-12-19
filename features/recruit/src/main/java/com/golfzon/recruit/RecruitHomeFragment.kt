package com.golfzon.recruit

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
import com.golfzon.core_ui.adapter.itemDecoration.VerticalMarginItemDecoration
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.core_ui.extension.addRecyclerViewLastItemMarginBottom
import com.golfzon.core_ui.extension.setOnDebounceClickListener
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
        setCreateRecruitClickListener()
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
        recruitViewModel.recruitsDisplayInfo.observe(viewLifecycleOwner) { recruits ->
            recruitPostAdapter?.submitList(recruits.toMutableList())
            // 이미 한번 Attach 되었던 경우 동적 Margin 추가
            with(binding.rvRecruitPosts) {
                if (childCount > 0) {
                    getChildAt(childCount - 1).addRecyclerViewLastItemMarginBottom(12.dp + binding.bottomNavigationRecruitHome.height + 12.dp)
                }
            }
        }
    }

    private fun setRecruitPostAdapter() {
        recruitPostAdapter = RecruitPostAdapter()
        with(binding.rvRecruitPosts) {
            adapter = recruitPostAdapter
            addItemDecoration(VerticalMarginItemDecoration(20))
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

        binding.rvRecruitPosts.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                with(binding.rvRecruitPosts) {
                    if (getChildAdapterPosition(view) + 1 == adapter?.itemCount) {
                        view.addRecyclerViewLastItemMarginBottom(20.dp + binding.bottomNavigationRecruitHome.height + 20.dp)
                    }
                }
            }

            override fun onChildViewDetachedFromWindow(view: View) {}
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

    private fun setCreateRecruitClickListener() {
        binding.btnRecruitAppbarCreate.setOnDebounceClickListener {
            findNavController().navigate(RecruitHomeFragmentDirections.actionRecruitHomeFragmentToRecruitCreateFragment())
        }
    }
}