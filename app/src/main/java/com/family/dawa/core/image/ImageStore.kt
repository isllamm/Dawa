package com.family.dawa.core.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ImageStore(private val context: Context) {

    private val photosDir = File(context.filesDir, "photos").apply { mkdirs() }

    fun saveBitmap(bitmap: Bitmap): String {
        val file = File(photosDir, "${UUID.randomUUID()}.jpg")
        val scaled = scaleDown(bitmap, maxDimension = 1280f)
        FileOutputStream(file).use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        if (scaled != bitmap) {
            scaled.recycle()
        }
        return file.absolutePath
    }

    fun saveFromUri(uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val original = BitmapFactory.decodeStream(stream) ?: return null
                saveBitmap(original)
            }
        } catch (e: Exception) {
            null
        }
    }

    fun deleteFile(path: String?) {
        if (path.isNullOrEmpty()) return
        try {
            val file = File(path)
            if (file.exists() && file.startsWith(photosDir)) {
                file.delete()
            }
        } catch (_: Exception) {}
    }

    private fun scaleDown(realImage: Bitmap, maxDimension: Float): Bitmap {
        val maxImageSize = maxDimension
        val ratio = Math.min(
            maxImageSize / realImage.width,
            maxImageSize / realImage.height
        )
        if (ratio >= 1.0) return realImage

        val width = Math.round(ratio * realImage.width)
        val height = Math.round(ratio * realImage.height)

        return Bitmap.createScaledBitmap(realImage, width, height, true)
    }
}
