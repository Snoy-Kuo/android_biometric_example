# biometric_example

An Android example App that shows how to use BiometricPrompt.<br/>
There are three options:

 - Need encryption: If checked, the user should first biometric reg with an input msg. One the encryption reg msg saved, the user can biometric auth.
 - Need strong auth: If checked, the user could only use BIOMETRIC_STRONG (Class 3) authenticators. Almost like restrict the user to fingerprint only. Pixel4 and Pixel4 XL are the only devices, that have Class3 face authentication.
 - Allow device credential: If checked, when biometric auth fail, it fallback to device's non-biometric credential, such as pattern, pin, password.

## Dev env

 - macOS 11.6 (Big Sur) x64
 - Android Studio Bumblebee Patch 1
 - Android SDK version 31
 - JDK: 11
 - Gradle: 7.2
 - Kotlin: 1.6.10

 ## References

 - [Show a biometric authentication dialog](https://developer.android.com/training/sign-in/biometric-auth)
 - [Login with Biometrics on Android](https://developer.android.com/codelabs/biometric-login)
 - [Android Biometric API: Getting Started](https://www.raywenderlich.com/18782293-android-biometric-api-getting-started)
 - [Which Android device has BIOMETRIC_STRONG (Class 3) face authentication?](https://stackoverflow.com/questions/68904823/which-android-device-has-biometric-strong-class-3-face-authentication)
 - [Face biometric testing in emulator?](https://www.reddit.com/r/androiddev/comments/dj4vmq/face_biometric_testing_in_emulator/)


 ## Libraries

 - [biometric](https://developer.android.com/jetpack/androidx/releases/biometric)
 - [lifecycle-runtime-ktx](https://developer.android.com/jetpack/androidx/releases/lifecycle)
 - [lifecycle-viewmodel-ktx](https://developer.android.com/jetpack/androidx/releases/lifecycle)


 ## Todos

 - add login flow example.
 - add transaction flow example.