package com.golfzon.screen

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.screen.databinding.FragmentScreenCreateRoomBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ScreenCreateRoomFragment : Fragment() {
    private var binding by autoCleared<FragmentScreenCreateRoomBinding>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentScreenCreateRoomBinding.inflate(inflater, container, false)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBackClickListener()
        setCompleteClickListener()
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun setBackClickListener() {
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                (requireActivity() as ScreenActivity).navigateToGroup()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }

    private fun setCompleteClickListener() {
        // TODO
        binding.btnScreenCreateRoomComplete.setOnDebounceClickListener {
            (requireActivity() as ScreenActivity).navigateToGroup()
        }
    }
}