package com.golfzon.login.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.golfzon.login.databinding.FragmentUserImageSetBinding
import com.golfzon.login.ui.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserImageSetFragment : Fragment() {
    private lateinit var binding: FragmentUserImageSetBinding
    private val loginViewModel by activityViewModels<LoginViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserImageSetBinding.inflate(inflater, container, false)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setDataBindingVariables() {
        binding.apply {
            vm = loginViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }
}