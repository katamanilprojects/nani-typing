package com.manacdc.nanityping1

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * Level 2: Real-World Image Association Engine.
 * 
 * Implements the "Filename-as-Trigger" zero-database pattern:
 * - Scans local directories and USB drives for images named after words (e.g. kodi.webp, hen.png, mamma.jpg).
 * - Matches words using case-insensitive lowercase comparison.
 * - Safely downsamples 1080p images to RGB_565 bitmaps to prevent OOM on Android TV hardware.
 */
object ImageManager {
    private const val TAG = "ImageManager"
    private val SUPPORTED_EXTENSIONS = setOf("webp", "png", "jpg", "jpeg")

    // In-memory registry: normalized lowercase word -> Image File
    private var imageRegistry = mapOf<String, File>()

    /**
     * Scans all accessible storage paths:
     * 1. App-specific storage on internal storage and USB drives (no runtime permissions required).
     * 2. Public /sdcard/NaniTyping/ folder.
     * 3. Root of attached USB OTG storage drives (/storage/XXXX-XXXX/NaniTyping/).
     */
    fun reloadRegistry(context: Context): Int {
        val registry = mutableMapOf<String, File>()
        val targetDirs = mutableListOf<File>()

        // 1. App-specific external storage (Internal flash + connected USB drives)
        // Path: /storage/.../Android/data/com.manacdc.nanityping1/files/NaniTyping
        try {
            context.getExternalFilesDirs("NaniTyping")?.filterNotNull()?.forEach { dir ->
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                targetDirs.add(dir)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to inspect getExternalFilesDirs: ${e.message}")
        }

        // 2. Standard public directory /sdcard/NaniTyping
        try {
            val publicDir = File(Environment.getExternalStorageDirectory(), "NaniTyping")
            if (publicDir.exists() && publicDir.isDirectory) {
                targetDirs.add(publicDir)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to inspect public external storage: ${e.message}")
        }

        // 3. Direct USB mount volumes (/storage/XXXX-XXXX/NaniTyping)
        try {
            val storageRoot = File("/storage")
            if (storageRoot.exists() && storageRoot.isDirectory) {
                storageRoot.listFiles()?.forEach { volume ->
                    if (volume.isDirectory && volume.name != "emulated" && volume.name != "self") {
                        val usbNaniDir = File(volume, "NaniTyping")
                        if (usbNaniDir.exists() && usbNaniDir.isDirectory) {
                            targetDirs.add(usbNaniDir)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to inspect USB volumes: ${e.message}")
        }

        // Index all matching image files
        for (dir in targetDirs) {
            dir.listFiles()?.forEach { file ->
                if (file.isFile && file.extension.lowercase(Locale.ROOT) in SUPPORTED_EXTENSIONS) {
                    val wordKey = file.nameWithoutExtension.lowercase(Locale.ROOT)
                    if (!registry.containsKey(wordKey)) {
                        registry[wordKey] = file
                    }
                }
            }
        }

        imageRegistry = registry
        Log.i(TAG, "Indexed ${registry.size} custom trigger images across ${targetDirs.size} locations.")
        return registry.size
    }

    /**
     * Looks up an image file matching the typed word.
     */
    fun findImageFile(word: String): File? {
        val key = word.trim().lowercase(Locale.ROOT)
        return imageRegistry[key]
    }

    /**
     * Asynchronously decodes and downsamples bitmap to prevent OOM on low-RAM TV chipsets.
     */
    suspend fun loadSampledBitmap(file: File, reqWidth: Int = 960, reqHeight: Int = 640): ImageBitmap? {
        return withContext(Dispatchers.IO) {
            try {
                // Defensive check: file exists, is readable, and is non-empty
                if (!file.exists() || !file.canRead() || file.length() == 0L) {
                    return@withContext null
                }

                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(file.absolutePath, options)

                // Guard against corrupt or unparseable images
                if (options.outWidth <= 0 || options.outHeight <= 0) {
                    return@withContext null
                }

                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
                options.inJustDecodeBounds = false
                // RGB_565 uses 2 bytes per pixel instead of 4 bytes, halving RAM usage
                options.inPreferredConfig = Bitmap.Config.RGB_565

                val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                bitmap?.asImageBitmap()
            } catch (e: Throwable) {
                Log.e(TAG, "Error decoding image ${file.name}: ${e.message}")
                null
            }
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
