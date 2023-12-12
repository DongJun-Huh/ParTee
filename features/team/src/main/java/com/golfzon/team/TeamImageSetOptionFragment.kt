package com.golfzon.team

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.golfzon.core_ui.DialogUtil
import com.golfzon.core_ui.ImageUploadUtil.extension
import com.golfzon.core_ui.ImageUploadUtil.isPermitExtension
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.core_ui.extension.toast
import com.golfzon.team.databinding.FragmentTeamImageSetOptionBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Objects

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
        DialogUtil.setDialogRadius(dialog!!)
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
            requestOpenGallery.launch(
                PERMISSIONS_GALLERY
            )
        }
    }

    private val requestOpenGallery =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if (it.value == false) {
                    return@registerForActivityResult
                }
            }
            openGallery()
        }

    private fun openGallery() {
        // ACTION PICK 사용시, intent type에서 설정한 종류의 데이터를 MediaStore에서 불러와서 목록으로 나열 후 선택할 수 있는 앱 실행
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        navigateGallaryActivity.launch(intent)
    }

    val navigateGallaryActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                val data: Intent? = activityResult.data
                // 호출된 갤러리에서 이미지 선택시, data의 data속성으로 해당 이미지의 Uri 전달
                val uri = data?.data!!
                // 이미지 파일과 함께, 파일 확장자도 같이 저장
                if (uri.extension(requireActivity().applicationContext.contentResolver).isPermitExtension) {
                    setImageInfo(
                        uri.toString(),
                        uri.extension(requireActivity().applicationContext.contentResolver)
                    )
                } else {
                    this.toast(msg = getString(com.golfzon.core_ui.R.string.upload_image_fail_file_extension))
                    findNavController().popBackStack()
                }
            } else {
                findNavController().popBackStack()
            }
        }

    private fun setImageTakePhotoClickListener() {
        binding.layoutTeamImageSetOptionTakePhoto.setOnDebounceClickListener {
            requestOpenCamera.launch(
                PERMISSIONS_CAMERA
            )
        }
    }

    val requestOpenCamera =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                if (it.value == false) {
                    return@registerForActivityResult
                }
            }
            openCamera()
        }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            var photoFile: File? = null
            val tmpDir: File? = requireContext().cacheDir
            val timeStamp: String =
                SimpleDateFormat("yyyy-MM-d-HH-mm-ss", Locale.KOREA).format(Date())
            val photoFileName = "Capture_${timeStamp}_"
            try {
                val tmpPhoto = File.createTempFile(photoFileName, ".jpg", tmpDir)
                currentTakenPhotoPath = tmpPhoto.absolutePath
                photoFile = tmpPhoto
            } catch (e: IOException) {
                e.printStackTrace()
            }
            if (photoFile != null) {
                val photoURI = FileProvider.getUriForFile(
                    Objects.requireNonNull(requireContext().applicationContext),
                    "ParTee" + ".fileprovider",
                    photoFile
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                navigateCameraActivity.launch(intent)
            }
        }
    }

    val navigateCameraActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if (activityResult.resultCode == Activity.RESULT_OK) {
                val photoFile = File(currentTakenPhotoPath)
                val photoFileExtension =
                    if (currentTakenPhotoPath
                            .split(".")
                            .last()
                            .isNotEmpty()
                    ) currentTakenPhotoPath.split(".").last() else "jpg"
                setImageInfo(Uri.fromFile(photoFile).toString(), photoFileExtension)
            }
        }

    private fun setImageInfo(imageString: String, fileExtension: String) {
        teamViewModel.setImageFileExtension(fileExtension)
        findNavController().navigate(
            TeamImageSetOptionFragmentDirections.actionTeamImageSetOptionFragmentToImageCropFragment(
                ImageString = imageString
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