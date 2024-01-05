package com.golfzon.login.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.golfzon.core_ui.autoCleared
import com.golfzon.login.databinding.FragmentUserInfoSetNicknameBinding
import com.golfzon.login.ui.LoginActivity
import com.golfzon.login.ui.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserInfoSetNicknameFragment : Fragment() {
    private var binding by autoCleared<FragmentUserInfoSetNicknameBinding>()
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
        binding.etUserInfoSetNickname.requestFocus()
    }

    private fun setDataBindingVariables() {
        binding.apply {
            vm = loginViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun setNavigate() {
        (requireActivity() as LoginActivity)
            .setNextClickListener(UserInfoSetNicknameFragmentDirections.actionUserInfoSetNicknameFragmentToUserInfoSetAgeFragment())
    }
}