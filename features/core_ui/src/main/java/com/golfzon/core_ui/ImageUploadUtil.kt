package com.golfzon.core_ui

import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import com.google.firebase.perf.metrics.AddTrace
import com.google.firebase.perf.metrics.Trace
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@EntryPoint
@InstallIn(SingletonComponent::class)
interface StorageComponent {
    fun getFirebaseStorage(): FirebaseStorage
}

object ImageUploadUtil {
    private val PERMIT_IMAGE_EXTENSIONS = listOf("jpg", "jpeg", "png", "webp")

    // Get Image Extension
    fun Uri.extension(contentResolver: ContentResolver): String = contentResolver
        .getType(this)
        .toString()
        .split("/")[1]

    fun Uri.toBitmap(contentResolver: ContentResolver): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(contentResolver, this)
            )
        } else {
            MediaStore.Images.Media.getBitmap(contentResolver, this)
        }
    }

    val String.isPermitExtension: Boolean get() = this in PERMIT_IMAGE_EXTENSIONS

    fun bitmapToFile(bitmap: Bitmap?, path: String?): File? {
        if (bitmap == null || path == null) {
            return null
        }
        val file = File(path)
        var out: OutputStream? = null
        try {
            file.createNewFile()
            out = FileOutputStream(file)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                bitmap.resizeBitmap()
                    .compress(Bitmap.CompressFormat.WEBP_LOSSY, 100, out)
            } else {
                bitmap.resizeBitmap()
                    .compress(Bitmap.CompressFormat.WEBP, 100, out)
            }
        } finally {
            out?.close()
        }
        return file
    }

    fun getTempImageFilePath(fileExtension: String, context: Context): String {
        val imageFileTimeFormat = SimpleDateFormat("yyyy-MM-d-HH-mm-ss", Locale.KOREA)
        // uri를 통하여 불러온 이미지를 임시로 파일로 저장할 경로로 앱 내부 캐시 디렉토리로 설정,
        // 파일 이름은 불러온 시간 사용
        val fileName = imageFileTimeFormat.format(Date(System.currentTimeMillis()))
            .toString() + "." + fileExtension
        val cacheDir = context.cacheDir.toString()
        return "$cacheDir/$fileName"
    }

    @AddTrace(name = "loadImageFromFirebaseStorage")
    fun RequestManager.loadImageFromFirebaseStorage(
        imageUId: String,
        imageType: ImageType,
        placeholder: Drawable? = null,
        size: Int,
        imageView: ImageView
    ) {
        val imageLoadingTrace: Trace = Firebase.performance.newTrace("image_loading_trace")
        var isTraceStarted = false
        try {
            with(imageLoadingTrace) {
                if (imageView.context is ContextWrapper) {
                    this.putAttribute(
                        "load_activity",
                        (imageView.context as ContextWrapper).baseContext.javaClass.simpleName
                    )
                }
                putAttribute("image_uid", imageUId)
                putAttribute(
                    "image_view_id",
                    imageView.resources.getResourceEntryName(imageView.id)
                )
                isTraceStarted = true
                start()
            }

            val firebaseStorage: FirebaseStorage by lazy {
                val hiltEntryPoint = EntryPoints.get(
                    imageView.context.applicationContext,
                    StorageComponent::class.java
                )
                hiltEntryPoint.getFirebaseStorage()
            }

            firebaseStorage.reference.child("${imageType.imageUrlPrefix}/${imageUId}").downloadUrl.addOnSuccessListener {
                this.asBitmap()
                    .load(it)
                    .override(size, size)
                    .placeholder(placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            if (imageView.width > 0 && imageView.height > 0) {
                                val resized = Bitmap.createScaledBitmap(
                                    resource,
                                    imageView.width,
                                    imageView.height,
                                    false
                                )
                                imageView.setImageBitmap(resized)
                            } else { imageView.setImageBitmap(resource) }

                            with(imageLoadingTrace) {
                                putAttribute(
                                    "image_size",
                                    "${resource.width}x${resource.height}"
                                )
                                putAttribute(
                                    "image_file_size__byte",
                                    "${resource.byteCount}"
                                )
                                if (isTraceStarted) {
                                    isTraceStarted = false
                                    stop()
                                }
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            with(imageLoadingTrace) {
//                                putAttribute("image_file_size__byte", "0")
                                if (isTraceStarted) {
                                    isTraceStarted = false
                                    stop()
                                }
                            }
                        }
                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            with(imageLoadingTrace) {
                                putAttribute("image_file_size__byte", "0")
                                if (isTraceStarted) {
                                    isTraceStarted = false
                                    stop()
                                }
                            }
                        }
                    })
            }
        } catch (e: StorageException) {
            if (isTraceStarted) {
                isTraceStarted = false
                imageLoadingTrace.stop()
            }
            e.printStackTrace()
        }
    }

    enum class ImageType(val imageUrlPrefix: String) {
        USER("users"),
        TEAM("teams"),
    }
}