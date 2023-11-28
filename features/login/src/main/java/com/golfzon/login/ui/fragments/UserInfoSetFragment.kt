package com.golfzon.login.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.login.databinding.FragmentUserInfoSetBinding
import com.golfzon.login.ui.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserInfoSetFragment : Fragment() {
    private lateinit var binding: FragmentUserInfoSetBinding
    private val loginViewModel by activityViewModels<LoginViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserInfoSetBinding.inflate(inflater)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setNextClickListener()
    }
    private fun setDataBindingVariables() {
        binding.apply {
            vm = loginViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun setNextClickListener() {
        binding.btnUserInfoSetNext.setOnDebounceClickListener {
            findNavController().navigate(UserInfoSetFragmentDirections.actionUserInfoSetFragmentToUserImageSetFragment())
        }
    }
}