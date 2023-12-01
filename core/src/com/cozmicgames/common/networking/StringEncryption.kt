package com.cozmicgames.common.networking

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec


object StringEncryption {
    fun generateKey(): ByteArray {
        val generator = KeyGenerator.getInstance("AES")
        generator.init(256)
        val key = generator.generateKey()
        return key.encoded
    }

    fun encrypt(value: String, key: ByteArray = NetworkConstants.STRING_ENCRYPTION_KEY): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(NetworkConstants.STRING_ENCRYPTION_IV))
        return Base64.getEncoder().encodeToString(cipher.doFinal(value.encodeToByteArray()))
    }

    fun decrypt(value: String, key: ByteArray = NetworkConstants.STRING_ENCRYPTION_KEY): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), IvParameterSpec(NetworkConstants.STRING_ENCRYPTION_IV))
        return String(cipher.doFinal(Base64.getDecoder().decode(value)))
    }
}
