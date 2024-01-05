package com.golfzon.team

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.golfzon.core_ui.DialogUtil
import com.golfzon.core_ui.ImageUploadUtil.bitmapToFile
import com.golfzon.core_ui.ImageUploadUtil.extension
import com.golfzon.core_ui.ImageUploadUtil.getTempImageFilePath
import com.golfzon.core_ui.ImageUploadUtil.isPermitExtension
import com.golfzon.core_ui.ImageUploadUtil.toBitmap
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.core_ui.extension.toast
import com.golfzon.team.databinding.FragmentTeamImageSetOptionBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class TeamImageSetOptionFragment : DialogFragment() {
    private var binding by autoCleared<FragmentTeamImageSetOptionBinding> {}
    private val teamViewModel by activityViewModels<TeamViewModel>()
    private lateinit var currentTakenPhotoPath: String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeamImageSetOptionBinding.inflate(inflater, container, false)
        DialogUtil.setDialogRadius(dialog!!, dialogGravity = Gravity.BOTTOM)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setImageSelectGalleryClickListener()
        setImageTakePhotoClickListener()
    }

    override fun onResume() {
        super.onResume()
        DialogUtil.resizeDialogFragment(requireContext(), dialog!!)
    }

    private fun setImageSelectGalleryClickListener() {
        binding.layoutTeamImageSetOptionSelectGallery.setOnDebounceClickListener {
            requestOpenGallery.launch(PERMISSIONS_GALLERY)
        }
    }

    private val requestOpenGallery =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) openGallery()
        }

    private fun openGallery() {
        // ACTION PICK 사용시, intent type에서 설정한 종류의 데이터를 MediaStore에서 불러와서 목록으로 나열 후 선택할 수 있는 앱 실행
        val intent = Intent(Intent.ACTION_PICK)
            .apply { type = MediaStore.Images.Media.CONTENT_TYPE }
        navigateGallaryActivity.launch(intent)
    }

    val navigateGallaryActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                handleGalleryResult(intent = activityResult.data)
            } else {
                findNavController().popBackStack()
            }
        }

    private fun handleGalleryResult(intent: Intent?) {
        intent?.data?.let { uri ->
            val contentResolver = requireActivity().applicationContext.contentResolver
            if (uri.extension(contentResolver).isPermitExtension) {
                setImageInfo(uri.toBitmap(requireContext().contentResolver))
            } else {
                this@TeamImageSetOptionFragment.toast(
                    message = getString(com.golfzon.core_ui.R.string.upload_image_fail_file_extension),
                    isError = true
                )
                findNavController().popBackStack()
            }
        } ?: run {
            this@TeamImageSetOptionFragment.toast(
                message = getString(com.golfzon.core_ui.R.string.upload_image_fail_unknown),
                isError = true
            )
            findNavController().popBackStack()
        }
    }

    private fun setImageTakePhotoClickListener() {
        binding.layoutTeamImageSetOptionTakePhoto.setOnDebounceClickListener {
            requestOpenCamera.launch(PERMISSIONS_CAMERA)
        }
    }

    val requestOpenCamera =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.all { it.value }) openCamera()
        }

    private fun openCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            if (resolveActivity(requireContext().packageManager) != null) {
                createImageFile()?.let { photoFile ->
                    val photoURI = FileProvider.getUriForFile(
                        requireContext(),
                        "ParTee" + ".fileprovider",
                        photoFile
                    )
                    putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    navigateCameraActivity.launch(this)
                }
            }
        }
    }

    private fun createImageFile(): File? =
        try {
            val timeStamp = SimpleDateFormat("yyyy-MM-d-HH-mm-ss", Locale.KOREA).format(Date())
            val photoFileName = "Capture_${timeStamp}_"
            val tmpDir: File? = requireContext().cacheDir
            File.createTempFile(photoFileName, ".webp", tmpDir).apply {
                currentTakenPhotoPath = absolutePath
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    val navigateCameraActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                val photoFile = File(currentTakenPhotoPath)
                setImageInfo(Uri.fromFile(photoFile).toBitmap(requireContext().contentResolver))
            }
        }

    private fun setImageInfo(bitmap: Bitmap) {
        findNavController().navigate(
            TeamImageSetOptionFragmentDirections.actionTeamImageSetOptionFragmentToImageCropFragment(
                ImagePath = bitmapToFile(
                    bitmap,
                    getTempImageFilePath("webp", requireContext())
                )?.absolutePath ?: ""
            )
        )
    }

    companion object {
        val PERMISSIONS_CAMERA = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        val PERMISSIONS_GALLERY = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }
}