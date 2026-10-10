package com.example.antigravity.config

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Encrypts secret configuration values at rest using an AES-256/GCM key held in the
 * Android Keystore. Non-secret values pass through untouched.
 *
 * On the JVM (unit tests) the Android Keystore is unavailable, so [encrypt] and
 * [decrypt] degrade to a transparent pass-through. That keeps persistence tests
 * deterministic without ever weakening on-device storage.
 *
 * Stored format: `enc:v1:<base64(iv)>:<base64(ciphertext)>`.
 */
object SecureValueStore {

    private const val TAG = "SecureValueStore"
    private const val PREFIX = "enc:v1:"
    private const val KEY_ALIAS = "antigravity.secrets.v1"
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_BITS = 128

    /** Keys whose values should be encrypted before they touch disk. */
    fun isSecretKey(key: String): Boolean {
        val k = key.uppercase()
        return k.contains("TOKEN") ||
            k.contains("SECRET") ||
            k.contains("PASSWORD") ||
            k.contains("API_KEY") ||
            k.contains("APIKEY") ||
            k.contains("BEARER") ||
            k.endsWith("_KEY")
    }

    fun isEncrypted(value: String): Boolean = value.startsWith(PREFIX)

    fun encrypt(plain: String): String {
        if (plain.isBlank() || isEncrypted(plain) || !keystoreAvailable()) return plain
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
            val cipherText = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
            PREFIX +
                Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" +
                Base64.encodeToString(cipherText, Base64.NO_WRAP)
        } catch (t: Throwable) {
            Log.w(TAG, "Encryption failed, storing value unencrypted: ${t.message}")
            plain
        }
    }

    fun decrypt(stored: String): String {
        if (!isEncrypted(stored)) return stored
        if (!keystoreAvailable()) return ""
        return try {
            val parts = stored.removePrefix(PREFIX).split(":", limit = 2)
            if (parts.size != 2) return ""
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val cipherText = Base64.decode(parts[1], Base64.NO_WRAP)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            String(cipher.doFinal(cipherText), Charsets.UTF_8)
        } catch (t: Throwable) {
            Log.w(TAG, "Decryption failed: ${t.message}")
            ""
        }
    }

    fun decryptOrSelf(value: String): String = if (isEncrypted(value)) decrypt(value) else value

    private fun keystoreAvailable(): Boolean = try {
        KeyStore.getInstance(ANDROID_KEYSTORE).load(null)
        true
    } catch (t: Throwable) {
        false
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }
}
