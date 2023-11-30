package com.golfzon.login.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.golfzon.core_ui.GenericKeyEvent
import com.golfzon.core_ui.GenericTextWatcher
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.login.databinding.FragmentUserInfoSetAgeBinding
import com.golfzon.login.ui.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserInfoSetAgeFragment : Fragment() {
    private lateinit var binding: FragmentUserInfoSetAgeBinding
    private val loginViewModel by activityViewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserInfoSetAgeBinding.inflate(inflater)
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
        binding.btnUserInfoSetAgeNext.setOnDebounceClickListener {
            findNavController().navigate(UserInfoSetAgeFragmentDirections.actionUserInfoSetAgeFragmentToUserInfoSetYearsPlayingFragment())
        }
    }

    private fun setAgeInput() {
        with(binding) {
            etUserInfoSetAgeInputDigitTen.addTextChangedListener(GenericTextWatcher(binding.etUserInfoSetAgeInputDigitTen, binding.etUserInfoSetAgeInputDigitOne))
            etUserInfoSetAgeInputDigitOne.addTextChangedListener(GenericTextWatcher(binding.etUserInfoSetAgeInputDigitOne, null))

            etUserInfoSetAgeInputDigitTen.setOnKeyListener(GenericKeyEvent(binding.etUserInfoSetAgeInputDigitTen, null, binding.etUserInfoSetAgeInputDigitTen.id))
            etUserInfoSetAgeInputDigitOne.setOnKeyListener(GenericKeyEvent(binding.etUserInfoSetAgeInputDigitOne, binding.etUserInfoSetAgeInputDigitTen, binding.etUserInfoSetAgeInputDigitTen.id))
        }
    }
}