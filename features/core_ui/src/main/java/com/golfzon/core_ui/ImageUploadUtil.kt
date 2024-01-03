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
import java.lang.Integer.max
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
    private const val IMAGE_LIMIT_DEFAULT_SIZE = 1080

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

    private fun Bitmap.resizeBitmap(
        resizedWidth: Int = IMAGE_LIMIT_DEFAULT_SIZE,
        resizedHeight: Int = IMAGE_LIMIT_DEFAULT_SIZE,
    ): Bitmap {
        var bmpWidth = this.width.toFloat()
        var bmpHeight = this.height.toFloat()

        if (bmpWidth > resizedWidth) {
            val mWidth = bmpWidth / 100
            val scale = resizedWidth / mWidth
            bmpWidth *= scale / 100
            bmpHeight *= scale / 100
        } else if (bmpHeight > resizedHeight) {
            val mHeight = bmpHeight / 100
            val scale = resizedHeight / mHeight
            bmpWidth *= scale / 100
            bmpHeight *= scale / 100
        }
        return Bitmap.createScaledBitmap(this, bmpWidth.toInt(), bmpHeight.toInt(), true)
    }

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


    fun RequestManager.loadImageFromFirebaseStoragePreload(
        imageUId: String,
        imageType: ImageType,
        applicationContext: Context
    ) {
        try {
            val firebaseStorage: FirebaseStorage by lazy {
                val hiltEntryPoint = EntryPoints.get(
                    applicationContext,
                    StorageComponent::class.java
                )
                hiltEntryPoint.getFirebaseStorage()
            }

            firebaseStorage.reference.child("${imageType.imageUrlPrefix}/resized/1080_${imageUId}").downloadUrl.addOnSuccessListener {
                this.asBitmap()
                    .load(it)
                    .preload()
            }
        } catch (e: StorageException) {
            e.printStackTrace()
        }
    }

    @AddTrace(name = "loadImageFromFirebaseStorage")
    fun RequestManager.loadImageFromFirebaseStorage(
        imageUId: String,
        imageType: ImageType,
        placeholder: Drawable? = null,
        width: Int,
        height: Int,
        imageView: ImageView
    ) {
        val imageLoadingTrace: Trace = Firebase.performance.newTrace("image_loading_trace")
        var isTraceStarted = false
        try {
            val requestSize = when (max(width, height)) {
                in 720..1080 -> 1080
                in 480..720 -> 720
                in 240..480 -> 480
                in 120..240 -> 240
                in 0..120 -> 120
                else -> 1080
            }

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

            this.asBitmap()
                .load("https://storage.googleapis.com/partee-1ba05.appspot.com/${imageType.imageUrlPrefix}/resized/${requestSize}_${imageUId}")
                .placeholder(placeholder)
                .override(width, height)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap,
                        transition: Transition<in Bitmap>?
                    ) {
                        imageView.setImageBitmap(resource)

                        with(imageLoadingTrace) {
                            putAttribute(
                                "image_size",
                                "${resource.width}x${resource.height}"
                            )
                            putAttribute(
                                "image_file_size_request",
                                "${requestSize}"
                            )
                            if (isTraceStarted) {
                                isTraceStarted = false
                                stop()
                            }
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                        with(imageLoadingTrace) {
                            if (isTraceStarted) {
                                isTraceStarted = false
                                stop()
                            }
                        }
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        super.onLoadFailed(errorDrawable)
                        with(imageLoadingTrace) {
                            if (isTraceStarted) {
                                isTraceStarted = false
                                stop()
                            }
                        }
                    }
                })

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