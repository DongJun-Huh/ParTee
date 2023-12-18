package com.golfzon.login.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.golfzon.core_ui.DialogUtil.resizeDialogFragment
import com.golfzon.core_ui.DialogUtil.setDialogRadius
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.login.databinding.FragmentUserInfoSetIntroduceBinding
import com.golfzon.login.ui.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserInfoSetIntroduceFragment : DialogFragment() {
    private lateinit var binding: FragmentUserInfoSetIntroduceBinding
    private val loginViewModel by activityViewModels<LoginViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserInfoSetIntroduceBinding.inflate(inflater, container, false)
        setDialogRadius(dialog!!)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSaveClickListener()
    }

    override fun onResume() {
        super.onResume()
        resizeDialogFragment(requireContext(), dialog!!)
    }

    private fun setDataBindingVariables() {
        binding.apply {
            vm = loginViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun setSaveClickListener() {
        binding.tvUserInfoSetIntroduceSave.setOnDebounceClickListener {
            loginViewModel.introduceMessage.postValue(binding.etUserInfoSetIntroduce.text.toString())
            findNavController().navigateUp()
        }
    }
}