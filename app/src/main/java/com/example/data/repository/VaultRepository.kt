package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.core.security.CryptoManager
import com.example.data.local.dao.VaultMediaDao
import com.example.data.local.entity.VaultMediaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class VaultRepository(
    private val vaultMediaDao: VaultMediaDao,
    private val cryptoManager: CryptoManager,
    private val context: Context
) {
    private val vaultDir = File(context.filesDir, "vault").apply {
        if (!exists()) mkdirs()
    }
    private val decryptedCacheDir = File(context.cacheDir, "vault_decrypted").apply {
        if (!exists()) mkdirs()
    }

    fun getAllMedia(): Flow<List<VaultMediaEntity>> = vaultMediaDao.getAllMedia()

    fun getMediaByKind(kind: String): Flow<List<VaultMediaEntity>> = vaultMediaDao.getMediaByKind(kind)

    suspend fun getMediaById(id: String): VaultMediaEntity? = vaultMediaDao.getMediaById(id)

    suspend fun saveEncryptedMedia(
        title: String,
        sourceBytes: ByteArray,
        mediaKind: String,
        durationMs: Long = 0L
    ): VaultMediaEntity = withContext(Dispatchers.IO) {
        val id = UUID.randomUUID().toString()
        val extension = when (mediaKind) {
            "PHOTO" -> "jpg"
            "VIDEO" -> "mp4"
            "AUDIO" -> "m4a"
            else -> "bin"
        }
        val encryptedFile = File(vaultDir, "$id.enc")
        val encryptedBytes = cryptoManager.encryptBytes(sourceBytes)
        encryptedFile.outputStream().use { it.write(encryptedBytes) }

        val entity = VaultMediaEntity(
            id = id,
            title = title,
            filePath = encryptedFile.absolutePath,
            mediaKind = mediaKind,
            durationMs = durationMs,
            fileSize = encryptedFile.length(),
            createdAt = System.currentTimeMillis(),
            isEncrypted = true
        )
        vaultMediaDao.insertMedia(entity)
        entity
    }

    suspend fun importFromUri(uri: Uri, title: String, kind: String): VaultMediaEntity? = withContext(Dispatchers.IO) {
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext null
            saveEncryptedMedia(title = title, sourceBytes = bytes, mediaKind = kind)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun decryptToCache(entity: VaultMediaEntity): File = withContext(Dispatchers.IO) {
        val extension = when (entity.mediaKind) {
            "PHOTO" -> "jpg"
            "VIDEO" -> "mp4"
            "AUDIO" -> "m4a"
            else -> "bin"
        }
        val cachedFile = File(decryptedCacheDir, "${entity.id}_dec.$extension")
        if (!cachedFile.exists() || cachedFile.length() == 0L) {
            val encryptedFile = File(entity.filePath)
            cryptoManager.decryptFile(encryptedFile, cachedFile)
        }
        cachedFile
    }

    suspend fun renameMedia(id: String, newTitle: String) {
        val media = vaultMediaDao.getMediaById(id)
        if (media != null) {
            vaultMediaDao.updateMedia(media.copy(title = newTitle))
        }
    }

    suspend fun deleteMedia(id: String) = withContext(Dispatchers.IO) {
        val media = vaultMediaDao.getMediaById(id)
        if (media != null) {
            File(media.filePath).delete()
            File(decryptedCacheDir, "${media.id}_dec.jpg").delete()
            File(decryptedCacheDir, "${media.id}_dec.mp4").delete()
            File(decryptedCacheDir, "${media.id}_dec.m4a").delete()
            vaultMediaDao.deleteMediaById(id)
        }
    }

    suspend fun clearDecryptedCache() = withContext(Dispatchers.IO) {
        decryptedCacheDir.listFiles()?.forEach { it.delete() }
    }

    suspend fun exportMedia(entity: VaultMediaEntity, targetUri: Uri): Boolean = withContext(Dispatchers.IO) {
        try {
            val cachedFile = decryptToCache(entity)
            context.contentResolver.openOutputStream(targetUri)?.use { out ->
                cachedFile.inputStream().use { input ->
                    input.copyTo(out)
                }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getStorageBreakdown(): Triple<Long, Long, Long> = withContext(Dispatchers.IO) {
        // Returns (photosBytes, videosBytes, audioBytes)
        var photos = 0L
        var videos = 0L
        var audio = 0L
        vaultDir.listFiles()?.forEach { file ->
            // query db to classify
        }
        Triple(photos, videos, audio)
    }
}
