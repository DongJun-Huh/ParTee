package com.golfzon.login.ui.fragments

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.golfzon.core_ui.ImageUploadUtil.getTempImageFilePath
import com.golfzon.core_ui.ImageUploadUtil.toBitmap
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.login.R
import com.golfzon.login.databinding.FragmentUserImageSetBinding
import com.golfzon.login.ui.LoginActivity
import com.golfzon.login.ui.LoginViewModel
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

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
        }

        loginViewModel.introduceMessage.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                with(binding.btnUserImageSetInputButtonIntroduce) {
                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            com.golfzon.core_ui.R.color.primary_8B95B3
                        )
                    )
                    iconTint = ContextCompat.getColorStateList(
                        requireContext(),
                        com.golfzon.core_ui.R.color.primary_8B95B3
                    )
                }
            } else {
                with(binding.btnUserImageSetInputButtonIntroduce) {
                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            com.golfzon.core_ui.R.color.gray_707777
                        )
                    )
                    iconTint = ContextCompat.getColorStateList(
                        requireContext(),
                        com.golfzon.core_ui.R.color.gray_707777
                    )
                }
            }
        }
    }

    private fun observeUserImage() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("editedImagePath")
            ?.observe(viewLifecycleOwner) { editedImagePath ->
                val curBitmap = Uri.fromFile(File(editedImagePath))
                    .toBitmap(requireContext().contentResolver)
                with(loginViewModel) {
                    profileImgBitmap.postValue(curBitmap)
                    profileImgPath.postValue(getTempImageFilePath("webp", requireContext()))
                }

                binding.ivUserImageSet.scaleType = ImageView.ScaleType.CENTER_CROP
                Glide.with(requireContext())
                    .load(curBitmap.copy(Bitmap.Config.ARGB_8888, true))
                    // Cannot create a mutable Bitmap with config: HARDWARE 오류로 COPY해 mutable가능하도록 한 뒤 사용
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(binding.ivUserImageSet)
                with(binding.btnUserImageSetInputButtonImage) {
                    setTextColor(
                        ContextCompat.getColor(
                            requireContext(),
                            com.golfzon.core_ui.R.color.primary_8B95B3
                        )
                    )
                    iconTint = ContextCompat.getColorStateList(
                        requireContext(),
                        com.golfzon.core_ui.R.color.primary_8B95B3
                    )
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