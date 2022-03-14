package com.snoy.biometric_example.ui.main

import android.content.Context
import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snoy.biometric_example.common.EncryptedMessage
import com.snoy.biometric_example.utils.BiometricReadyStatus
import com.snoy.biometric_example.utils.BiometricUtil
import com.snoy.biometric_example.utils.CryptographyUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.crypto.Cipher

class MainViewModel : ViewModel() {
    private val _mainState: MutableLiveData<MainState> by lazy {
        MutableLiveData(NeedEncryption.NeedEncryptionInit)
    }
    val mainState: LiveData<MainState> = _mainState

    var encryptMsg: EncryptedMessage? = null
        private set

    fun setNeedEncrypt(
        needEncryption: Boolean,
        needStrong: Boolean,
        allowDeviceCredential: Boolean
    ) {
        if (needEncryption) {
            _mainState.value = NeedEncryption.NeedEncryptionInit
        } else {
            _mainState.value = NoEncryptionInit(needStrong, allowDeviceCredential)
        }
    }

    fun checkBiometricSupport(context: Context, needStrong: Boolean) {
        val isBiometricReady: BiometricReadyStatus
        if (needStrong) {
            isBiometricReady = BiometricUtil.isBiometricStrongReady(context)
            if (isBiometricReady is BiometricReadyStatus.BiometricNotReady) {
                if (_mainState.value is NeedEncryption)
                    _mainState.value =
                        NeedEncryption.NeedEncryptNotSupport("Biometric Strong is not ready. ${isBiometricReady.msg}")
                else if (_mainState.value is NoEncryptionInit) {
                    val currentState = _mainState.value as NoEncryptionInit
                    _mainState.value = NotSupportStrong(
                        currentState.allowDeviceCredential,
                        "Biometric Strong is not ready. ${isBiometricReady.msg}"
                    )
                }
            }
        } else {
            isBiometricReady = BiometricUtil.isBiometricReady(context)
            if (isBiometricReady is BiometricReadyStatus.BiometricNotReady) {
                if (_mainState.value is NoEncryptionInit) {
                    val currentState = _mainState.value as NoEncryptionInit
                    _mainState.value = NotSupportWeak(
                        currentState.allowDeviceCredential,
                        "Biometric is not ready. ${isBiometricReady.msg}"
                    )
                }
            }
        }
    }

    private fun startEncryption(inputMsg: String) {
        if (_mainState.value !is NeedEncryption.NeedEncryptionInit) {
            Log.e("RDTest", "wrong usage, current state= ${_mainState.value}")
            return
        }

        _mainState.value = NeedEncryption.NeedEncryptStart(inputMsg)
    }

    private fun storeEncryptedMessage(input: String, msg: EncryptedMessage) { //TODO: impl store
        viewModelScope.launch {
            delay(1000)
            encryptMsg = msg
            _mainState.value = NeedEncryption.NeedEncryptionEncrypted(input, msg)
        }
    }

    fun startBiometricReg(input: String) {
        if (supportEncrypt()) {
            onClickEncryptMessage(input)
        }
    }

    private fun supportEncrypt(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    private suspend fun clearEncryptedMessage() { //TODO: impl clear
        delay(100)
        encryptMsg = null
    }

    private fun onClickEncryptMessage(message: String) {
        startEncryption(message)
        viewModelScope.launch {
            clearEncryptedMessage()

            if (!TextUtils.isEmpty(message.trim())) {
                _mainState.value = NeedEncryption.NeedEncryptShowEncryptPrompt(message)
            } else {
                _mainState.value =
                    NeedEncryption.NeedEncryptEncryptError("", "input msg cannot be empty.")
            }
        }
    }

    fun onBioRegAuthError(input: String, errorCode: Int, errString: String) {
        _mainState.value = NeedEncryption.NeedEncryptEncryptError(
            input,
            "Biometric reg error: \n [$errorCode] $errString."
        )
    }

    fun onBioRegSuccess(input: String, result: BiometricPrompt.AuthenticationResult) {
        result.cryptoObject?.cipher?.let {
            if (!TextUtils.isEmpty(input)) {
                encryptAndSave(input, it)
            }
        }
    }

    private fun encryptAndSave(input: String, cipher: Cipher) {
        val encryptedMessage: EncryptedMessage
        try {
            encryptedMessage = CryptographyUtil.encryptData(input, cipher)
            storeEncryptedMessage(input, encryptedMessage)
        } catch (e: Exception) {
            _mainState.value =
                NeedEncryption.NeedEncryptEncryptError(input, "Biometric reg Fail, e= $e")
        }
    }

    fun onClickDecryptMessage(inputMsg: String) {
        _mainState.value = NeedEncryption.NeedEncryptShowDecryptPrompt(inputMsg, encryptMsg)
    }

    fun onBioAuthError(input: String, errorCode: Int, errString: String) {
        _mainState.value = NeedEncryption.NeedEncryptDecryptError(
            input,
            "Biometric auth error: \n [$errorCode] $errString."
        )
    }

    fun onBioAuthSuccess(input: String, result: BiometricPrompt.AuthenticationResult) {
        result.cryptoObject?.cipher?.let {
            if (!TextUtils.isEmpty(input)) {
                decryptAndDisplay(input, it)
            }
        }
    }

    private fun decryptAndDisplay(input: String, cipher: Cipher) {
        encryptMsg?.ciphertext?.let { it ->
            val decryptedMessage = CryptographyUtil.decryptData(it, cipher)
            if (input == decryptedMessage) {
                _mainState.value =
                    NeedEncryption.NeedEncryptionDecrypted(input, encryptMsg!!, decryptedMessage)
            } else {
                _mainState.value = NeedEncryption.NeedEncryptDecryptError(
                    input,
                    "Biometric Authentication Fail, input and stored msg not equal"
                )
            }
        }
    }
}