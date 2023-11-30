package com.golfzon.login.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.golfzon.core_ui.GenericKeyEvent
import com.golfzon.core_ui.GenericTextWatcher
import com.golfzon.core_ui.autoCleared
import com.golfzon.login.databinding.FragmentUserInfoSetYearsPlayingBinding
import com.golfzon.login.ui.LoginActivity
import com.golfzon.login.ui.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserInfoSetYearsPlayingFragment : Fragment() {
    private var binding by autoCleared<FragmentUserInfoSetYearsPlayingBinding>()
    private val loginViewModel by activityViewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserInfoSetYearsPlayingBinding.inflate(inflater)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNavigate()
        setAgeInput()
    }

    private fun setDataBindingVariables() {
        binding.apply {
            vm = loginViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun setNavigate() {
        (requireActivity() as LoginActivity)
            .setNextClickListener(UserInfoSetYearsPlayingFragmentDirections.actionUserInfoSetYearsPlayingFragmentToUserInfoSetAverageFragment())
    }

    private fun setAgeInput() {
        with(binding) {
            etUserInfoSetYearsPlayingInputDigitTen.addTextChangedListener(
                GenericTextWatcher(
                    binding.etUserInfoSetYearsPlayingInputDigitTen,
                    binding.etUserInfoSetYearsPlayingInputDigitOne
                )
            )
            etUserInfoSetYearsPlayingInputDigitOne.addTextChangedListener(
                GenericTextWatcher(
                    binding.etUserInfoSetYearsPlayingInputDigitOne,
                    null
                )
            )

            etUserInfoSetYearsPlayingInputDigitTen.setOnKeyListener(
                GenericKeyEvent(
                    binding.etUserInfoSetYearsPlayingInputDigitTen,
                    null,
                    binding.etUserInfoSetYearsPlayingInputDigitTen.id
                )
            )
            etUserInfoSetYearsPlayingInputDigitOne.setOnKeyListener(
                GenericKeyEvent(
                    binding.etUserInfoSetYearsPlayingInputDigitOne,
                    binding.etUserInfoSetYearsPlayingInputDigitTen,
                    binding.etUserInfoSetYearsPlayingInputDigitTen.id
                )
            )
        }
    }
}