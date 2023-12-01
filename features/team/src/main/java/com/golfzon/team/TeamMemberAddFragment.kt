package com.golfzon.team

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.golfzon.core_ui.autoCleared
import com.golfzon.team.databinding.FragmentTeamMemberAddBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamMemberAddFragment : BottomSheetDialogFragment() {
    private var binding by autoCleared<FragmentTeamMemberAddBinding>()
    private val teamViewModel by activityViewModels<TeamViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeamMemberAddBinding.inflate(inflater, container, false)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeSearchResults()
    }

    private fun setDataBindingVariables() {
        binding.apply {
            vm = teamViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun observeSearchResults() {
        teamViewModel.searchedUsers.observe(viewLifecycleOwner) {
            // TODO INITIALIZE USERS
        }
    }

}