package com.snoy.biometric_example.ui.main

import com.snoy.biometric_example.common.EncryptedMessage

sealed interface MainState

sealed interface NeedEncryption : MainState {
    object NeedEncryptionInit : NeedEncryption
    data class NeedEncryptNotSupport(val warning: String) : NeedEncryption
    data class NeedEncryptStart(val inputMsg: String) : NeedEncryption
    data class NeedEncryptShowEncryptPrompt(val inputMsg: String) : NeedEncryption
    data class NeedEncryptionEncrypted(val inputMsg: String, val encryptedMsg: EncryptedMessage?) :
        NeedEncryption

    data class NeedEncryptEncryptError(val inputMsg: String, val errString: String) : NeedEncryption
    data class NeedEncryptShowDecryptPrompt(
        val inputMsg: String,
        val encryptedMsg: EncryptedMessage?
    ) : NeedEncryption

    data class NeedEncryptDecryptError(val inputMsg: String, val errString: String) : NeedEncryption

    data class NeedEncryptionDecrypted(
        val inputMsg: String,
        val encryptedMsg: EncryptedMessage,
        val decryptedMsg: String
    ) :
        NeedEncryption
}

data class NoEncryptionInit(val needStrongAuth: Boolean, val allowDeviceCredential: Boolean) :
    MainState

data class NotSupportStrong(val allowDeviceCredential: Boolean, val warning: String) : MainState

data class NotSupportWeak(val allowDeviceCredential: Boolean, val warning: String) : MainState