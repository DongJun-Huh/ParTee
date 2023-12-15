package com.golfzon.login.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import dagger.hilt.android.AndroidEntryPoint
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.firebase.ui.auth.AuthUI
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.core_ui.extension.toast
import com.golfzon.login.databinding.FragmentLoginBinding
import com.golfzon.login.ui.LoginActivity
import com.golfzon.login.ui.LoginViewModel

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var binding by autoCleared<FragmentLoginBinding>()
    private val loginViewModel by activityViewModels<LoginViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeLoginResult()
        setGoogleLogin()
    }

    private fun setDataBindingVariables() {
        with(binding) {
            vm = loginViewModel
            lifecycleOwner = this@LoginFragment
        }
    }

    private fun setGoogleLogin() {
        val signInLauncher = registerForActivityResult(
            FirebaseAuthUIActivityResultContract(),
        ) { res -> loginViewModel.onGoogleLoginResult(res) }

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(listOf(AuthUI.IdpConfig.GoogleBuilder().build()))
            .build()

        binding.layoutLoginGoogleBtn.setOnDebounceClickListener {
            signInLauncher.launch(signInIntent)
        }
    }

    private fun observeLoginResult() {
        loginViewModel.loginSuccess.observe(viewLifecycleOwner) { isLoginSuccess ->
            if (isLoginSuccess.getContentIfNotHandled() == false) {
                // 로그인 자체가 실패하는 경우
                this.toast(msg = "로그인에 실패했습니다")
            }
        }

        loginViewModel.isUserInitialized.observe(viewLifecycleOwner) { isUserInitialized ->
            if (isUserInitialized.getContentIfNotHandled() == true) {
                (requireActivity() as LoginActivity).navigateToMatching()
            } else {
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginFragmentToUserInfoSetNicknameFragment()
                )
                // 새로 회원가입한 유저 or 유저 정보는 있지만, 정보설정은 안된 유저인지 체크 후, 체크가 안되면 정보설정 화면으로 이동
            }
        }
    }
}