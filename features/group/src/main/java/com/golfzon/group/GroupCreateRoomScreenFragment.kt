package com.golfzon.group

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.golfzon.core_ui.autoCleared
import com.golfzon.group.databinding.FragmentGroupCreateRoomScreenBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GroupCreateRoomScreenFragment : Fragment() {
    private var binding by autoCleared<FragmentGroupCreateRoomScreenBinding>()
    private val groupViewModel by activityViewModels<GroupViewModel>()
    private val args by navArgs<GroupCreateRoomScreenFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupCreateRoomScreenBinding.inflate(inflater, container, false)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        createGroupRoomScreen()// TODO 임시
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = groupViewModel
        }
    }

    private fun createGroupRoomScreen() {
        // TODO
    }
}