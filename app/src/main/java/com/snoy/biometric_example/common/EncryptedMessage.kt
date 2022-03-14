package com.snoy.biometric_example.common

data class EncryptedMessage(val ciphertext: ByteArray, val iv: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedMessage

        if (!ciphertext.contentEquals(other.ciphertext)) return false
        if (!iv.contentEquals(other.iv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ciphertext.hashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}
