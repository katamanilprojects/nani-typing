package com.manacdc.nanityping1

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * Level 2: Real-World Image Association Engine.
 *
 * Implements the "Filename-as-Trigger" zero-database pattern:
 * - Scans local directories, attached USB thumb drives, and Android MediaStore for
 *   images named after words (e.g. kodi.webp, hen.png, mamma.jpg).
 * - Matches words using case-insensitive lowercase comparison.
 * - Safely downsamples 1080p/4K images to RGB_565 bitmaps to prevent OOM on Android TV hardware.
 */
data class ImageSource(
    val file: File? = null,
    val uri: Uri? = null,
    val displayName: String
)

object ImageManager {
    private const val TAG = "ImageManager"
    private val SUPPORTED_EXTENSIONS = setOf("webp", "png", "jpg", "jpeg")

    // In-memory registry: normalized lowercase word -> ImageSource
    private var imageRegistry = mapOf<String, ImageSource>()

    /**
     * Checks if standard storage/media read permission is granted.
     */
    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Checks if All Files Access (MANAGE_EXTERNAL_STORAGE) is granted on Android 11+.
     */
    fun hasAllFilesAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    /**
     * Launches the system settings screen on Android TV to grant All Files Access for USB drives.
     */
    fun requestAllFilesAccess(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e2: Exception) {
                    Log.e(TAG, "Cannot launch all files settings: ${e2.message}")
                }
            }
        }
    }

    /**
     * Scans all accessible storage paths:
     * 1. Android MediaStore (indexes USB OTG images across all Android versions).
     * 2. App-specific storage on internal storage and USB drives (no runtime permissions required).
     * 3. Root of attached USB OTG storage drives (via StorageManager, /storage/XXXX-XXXX, etc.).
     * 4. Public /sdcard/NaniTyping/ folder.
     */
    fun reloadRegistry(context: Context): Int {
        val registry = mutableMapOf<String, ImageSource>()
        val targetDirs = mutableListOf<File>()

        // 🟢 1. MEDIASTORE INDEXING (Standard Android mechanism for USB drives)
        try {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATA
            )
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
            )
            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dataCol = c.getColumnIndex(MediaStore.Images.Media.DATA)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val displayName = c.getString(nameCol) ?: ""
                    val dataPath = if (dataCol >= 0) c.getString(dataCol) ?: "" else ""

                    // Check if file belongs to a NaniTyping directory (case-insensitive)
                    val isInNaniFolder = dataPath.contains("nanityping", ignoreCase = true) ||
                            displayName.contains("nanityping", ignoreCase = true)

                    if (isInNaniFolder) {
                        val ext = displayName.substringAfterLast('.', "").lowercase(Locale.ROOT)
                        if (ext in SUPPORTED_EXTENSIONS) {
                            val wordKey = displayName.substringBeforeLast('.').trim().lowercase(Locale.ROOT)
                            if (wordKey.isNotEmpty() && !registry.containsKey(wordKey)) {
                                val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                                val file = if (dataPath.isNotEmpty()) File(dataPath) else null
                                registry[wordKey] = ImageSource(file = file, uri = uri, displayName = displayName)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "MediaStore scan note: ${e.message}")
        }

        // 🟢 2. APP-SPECIFIC EXTERNAL STORAGE (Internal flash + connected USB drives)
        // Path: /storage/.../Android/data/com.manacdc.nanityping1/files/NaniTyping
        try {
            context.getExternalFilesDirs("NaniTyping")?.filterNotNull()?.forEach { dir ->
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                targetDirs.add(dir)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to inspect getExternalFilesDirs(NaniTyping): ${e.message}")
        }

        // 🟢 3. DERIVE USB ROOTS FROM getExternalFilesDirs(null)
        try {
            context.getExternalFilesDirs(null)?.filterNotNull()?.forEach { dir ->
                val path = dir.absolutePath
                if (path.contains("/Android/data/")) {
                    val volumeRoot = File(path.substringBefore("/Android/data/"))
                    checkAndAddNaniFolder(volumeRoot, targetDirs)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to inspect volume roots from getExternalFilesDirs: ${e.message}")
        }

        // 🟢 4. STORAGE MANAGER (API 24+ - Standard Android volume discovery)
        try {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as? StorageManager
            storageManager?.storageVolumes?.forEach { volume ->
                val rootDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    volume.directory
                } else {
                    try {
                        val getPathMethod = volume.javaClass.getMethod("getPath")
                        val pathStr = getPathMethod.invoke(volume) as? String
                        if (pathStr != null) File(pathStr) else null
                    } catch (e: Exception) {
                        null
                    }
                }
                if (rootDir != null && rootDir.exists()) {
                    checkAndAddNaniFolder(rootDir, targetDirs)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "StorageManager inspection note: ${e.message}")
        }

        // 🟢 5. DIRECT FILESYSTEM MOUNT POINTS (/storage, /mnt/media_rw, /mnt, /sdcard)
        listOf(
            File("/storage"),
            File("/mnt/media_rw"),
            File("/mnt"),
            File("/mnt/usb"),
            File("/storage/usbotg")
        ).forEach { root ->
            try {
                if (root.exists() && root.isDirectory) {
                    root.listFiles()?.forEach { volume ->
                        if (volume.isDirectory && volume.name != "self" && volume.name != "emulated") {
                            checkAndAddNaniFolder(volume, targetDirs)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Unable to inspect root ${root.path}: ${e.message}")
            }
        }

        // Standard public directory /sdcard/NaniTyping
        try {
            val publicDir = Environment.getExternalStorageDirectory()
            if (publicDir != null && publicDir.exists()) {
                checkAndAddNaniFolder(publicDir, targetDirs)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to inspect public external storage: ${e.message}")
        }

        // 🟢 6. INDEX ALL IMAGE FILES IN TARGET DIRECTORIES (Supporting subfolders up to depth 3)
        for (dir in targetDirs) {
            try {
                if (!dir.exists() || !dir.isDirectory) continue
                dir.walkTopDown().maxDepth(3).forEach { file ->
                    if (file.isFile && file.extension.lowercase(Locale.ROOT) in SUPPORTED_EXTENSIONS) {
                        val wordKey = file.nameWithoutExtension.trim().lowercase(Locale.ROOT)
                        if (wordKey.isNotEmpty() && !registry.containsKey(wordKey)) {
                            registry[wordKey] = ImageSource(file = file, displayName = file.name)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error walking directory ${dir.path}: ${e.message}")
            }
        }

        imageRegistry = registry
        Log.i(TAG, "Indexed ${registry.size} custom trigger images across ${targetDirs.size} directory locations.")
        return registry.size
    }

    private fun checkAndAddNaniFolder(parent: File, targetDirs: MutableList<File>) {
        if (!parent.exists() || !parent.isDirectory) return

        // 1. Direct parent matches NaniTyping (case-insensitive)
        if (parent.name.equals("nanityping", ignoreCase = true)) {
            if (!targetDirs.contains(parent)) targetDirs.add(parent)
            return
        }

        // 2. Direct subdirectories match NaniTyping (case-insensitive)
        try {
            parent.listFiles()?.forEach { child ->
                if (child.isDirectory && child.name.equals("nanityping", ignoreCase = true)) {
                    if (!targetDirs.contains(child)) {
                        targetDirs.add(child)
                    }
                }
            }
        } catch (e: Exception) {
            // Permission restricted subfolder
        }
    }

    /**
     * Looks up an image matching the typed word.
     */
    fun findImage(word: String): ImageSource? {
        val key = word.trim().lowercase(Locale.ROOT)
        return imageRegistry[key]
    }

    /**
     * Backward-compatible helper returning raw File if available.
     */
    fun findImageFile(word: String): File? {
        return findImage(word)?.file
    }

    /**
     * Asynchronously decodes and downsamples bitmap from ImageSource to prevent OOM on TV chipsets.
     */
    suspend fun loadSampledBitmap(
        context: Context,
        source: ImageSource,
        reqWidth: Int = 960,
        reqHeight: Int = 640
    ): ImageBitmap? {
        return withContext(Dispatchers.IO) {
            try {
                // Method A: Decode from direct File
                val file = source.file
                if (file != null && file.exists() && file.canRead() && file.length() > 0L) {
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeFile(file.absolutePath, options)
                    if (options.outWidth > 0 && options.outHeight > 0) {
                        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
                        options.inJustDecodeBounds = false
                        options.inPreferredConfig = Bitmap.Config.RGB_565

                        val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                        if (bitmap != null) {
                            return@withContext bitmap.asImageBitmap()
                        }
                    }
                }

                // Method B: Decode from Content URI (MediaStore / SAF)
                val uri = source.uri
                if (uri != null) {
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream, null, options)
                    }
                    if (options.outWidth > 0 && options.outHeight > 0) {
                        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
                        options.inJustDecodeBounds = false
                        options.inPreferredConfig = Bitmap.Config.RGB_565

                        context.contentResolver.openInputStream(uri)?.use { stream ->
                            val bitmap = BitmapFactory.decodeStream(stream, null, options)
                            if (bitmap != null) {
                                return@withContext bitmap.asImageBitmap()
                            }
                        }
                    }
                }

                null
            } catch (e: Throwable) {
                Log.e(TAG, "Error decoding image ${source.displayName}: ${e.message}")
                null
            }
        }
    }

    /**
     * Backward-compatible overload for File.
     */
    suspend fun loadSampledBitmap(file: File, reqWidth: Int = 960, reqHeight: Int = 640): ImageBitmap? {
        val dummySource = ImageSource(file = file, displayName = file.name)
        return withContext(Dispatchers.IO) {
            try {
                if (!file.exists() || !file.canRead() || file.length() == 0L) return@withContext null
                val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFile(file.absolutePath, options)
                if (options.outWidth <= 0 || options.outHeight <= 0) return@withContext null

                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
                options.inJustDecodeBounds = false
                options.inPreferredConfig = Bitmap.Config.RGB_565

                val bitmap = BitmapFactory.decodeFile(file.absolutePath, options)
                bitmap?.asImageBitmap()
            } catch (e: Throwable) {
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
