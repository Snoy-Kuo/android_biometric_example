package com.snoy.biometric_example.utils

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.RequiresApi
import com.snoy.biometric_example.common.EncryptedMessage
import java.nio.charset.Charset
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

//ref = https://www.raywenderlich.com/18782293-android-biometric-api-getting-started
//      https://developer.android.com/training/sign-in/biometric-auth
object CryptographyUtil {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val YOUR_SECRET_KEY_NAME = "Y0UR$3CR3TK3YN@M3" //"Y0UR$3CR3TK3YN@M3CBC"
    private const val KEY_SIZE = 128

    @RequiresApi(Build.VERSION_CODES.M)
    private const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM //BLOCK_MODE_CBC

    @RequiresApi(Build.VERSION_CODES.M)
    private const val ENCRYPTION_PADDING =
        KeyProperties.ENCRYPTION_PADDING_NONE// ENCRYPTION_PADDING_PKCS7

    @RequiresApi(Build.VERSION_CODES.M)
    private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES

    @RequiresApi(Build.VERSION_CODES.M)
    fun getOrCreateSecretKey(keyName: String): SecretKey {
        // 1
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null) // Keystore must be loaded before it can be accessed
        keyStore.getKey(keyName, null)?.let { return it as SecretKey }

        // 2
        val paramsBuilder = KeyGenParameterSpec.Builder(
            keyName,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
        paramsBuilder.apply {
            setBlockModes(ENCRYPTION_BLOCK_MODE)
            setEncryptionPaddings(ENCRYPTION_PADDING)
            setKeySize(KEY_SIZE)
            setUserAuthenticationRequired(true)
        }

        // 3
        val keyGenParams = paramsBuilder.build()
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )
        keyGenerator.init(keyGenParams)

        return keyGenerator.generateKey()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"

        return Cipher.getInstance(transformation)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getInitializedCipherForEncryption(): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(YOUR_SECRET_KEY_NAME)
        try {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        } catch (e: KeyPermanentlyInvalidatedException) {//add or remove biometric
            Log.e("RDTest", "getInitializedCipherForEncryption e= $e")
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null) // Keystore must be loaded before it can be accessed
            keyStore.deleteEntry(YOUR_SECRET_KEY_NAME)
            val secretKey2 = getOrCreateSecretKey(YOUR_SECRET_KEY_NAME)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey2)
        }
        return cipher
    }

    fun encryptData(plaintext: String, cipher: Cipher): EncryptedMessage {
        val ciphertext = cipher
            .doFinal(plaintext.toByteArray(Charset.forName("UTF-8")))
        return EncryptedMessage(ciphertext, cipher.iv)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun getInitializedCipherForDecryption(
        initializationVector: ByteArray? = null
    ): Cipher {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(YOUR_SECRET_KEY_NAME)
        cipher.init(
            Cipher.DECRYPT_MODE,
            secretKey,
            GCMParameterSpec(KEY_SIZE, initializationVector)
//            IvParameterSpec(initializationVector)
        )

        return cipher
    }

    fun decryptData(ciphertext: ByteArray, cipher: Cipher): String {
        val plaintext = cipher.doFinal(ciphertext)
        return String(plaintext, Charset.forName("UTF-8"))
    }

}