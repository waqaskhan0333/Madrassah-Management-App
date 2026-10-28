package com.example.util

import android.content.Context
import android.net.Uri
import java.io.File

object ImageStorageHelper {
    /**
     * Copies an image from content URI (e.g. Photo Picker / SAF) into internal app storage
     * so it never expires, works 100% offline, and doesn't require runtime storage permissions.
     */
    fun saveImageToInternalStorage(
        context: Context,
        sourceUri: Uri,
        subFolder: String,
        prefix: String
    ): String? {
        return try {
            val directory = File(context.filesDir, subFolder).apply {
                if (!exists()) mkdirs()
            }
            val targetFile = File(directory, "${prefix}_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                targetFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            targetFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Converts a stored file path or URI string to an object that Coil AsyncImage
     * can load reliably without protocol/path resolution errors.
     */
    fun toImageModel(uriOrPath: String?): Any? {
        if (uriOrPath.isNullOrBlank()) return null
        return try {
            when {
                uriOrPath.startsWith("/") -> File(uriOrPath)
                uriOrPath.startsWith("file://") -> File(uriOrPath.removePrefix("file://"))
                uriOrPath.startsWith("content://") -> Uri.parse(uriOrPath)
                else -> File(uriOrPath)
            }
        } catch (e: Exception) {
            uriOrPath
        }
    }

    /**
     * Converts a local file to Base64 string for embedding images inside JSON backups.
     */
    fun fileToBase64(pathOrUri: String?): String? {
        if (pathOrUri.isNullOrBlank()) return null
        return try {
            val file = when {
                pathOrUri.startsWith("file://") -> File(pathOrUri.removePrefix("file://"))
                pathOrUri.startsWith("/") -> File(pathOrUri)
                else -> File(pathOrUri)
            }
            if (file.exists() && file.isFile) {
                val bytes = file.readBytes()
                android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Recreates an image file on device storage from a Base64 string when restoring a backup.
     */
    fun base64ToFile(
        context: Context,
        base64String: String?,
        subFolder: String,
        prefix: String
    ): String? {
        if (base64String.isNullOrBlank()) return null
        return try {
            val bytes = android.util.Base64.decode(base64String, android.util.Base64.NO_WRAP)
            val directory = File(context.filesDir, subFolder).apply {
                if (!exists()) mkdirs()
            }
            val targetFile = File(directory, "${prefix}_${System.currentTimeMillis()}.jpg")
            targetFile.writeBytes(bytes)
            targetFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Retrieves all image folders inside the app's internal files directory.
     */
    fun getAllFolders(context: Context): List<String> {
        val defaultFolders = mutableListOf("student_photos", "teacher_photos", "madrassa_logos", "reports")
        val filesDir = context.filesDir ?: return defaultFolders
        val dirs = filesDir.listFiles { file -> file.isDirectory } ?: emptyArray()
        for (dir in dirs) {
            if (!defaultFolders.contains(dir.name)) {
                defaultFolders.add(dir.name)
            }
        }
        return defaultFolders
    }

    /**
     * Creates a new subfolder in internal app storage.
     */
    fun createFolder(context: Context, folderName: String): Boolean {
        val clean = folderName.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
        if (clean.isBlank()) return false
        val dir = File(context.filesDir, clean)
        return if (!dir.exists()) dir.mkdirs() else true
    }

    /**
     * Renames a folder in internal app storage.
     */
    fun renameFolder(context: Context, oldName: String, newName: String): Boolean {
        val clean = newName.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_")
        if (clean.isBlank() || oldName == clean) return false
        val oldDir = File(context.filesDir, oldName)
        val newDir = File(context.filesDir, clean)
        return if (oldDir.exists() && !newDir.exists()) {
            oldDir.renameTo(newDir)
        } else false
    }

    /**
     * Deletes a folder and all its files from internal storage.
     */
    fun deleteFolder(context: Context, folderName: String): Boolean {
        val dir = File(context.filesDir, folderName)
        return if (dir.exists()) {
            dir.deleteRecursively()
        } else false
    }

    /**
     * Deletes a specific file from internal storage.
     */
    fun deleteFile(file: File): Boolean {
        return try {
            if (file.exists()) file.delete() else false
        } catch (e: Exception) {
            false
        }
    }

    data class StoredPhotoInfo(
        val file: File,
        val folder: String,
        val fileName: String,
        val path: String,
        val sizeBytes: Long,
        val lastModified: Long
    )

    /**
     * Lists all photo files in a given subfolder.
     */
    fun getPhotosInFolder(context: Context, folderName: String): List<StoredPhotoInfo> {
        return try {
            val dir = File(context.filesDir, folderName)
            if (!dir.exists() || !dir.isDirectory) return emptyList()
            dir.listFiles { f ->
                val name = f.name.lowercase()
                f.isFile && (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") || name.endsWith(".webp"))
            }?.map { file ->
                StoredPhotoInfo(
                    file = file,
                    folder = folderName,
                    fileName = file.name,
                    path = file.absolutePath,
                    sizeBytes = file.length(),
                    lastModified = file.lastModified()
                )
            }?.sortedByDescending { it.lastModified } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Computes the total byte size of stored images across all default image folders.
     */
    fun getTotalStorageSizeBytes(context: Context): Long {
        val folders = listOf("student_photos", "teacher_photos", "madrassa_logos", "reports")
        var total = 0L
        for (folder in folders) {
            val list = getPhotosInFolder(context, folder)
            total += list.sumOf { it.sizeBytes }
        }
        return total
    }

    /**
     * Formats bytes to human-readable string (e.g. 1.2 MB or 450 KB).
     */
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f MB", bytes / (1024f * 1024f))
            bytes >= 1024 -> String.format(java.util.Locale.US, "%.1f KB", bytes / 1024f)
            else -> "$bytes B"
        }
    }
}
