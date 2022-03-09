package com.snoy.biometric_example.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _needStrongAuth: MutableLiveData<Boolean> by lazy {
        MutableLiveData(false)
    }
    val needStrongAuth: LiveData<Boolean> = _needStrongAuth

    private val _allowDeviceCredential: MutableLiveData<Boolean> by lazy {
        MutableLiveData(true)
    }
    val allowDeviceCredential: LiveData<Boolean> = _allowDeviceCredential

    fun setNeedStrongAuth(value: Boolean) {
        _needStrongAuth.value = value
    }

    fun setAllowDeviceCredential(value: Boolean) {
        _allowDeviceCredential.value = value
    }
}