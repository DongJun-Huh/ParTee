package com.golfzon.login.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.login.databinding.FragmentUserInfoSetNicknameBinding
import com.golfzon.login.ui.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserInfoSetNicknameFragment : Fragment() {
    private lateinit var binding: FragmentUserInfoSetNicknameBinding
    private val loginViewModel by activityViewModels<LoginViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserInfoSetNicknameBinding.inflate(inflater)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNavigate()
    }

    private fun setDataBindingVariables() {
        binding.apply {
            vm = loginViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun setNavigate() {
        binding.btnUserInfoSetNicknameNext.setOnDebounceClickListener {
            findNavController().navigate(UserInfoSetNicknameFragmentDirections.actionUserInfoSetNicknameFragmentToUserInfoSetAgeFragment())
        }
    }
}