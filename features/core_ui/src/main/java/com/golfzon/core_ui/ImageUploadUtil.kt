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
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
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

    fun ImageView.loadImageFromFirebaseStorage(imageUId: String, imageType: ImageType, placeholder: Drawable?= null) {
        try {
            val firebaseStorage: FirebaseStorage by lazy {
                val hiltEntryPoint = EntryPoints.get(this.context.applicationContext, StorageComponent::class.java)
                hiltEntryPoint.getFirebaseStorage()
            }

            firebaseStorage.reference.child("${imageType.imageUrlPrefix}/${imageUId}").downloadUrl.addOnSuccessListener {
                Glide.with(this.context)
                    .load(it)
                    .placeholder(placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(this)
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
        size: Int,
        imageView: ImageView
    ) {
        val imageLoadingTrace: Trace = Firebase.performance.newTrace("image_loading_trace")
        with(imageLoadingTrace) {
            if (imageView.context is ContextWrapper) {
                this.putAttribute(
                    "load_activity",
                    (imageView.context as ContextWrapper).baseContext.javaClass.simpleName
                )
            }
            putAttribute("image_uid", imageUId)
            putAttribute("image_view_id", imageView.resources.getResourceEntryName(imageView.id))
            start()
        }
        try {
            val firebaseStorage: FirebaseStorage by lazy {
                val hiltEntryPoint = EntryPoints.get(
                    imageView.context.applicationContext,
                    StorageComponent::class.java
                )
                hiltEntryPoint.getFirebaseStorage()
            }

            firebaseStorage.reference.child("${imageType.imageUrlPrefix}/${imageUId}").downloadUrl.addOnSuccessListener {
                this.load(it)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            with(imageLoadingTrace) {
                                putAttribute("image_file_size__byte", "0")
                                stop()
                            }
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            with(imageLoadingTrace) {
                                putAttribute(
                                    "image_size",
                                    "${resource.intrinsicWidth}x${resource.intrinsicHeight}"
                                )
                                putAttribute(
                                    "image_file_size__byte",
                                    "${resource.toBitmap().byteCount}"
                                )
                                stop()
                            }
                            return false
                        }
                    })
                    .placeholder(placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
                    .into(imageView)
            }
        } catch (e: StorageException) {
            e.printStackTrace()
        }
    }

    enum class ImageType(val imageUrlPrefix: String) {
        USER("users"),
        TEAM("teams"),
    }
}