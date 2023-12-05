package com.golfzon.core_ui.image.crop

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.golfzon.core_ui.ImageUploadUtil.toBitmap
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.databinding.FragmentImageCropBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImageCropFragment : Fragment() {
    private var binding by autoCleared<FragmentImageCropBinding> { onDestroyBindingView() }
    private val args by navArgs<ImageCropFragmentArgs>()
    private lateinit var editedImage: Bitmap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentImageCropBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setImageView()
    }

    private fun onDestroyBindingView() {
        editedImage = binding.imageCropView.croppedImage
        findNavController().currentBackStackEntry?.savedStateHandle?.set("editedImage", editedImage)
    }

    private fun setImageView() {
        with(binding.imageCropView) {
            setAspectRatio(9, 16)
            setImageBitmap(args.ImageString.toUri().toBitmap(requireContext().contentResolver)) // CustomView 이므로 Glide 사용시 오류
        }
    }
}