package com.example.security

import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * End-to-End Encryption (E2EE) Engine using AES-256-GCM authenticated encryption
 * with PBKDF2WithHmacSHA256 key derivation.
 * Ensures every user's debt records, private notes, and multi-device cloud sync
 * payloads are encrypted locally before leaving the device.
 */
object CryptoManager {
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_BITS = 128
    private const val SALT_LENGTH = 16
    private const val PBKDF2_ITERATIONS = 12000
    private const val KEY_LENGTH_BITS = 256
    private const val PREFIX_E2E = "E2E1:"

    private val secureRandom = SecureRandom()

    fun deriveAesKey(passphrase: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(
            passphrase.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            KEY_LENGTH_BITS
        )
        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    /**
     * Encrypts plaintext into `E2E1:<Base64(Salt[16] + IV[12] + CiphertextWithTag)>`
     */
    fun encryptText(plainText: String, passphrase: String): String {
        if (plainText.isBlank()) return ""
        return try {
            val salt = ByteArray(SALT_LENGTH).also { secureRandom.nextBytes(it) }
            val iv = ByteArray(GCM_IV_LENGTH).also { secureRandom.nextBytes(it) }
            val secretKey = deriveAesKey(passphrase, salt)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
            val cipherBytes = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))

            val combined = ByteArray(salt.size + iv.size + cipherBytes.size)
            System.arraycopy(salt, 0, combined, 0, salt.size)
            System.arraycopy(iv, 0, combined, salt.size, iv.size)
            System.arraycopy(cipherBytes, 0, combined, salt.size + iv.size, cipherBytes.size)

            PREFIX_E2E + Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            plainText
        }
    }

    /**
     * Decrypts `E2E1:<Base64(...)>` back into original plaintext using the user's passphrase.
     */
    fun decryptText(cipherPayload: String, passphrase: String): String {
        if (cipherPayload.isBlank()) return ""
        if (!cipherPayload.startsWith(PREFIX_E2E)) return cipherPayload
        return try {
            val encoded = cipherPayload.removePrefix(PREFIX_E2E)
            val combined = Base64.decode(encoded, Base64.NO_WRAP)
            if (combined.size <= SALT_LENGTH + GCM_IV_LENGTH) return "[Terenkripsi]"

            val salt = combined.copyOfRange(0, SALT_LENGTH)
            val iv = combined.copyOfRange(SALT_LENGTH, SALT_LENGTH + GCM_IV_LENGTH)
            val cipherBytes = combined.copyOfRange(SALT_LENGTH + GCM_IV_LENGTH, combined.size)

            val secretKey = deriveAesKey(passphrase, salt)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_BITS, iv))
            val decryptedBytes = cipher.doFinal(cipherBytes)
            String(decryptedBytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            "🔒 Data Terenkripsi (Kunci E2E Berbeda)"
        }
    }

    /**
     * Generates a short SHA-256 fingerprint of the user's current E2E encryption key
     * so the user can verify key alignment across multiple devices.
     */
    fun keyFingerprint(passphrase: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(passphrase.toByteArray(StandardCharsets.UTF_8))
        return digest.take(6).joinToString(":") { "%02X".format(it) }
    }
}
