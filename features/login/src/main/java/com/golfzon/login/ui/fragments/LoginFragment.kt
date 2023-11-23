package com.golfzon.login.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.golfzon.core_ui.autoCleared
import com.golfzon.login.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private var binding by autoCleared<FragmentLoginBinding> { onDestroyBindingView() }
    private val loginViewModel by viewModels<LoginViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        binding.vm = loginViewModel
        binding.lifecycleOwner = this

        mAuth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun onDestroyBindingView() {

    }
}