package com.manacdc.nanityping1

import android.content.ContentUris
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.util.Locale

/**
 * Level 2+: Real-World Custom Voice & Sound Engine.
 *
 * Implements the "Filename-as-Trigger" pattern for recorded human audio:
 * - Scans local directories, attached USB thumb drives, and Android MediaStore for
 *   recorded voice clips named after words (e.g. amma.m4a, amma.mp3, kodi.wav, nanna.ogg).
 * - Matches words using case-insensitive lowercase comparison.
 * - Plays via low-latency USAGE_ASSISTANCE_ACCESSIBILITY stream (same as TTS).
 * - Silent fallback to synthetic TTS if no custom voice recording exists.
 */
data class SoundEntry(
    val file: File? = null,
    val uri: Uri? = null,
    val displayName: String
)

object SoundManager {
    private const val TAG = "SoundManager"
    private val SUPPORTED_EXTENSIONS = setOf("m4a", "mp3", "wav", "ogg", "aac", "3gp", "flac")

    // In-memory registry: normalized lowercase word -> SoundEntry
    private var soundRegistry = mapOf<String, SoundEntry>()

    // Active media player instance
    private var activePlayer: MediaPlayer? = null

    /**
     * Looks up a custom sound entry matching the typed word.
     */
    fun findSound(word: String): SoundEntry? {
        val key = word.trim().lowercase(Locale.ROOT)
        return soundRegistry[key]
    }

    /**
     * Checks if a custom sound exists for this word.
     */
    fun hasSound(word: String): Boolean {
        return findSound(word) != null
    }

    /**
     * Stops and releases any currently playing audio immediately.
     */
    fun stop() {
        try {
            activePlayer?.let { player ->
                if (player.isPlaying) {
                    player.stop()
                }
                player.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Exception stopping sound: ${e.message}")
        } finally {
            activePlayer = null
        }
    }

    /**
     * Cleanly releases all audio resources on app exit.
     */
    fun release() {
        stop()
    }

    /**
     * Plays the custom recorded voice with zero perceivable latency.
     * Uses USAGE_ASSISTANCE_ACCESSIBILITY audio stream to prevent TV HDMI power-saving mute.
     */
    fun play(
        context: Context,
        sound: SoundEntry,
        onCompletion: (() -> Unit)? = null,
        onError: (() -> Unit)? = null
    ) {
        stop()

        try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            val player = MediaPlayer().apply {
                setAudioAttributes(audioAttributes)

                if (sound.file != null && sound.file.exists() && sound.file.canRead()) {
                    setDataSource(sound.file.absolutePath)
                } else if (sound.uri != null) {
                    setDataSource(context, sound.uri)
                } else {
                    onError?.invoke() ?: onCompletion?.invoke()
                    return
                }

                setOnPreparedListener { mp ->
                    try {
                        mp.start()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting playback: ${e.message}")
                        onError?.invoke() ?: onCompletion?.invoke()
                    }
                }

                setOnCompletionListener { mp ->
                    try {
                        mp.release()
                    } catch (e: Exception) {}
                    if (activePlayer == mp) activePlayer = null
                    onCompletion?.invoke()
                }

                setOnErrorListener { mp, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra for ${sound.displayName}")
                    try {
                        mp.release()
                    } catch (e: Exception) {}
                    if (activePlayer == mp) activePlayer = null
                    onError?.invoke() ?: onCompletion?.invoke()
                    true
                }

                prepareAsync()
            }

            activePlayer = player
        } catch (e: Throwable) {
            Log.e(TAG, "Error preparing custom audio ${sound.displayName}: ${e.message}")
            stop()
            onError?.invoke() ?: onCompletion?.invoke()
        }
    }

    /**
     * Scans all accessible storage paths for custom voice recordings.
     */
    fun reloadRegistry(context: Context): Int {
        val registry = mutableMapOf<String, SoundEntry>()
        val targetDirs = mutableListOf<File>()

        // 🟢 1. MEDIASTORE INDEXING (Standard Android mechanism for USB audio)
        try {
            val projection = arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.DISPLAY_NAME,
                MediaStore.Audio.Media.DATA
            )
            val cursor = context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
            )
            cursor?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val dataCol = c.getColumnIndex(MediaStore.Audio.Media.DATA)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val displayName = c.getString(nameCol) ?: ""
                    val dataPath = if (dataCol >= 0) c.getString(dataCol) ?: "" else ""

                    // Check if file is inside a NaniTyping or sounds folder (case-insensitive)
                    val isInNaniFolder = dataPath.contains("nanityping", ignoreCase = true) ||
                            displayName.contains("nanityping", ignoreCase = true)

                    if (isInNaniFolder) {
                        val ext = displayName.substringAfterLast('.', "").lowercase(Locale.ROOT)
                        if (ext in SUPPORTED_EXTENSIONS) {
                            val wordKey = displayName.substringBeforeLast('.').trim().lowercase(Locale.ROOT)
                            if (wordKey.isNotEmpty() && !registry.containsKey(wordKey)) {
                                val uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                                val file = if (dataPath.isNotEmpty()) File(dataPath) else null
                                registry[wordKey] = SoundEntry(file = file, uri = uri, displayName = displayName)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "MediaStore audio scan note: ${e.message}")
        }

        // 🟢 2. APP-SPECIFIC EXTERNAL STORAGE (Internal flash + connected USB drives)
        try {
            context.getExternalFilesDirs("NaniTyping")?.filterNotNull()?.forEach { dir ->
                if (!dir.exists()) dir.mkdirs()
                targetDirs.add(dir)
                listOf("sounds", "sound", "audio", "voice", "voices").forEach { sub ->
                    val subDir = File(dir, sub)
                    if (subDir.exists() && subDir.isDirectory) targetDirs.add(subDir)
                }
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
                    checkAndAddAudioFolders(volumeRoot, targetDirs)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to inspect volume roots for audio: ${e.message}")
        }

        // 🟢 4. STORAGE MANAGER (API 24+)
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
                    checkAndAddAudioFolders(rootDir, targetDirs)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "StorageManager audio inspection note: ${e.message}")
        }

        // 🟢 5. DIRECT FILESYSTEM MOUNT POINTS
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
                            checkAndAddAudioFolders(volume, targetDirs)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Unable to inspect audio root ${root.path}: ${e.message}")
            }
        }

        // Standard public directory /sdcard/NaniTyping
        try {
            val publicDir = Environment.getExternalStorageDirectory()
            if (publicDir != null && publicDir.exists()) {
                checkAndAddAudioFolders(publicDir, targetDirs)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to inspect public external storage for audio: ${e.message}")
        }

        // 🟢 6. INDEX ALL AUDIO FILES IN TARGET DIRECTORIES (Supporting subfolders up to depth 4)
        for (dir in targetDirs) {
            try {
                if (!dir.exists() || !dir.isDirectory) continue
                dir.walkTopDown().maxDepth(4).forEach { file ->
                    if (file.isFile && file.extension.lowercase(Locale.ROOT) in SUPPORTED_EXTENSIONS) {
                        val wordKey = file.nameWithoutExtension.trim().lowercase(Locale.ROOT)
                        if (wordKey.isNotEmpty() && !registry.containsKey(wordKey)) {
                            registry[wordKey] = SoundEntry(file = file, displayName = file.name)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error walking audio directory ${dir.path}: ${e.message}")
            }
        }

        soundRegistry = registry
        Log.i(TAG, "Indexed ${registry.size} custom voice recordings across ${targetDirs.size} locations.")
        return registry.size
    }

    private fun checkAndAddAudioFolders(parent: File, targetDirs: MutableList<File>) {
        if (!parent.exists() || !parent.isDirectory) return

        if (parent.name.equals("nanityping", ignoreCase = true)) {
            if (!targetDirs.contains(parent)) targetDirs.add(parent)
            return
        }

        try {
            parent.listFiles()?.forEach { child ->
                if (child.isDirectory && child.name.equals("nanityping", ignoreCase = true)) {
                    if (!targetDirs.contains(child)) {
                        targetDirs.add(child)
                    }
                }
            }
        } catch (e: Exception) {}
    }
}
