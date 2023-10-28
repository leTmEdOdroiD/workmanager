package com.dew.workmanager

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt

class ImageCompressionWorker(private val appContext: Context, private val param: WorkerParameters) :
    CoroutineWorker(appContext, param) {

    override suspend fun doWork(): Result {

        return withContext(Dispatchers.IO) {

            val url = param.inputData.getString(KEY_IMG_URL)
            val byteArray = appContext.contentResolver.openInputStream(Uri.parse(url))?.use {
                it.readBytes()
            } ?: return@withContext Result.failure()

            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)

            var outputByteArray: ByteArray
            var quality = 100
            do {
                val outputStream = ByteArrayOutputStream()
                outputStream.use {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, it)
                    outputByteArray = it.toByteArray()
                    quality -= (quality * 0.1).roundToInt()
                }
            } while (outputByteArray.size > (1024 * 20L) && quality > 5)

            val file = File(appContext.cacheDir, "${param.id}.jpg")
            file.writeBytes(outputByteArray)
            return@withContext Result.success(
                workDataOf(
                    KEY_OUTPUT_IMG_URL to file.absolutePath
                )
            )
        }
    }

    companion object {
        const val KEY_IMG_URL = "KEY_IMG_URL"
        const val KEY_OUTPUT_IMG_URL = "KEY_OUTPUT_IMG_URL"
    }
}