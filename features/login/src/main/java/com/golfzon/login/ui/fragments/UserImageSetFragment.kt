package com.golfzon.login.ui.fragments

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.golfzon.core_ui.ImageUploadUtil.getTempImageFilePath
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.login.R
import com.golfzon.login.databinding.FragmentUserImageSetBinding
import com.golfzon.login.ui.LoginActivity
import com.golfzon.login.ui.LoginViewModel
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserImageSetFragment : Fragment() {
    private var binding by autoCleared<FragmentUserImageSetBinding>()
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
        setUserImageOptionClickListener()
        setUserIntroduceOptionClickListener()
        observeUserInitializeComplete()
        setNext()
    }

    override fun onResume() {
        super.onResume()
        observeUserImage()
    }

    private fun setDataBindingVariables() {
        binding.apply {
            vm = loginViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun setUserImageOptionClickListener() {
        binding.btnUserImageSetInputButtonImage.setOnDebounceClickListener {
            findNavController().navigate(UserImageSetFragmentDirections.actionUserImageSetFragmentToUserImageSetOptionFragment())
        }
    }

    private fun setUserIntroduceOptionClickListener() {
        binding.btnUserImageSetInputButtonIntroduce.setOnDebounceClickListener {
            findNavController().navigate(UserImageSetFragmentDirections.actionUserImageSetFragmentToUserInfoSetIntroduceFragment())
            (it as MaterialButton).text =
                getString(R.string.register_user_image_set_button_change_introduce_message)
        }
    }

    private fun observeUserImage() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Bitmap>("editedImage")
            ?.observe(viewLifecycleOwner) {
                with(loginViewModel) {
                    profileImgBitmap.postValue(it)
                    profileImgPath.postValue(
                        getTempImageFilePath(profileImgExtension.value ?: "jpg", requireContext())
                    )
                }

                Glide.with(requireContext())
                    .load(it.copy(Bitmap.Config.ARGB_8888, true))
                    // Cannot create a mutable Bitmap with config: HARDWARE 오류로 COPY해 mutable가능하도록 한 뒤 사용
                    .into(binding.ivUserImageSet)
                with(binding) {
                    tvUserImageSetInputDescription.visibility = View.GONE
                    btnUserImageSetInputButtonImage.text =
                        getString(R.string.register_user_image_set_button_change_image)
                }
            }
    }

    private fun setNext() {
        (requireActivity() as LoginActivity).setNextClickListener()
    }

    private fun observeUserInitializeComplete() {
        loginViewModel.isSetUserInfoSuccess.observe(viewLifecycleOwner) { isComplete ->
            if (isComplete.getContentIfNotHandled() == true) {
                (requireActivity() as LoginActivity).navigateToMatching()
            }
        }
    }
}