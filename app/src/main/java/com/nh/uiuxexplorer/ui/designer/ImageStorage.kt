package com.nh.uiuxexplorer.ui.designer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max

/**
 * Copies user-picked Gallery images into the app's persistent internal storage so they remain available
 * across app restarts, saved screens, and high-resolution PNG exports.
 */
object ImageStorage {
    private const val MAX_DIM = 1600

    private val bitmapCache = object : LruCache<String, Bitmap>(24) {}

    /** Copies and optimizes a Gallery [sourceUri] into internal storage, returning the saved file path. */
    suspend fun importFromGallery(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, boundsOpts)
            }
            val srcW = boundsOpts.outWidth
            val srcH = boundsOpts.outHeight
            if (srcW <= 0 || srcH <= 0) return@runCatching null

            var sampleSize = 1
            while (max(srcW / sampleSize, srcH / sampleSize) > MAX_DIM * 2) {
                sampleSize *= 2
            }

            val decodeOpts = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val rawBitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                BitmapFactory.decodeStream(input, null, decodeOpts)
            } ?: return@runCatching null

            val longest = max(rawBitmap.width, rawBitmap.height)
            val finalBitmap = if (longest > MAX_DIM) {
                val ratio = MAX_DIM.toFloat() / longest.toFloat()
                val nw = (rawBitmap.width * ratio).toInt().coerceAtLeast(1)
                val nh = (rawBitmap.height * ratio).toInt().coerceAtLeast(1)
                Bitmap.createScaledBitmap(rawBitmap, nw, nh, true)
            } else {
                rawBitmap
            }

            val dir = File(context.filesDir, "designer_images").apply { mkdirs() }
            val outFile = File(dir, "img_${UUID.randomUUID()}.png")
            FileOutputStream(outFile).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            val path = outFile.absolutePath
            bitmapCache.put(path, finalBitmap)
            path
        }.getOrNull()
    }

    /** Loads a [Bitmap] synchronously from memory cache or internal file path. */
    fun loadBitmap(path: String): Bitmap? {
        if (path.isBlank()) return null
        bitmapCache.get(path)?.let { if (!it.isRecycled) return it }
        return runCatching {
            val file = File(path)
            if (!file.exists()) return null
            BitmapFactory.decodeFile(file.absolutePath)?.also { bmp ->
                bitmapCache.put(path, bmp)
            }
        }.getOrNull()
    }

    /** Loads a Compose [ImageBitmap] for fast synchronous canvas drawing. */
    fun loadImageBitmap(path: String): ImageBitmap? = loadBitmap(path)?.asImageBitmap()
}
