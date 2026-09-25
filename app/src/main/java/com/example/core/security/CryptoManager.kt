package com.example.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManager {

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "SecureLensVaultMasterKey"
        private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"
        private const val TAG_LENGTH_BITS = 128
        private const val IV_LENGTH_BYTES = 12
    }

    private val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    private fun getOrCreateSecretKey(): SecretKey {
        if (keyStore.containsAlias(KEY_ALIAS)) {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            if (entry != null) {
                return entry.secretKey
            }
        }

        val keyGenerator = KeyGenerator.getInstance(ALGORITHM, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(BLOCK_MODE)
            .setEncryptionPaddings(PADDING)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun encryptBytes(plainBytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(plainBytes)
        // Prepend IV to ciphertext
        return iv + encryptedData
    }

    fun decryptBytes(encryptedBytesWithIv: ByteArray): ByteArray {
        if (encryptedBytesWithIv.size < IV_LENGTH_BYTES) {
            throw IllegalArgumentException("Invalid encrypted payload")
        }
        val iv = encryptedBytesWithIv.copyOfRange(0, IV_LENGTH_BYTES)
        val encryptedData = encryptedBytesWithIv.copyOfRange(IV_LENGTH_BYTES, encryptedBytesWithIv.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(TAG_LENGTH_BITS, iv)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateSecretKey(), spec)
        return cipher.doFinal(encryptedData)
    }

    fun encryptFile(inputFile: File, outputFile: File) {
        val bytes = inputFile.readBytes()
        val encrypted = encryptBytes(bytes)
        outputFile.outputStream().use { it.write(encrypted) }
    }

    fun decryptFile(encryptedFile: File, outputFile: File) {
        val bytes = encryptedFile.readBytes()
        val decrypted = decryptBytes(bytes)
        outputFile.outputStream().use { it.write(decrypted) }
    }
}
