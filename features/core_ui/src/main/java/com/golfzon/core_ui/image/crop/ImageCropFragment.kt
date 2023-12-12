package com.golfzon.core_ui.image.crop

import android.graphics.Bitmap
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.golfzon.core_ui.ImageUploadUtil.bitmapToFile
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.databinding.FragmentImageCropBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ImageCropFragment : Fragment() {
    private var binding by autoCleared<FragmentImageCropBinding> { onDestroyBindingView() }
    private val args by navArgs<ImageCropFragmentArgs>()

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
        createEditedImage()
        findNavController().currentBackStackEntry?.savedStateHandle?.set("editedImagePath", args.ImagePath)
    }

    private fun setImageView() {
        with(binding.imageCropView) {
            setAspectRatio(9, 16)
            setImageFilePath(args.ImagePath)
        }
    }

    private fun createEditedImage() {
        bitmapToFile(
            bitmap = binding.imageCropView.croppedImage.copy(Bitmap.Config.ARGB_8888, true),
            path = args.ImagePath
        )
    }
}