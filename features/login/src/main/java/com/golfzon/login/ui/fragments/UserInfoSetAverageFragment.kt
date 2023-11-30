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
import com.golfzon.login.databinding.FragmentUserInfoSetAverageBinding
import com.golfzon.login.ui.LoginActivity
import com.golfzon.login.ui.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserInfoSetAverageFragment : Fragment() {
    private var binding by autoCleared<FragmentUserInfoSetAverageBinding>()
    private val loginViewModel by activityViewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserInfoSetAverageBinding.inflate(inflater)
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
            .setNextClickListener(UserInfoSetAverageFragmentDirections.actionUserInfoSetAverageFragmentToUserImageSetFragment())
    }

    private fun setAgeInput() {
        with(binding) {
            etUserInfoSetAverageInputDigitHundred.addTextChangedListener(
                GenericTextWatcher(
                    binding.etUserInfoSetAverageInputDigitHundred,
                    binding.etUserInfoSetAverageInputDigitTen
                )
            )
            etUserInfoSetAverageInputDigitTen.addTextChangedListener(
                GenericTextWatcher(
                    binding.etUserInfoSetAverageInputDigitTen,
                    binding.etUserInfoSetAverageInputDigitOne
                )
            )
            etUserInfoSetAverageInputDigitOne.addTextChangedListener(
                GenericTextWatcher(
                    binding.etUserInfoSetAverageInputDigitOne,
                    null
                )
            )

            etUserInfoSetAverageInputDigitHundred.setOnKeyListener(
                GenericKeyEvent(
                    binding.etUserInfoSetAverageInputDigitHundred,
                    null,
                    binding.etUserInfoSetAverageInputDigitHundred.id
                )
            )
            etUserInfoSetAverageInputDigitTen.setOnKeyListener(
                GenericKeyEvent(
                    binding.etUserInfoSetAverageInputDigitTen,
                    binding.etUserInfoSetAverageInputDigitHundred,
                    binding.etUserInfoSetAverageInputDigitHundred.id
                )
            )
            etUserInfoSetAverageInputDigitOne.setOnKeyListener(
                GenericKeyEvent(
                    binding.etUserInfoSetAverageInputDigitOne,
                    binding.etUserInfoSetAverageInputDigitTen,
                    binding.etUserInfoSetAverageInputDigitHundred.id
                )
            )
        }
    }
}