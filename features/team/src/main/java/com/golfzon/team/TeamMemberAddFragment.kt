package com.golfzon.team

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.golfzon.core_ui.DialogUtil.setDialogRadius
import com.golfzon.core_ui.GridSpacingItemDecoration
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.team.databinding.FragmentTeamMemberAddBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamMemberAddFragment : BottomSheetDialogFragment() {
    private var binding by autoCleared<FragmentTeamMemberAddBinding>() { onDestroyBindingView() }
    private val teamViewModel by activityViewModels<TeamViewModel>()
    private var searchUserResultAdapter: SearchUserResultAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeamMemberAddBinding.inflate(inflater, container, false)
        setDialogRadius(dialog!!)
        setDialogStyle()
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSearchUserResultAdapter()
        observeSearchResults()
    }

    private fun setDataBindingVariables() {
        binding.apply {
            vm = teamViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun onDestroyBindingView() {
        searchUserResultAdapter = null
    }

    private fun setDialogStyle() {
        setStyle(STYLE_NORMAL, com.golfzon.core_ui.R.style.CustomBottomSheetDialogTheme)
        dialog?.setOnShowListener {
            binding.root?.minimumHeight = 330.dp
        }
    }

    private fun observeSearchResults() {
        teamViewModel.searchedUsers.observe(viewLifecycleOwner) {
            searchUserResultAdapter?.submitList(it)
        }
    }

    private fun setSearchUserResultAdapter() {
        searchUserResultAdapter = SearchUserResultAdapter()
        with(binding.rvTeamMembersAddSearchResult) {
            adapter = searchUserResultAdapter
            addItemDecoration(GridSpacingItemDecoration(1, 8.dp))
        }
    }
}