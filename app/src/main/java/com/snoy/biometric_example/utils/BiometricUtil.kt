package com.snoy.biometric_example.utils

import android.content.Context
import android.util.Log
import androidx.annotation.IntDef
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

//ref = https://www.raywenderlich.com/18782293-android-biometric-api-getting-started
object BiometricUtil {
    @IntDef(
        flag = true,
        value = [
            BiometricManager.Authenticators.BIOMETRIC_STRONG,
            BiometricManager.Authenticators.BIOMETRIC_WEAK,
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ]
    )
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    internal annotation class AuthenticationStatus

    private fun hasBiometricCapability(
        context: Context,
        @AuthenticationStatus authenticators: Int
    ): Int {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(authenticators)
    }

    @Suppress("unused")
    fun isBiometricReady(context: Context) =
        hasBiometricCapability(
            context,
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS

    fun isBiometricStrongReady(context: Context) =
        hasBiometricCapability(
            context,
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS

    private fun setBiometricPromptInfo(
        title: String,
        subtitle: String,
        description: String,
        negativeButton: String,
        allowDeviceCredential: Boolean,
        useStrongAuth: Boolean
    ): BiometricPrompt.PromptInfo {
        val builder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)

        // Use Device Credentials if allowed, otherwise show Cancel Button
        builder.apply {
            var auths = 0
            if (allowDeviceCredential) {
                auths = auths or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            } else {
                setNegativeButtonText(negativeButton)
            }
            auths = if (useStrongAuth) {
                (auths or BiometricManager.Authenticators.BIOMETRIC_STRONG)
            } else {
                (auths or BiometricManager.Authenticators.BIOMETRIC_WEAK)
            }

            setAllowedAuthenticators(auths)
        }

        return builder.build()
    }

    private fun initBiometricPrompt(
        activity: FragmentActivity,
        listener: BiometricAuthListener
    ): BiometricPrompt {
        // 1
        val executor = ContextCompat.getMainExecutor(activity)

        // 2
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                listener.onBiometricAuthenticationError(errorCode, errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.w(this.javaClass.simpleName, "Authentication failed for an unknown reason")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                listener.onBiometricAuthenticationSuccess(result)
            }
        }

        // 3
        return BiometricPrompt(activity, executor, callback)
    }

    fun showBiometricPrompt(
        title: String = "Biometric Authentication",
        subtitle: String = "Enter biometric credentials to proceed.",
        description: String = "Input your Fingerprint or FaceID to ensure it's you!",
        negativeButton: String = "Cancel",
        activity: FragmentActivity,
        listener: BiometricAuthListener,
        cryptoObject: BiometricPrompt.CryptoObject? = null,
        allowDeviceCredential: Boolean = false,
        useStrongAuth: Boolean = false
    ) {
        // 1
        val promptInfo = setBiometricPromptInfo(
            title,
            subtitle,
            description,
            negativeButton = negativeButton,
            allowDeviceCredential = allowDeviceCredential,
            useStrongAuth = useStrongAuth
        )

        // 2
        val biometricPrompt = initBiometricPrompt(activity, listener)

        // 3
        biometricPrompt.apply {
            if (cryptoObject == null) authenticate(promptInfo)
            else authenticate(promptInfo, cryptoObject)
        }
    }

}

interface BiometricAuthListener {
    fun onBiometricAuthenticationError(errorCode: Int, errString: String)
    fun onBiometricAuthenticationSuccess(result: BiometricPrompt.AuthenticationResult)
}
