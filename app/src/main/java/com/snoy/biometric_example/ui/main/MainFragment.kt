package com.snoy.biometric_example.ui.main

import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.snoy.biometric_example.common.EncryptedMessage
import com.snoy.biometric_example.databinding.MainFragmentBinding
import com.snoy.biometric_example.utils.BiometricAuthListener
import com.snoy.biometric_example.utils.BiometricUtil
import com.snoy.biometric_example.utils.CryptographyUtil

//ref= https://developer.android.com/training/sign-in/biometric-auth
// https://developer.android.com/codelabs/biometric-login
class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        viewModel.mainState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is NeedEncryption.NeedEncryptionInit -> {
                    binding.checkNeedEncrypt.isChecked = true
                    binding.checkNeedStrongAuth.isChecked = true
                    binding.checkAllowDeviceCredential.isChecked = false
                    binding.inputLayout.hint = "Enter message to encrypt"
                    binding.inputText.setText("")
                    binding.inputLayout.visibility = View.VISIBLE
                    binding.inputText.isEnabled = true
                    binding.cypherLayout.visibility = View.GONE
                    binding.plainLayout.visibility = View.GONE
                    binding.biometricReg.visibility = View.VISIBLE
                    binding.biometricAuth.visibility = View.GONE
                    viewModel.checkBiometricSupport(requireContext(), true)
                }
                is NeedEncryption.NeedEncryptNotSupport -> {
                    binding.checkNeedEncrypt.isChecked = true
                    binding.checkNeedStrongAuth.isChecked = true
                    binding.checkAllowDeviceCredential.isChecked = false
                    binding.inputLayout.visibility = View.GONE
                    binding.inputText.setText("")
                    binding.cypherLayout.visibility = View.GONE
                    binding.plainLayout.visibility = View.GONE
                    binding.biometricReg.visibility = View.GONE
                    binding.biometricAuth.visibility = View.GONE
                    Toast.makeText(requireContext(), state.warning, Toast.LENGTH_SHORT).show()
                }
                is NoEncryptionInit -> {
                    binding.checkNeedEncrypt.isChecked = false
                    binding.checkNeedStrongAuth.isChecked = state.needStrongAuth
                    binding.checkAllowDeviceCredential.isChecked = state.allowDeviceCredential
                    binding.inputLayout.visibility = View.GONE
                    binding.inputText.setText("")
                    binding.cypherLayout.visibility = View.GONE
                    binding.plainLayout.visibility = View.GONE
                    binding.biometricReg.visibility = View.GONE
                    binding.biometricAuth.visibility = View.VISIBLE
                    viewModel.checkBiometricSupport(requireContext(), state.needStrongAuth)
                }
                is NotSupportStrong -> {
                    binding.checkNeedEncrypt.isChecked = false
                    binding.checkNeedStrongAuth.isChecked = true
                    binding.checkAllowDeviceCredential.isChecked = state.allowDeviceCredential
                    binding.inputLayout.visibility = View.GONE
                    binding.inputText.setText("")
                    binding.cypherLayout.visibility = View.GONE
                    binding.plainLayout.visibility = View.GONE
                    binding.biometricReg.visibility = View.GONE
                    binding.biometricAuth.visibility = View.GONE
                    Toast.makeText(requireContext(), state.warning, Toast.LENGTH_LONG).show()
                }
                is NotSupportWeak -> {
                    binding.checkNeedEncrypt.isChecked = false
                    binding.checkNeedStrongAuth.isChecked = false
                    binding.checkAllowDeviceCredential.isChecked = state.allowDeviceCredential
                    binding.inputLayout.visibility = View.GONE
                    binding.inputText.setText("")
                    binding.cypherLayout.visibility = View.GONE
                    binding.plainLayout.visibility = View.GONE
                    binding.biometricReg.visibility = View.GONE
                    binding.biometricAuth.visibility = View.GONE
                    Toast.makeText(requireContext(), state.warning, Toast.LENGTH_LONG).show()
                }
                is NeedEncryption.NeedEncryptStart -> {
                    binding.checkNeedEncrypt.isChecked = true
                    binding.checkNeedStrongAuth.isChecked = true
                    binding.checkAllowDeviceCredential.isChecked = false
                    binding.inputLayout.visibility = View.VISIBLE
                    binding.inputText.setText(state.inputMsg)
                    binding.inputText.isEnabled = true
                    binding.cypherLayout.visibility = View.GONE
                    binding.plainLayout.visibility = View.GONE
                    binding.biometricReg.visibility = View.VISIBLE
                    binding.biometricAuth.visibility = View.GONE
                }
                is NeedEncryption.NeedEncryptShowEncryptPrompt -> {
                    binding.checkNeedEncrypt.isChecked = true
                    binding.checkNeedStrongAuth.isChecked = true
                    binding.checkAllowDeviceCredential.isChecked = false
                    binding.inputLayout.hint = "Enter message to encrypt"
                    binding.inputText.setText(state.inputMsg)
                    binding.inputText.isEnabled = false
                    binding.inputLayout.visibility = View.VISIBLE
                    binding.cypherLayout.visibility = View.GONE
                    binding.cypherText.setText("")
                    binding.plainLayout.visibility = View.GONE
                    binding.biometricReg.visibility = View.VISIBLE
                    binding.biometricAuth.visibility = View.GONE

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        showBiometricRegPrompt()
                    }
                }
                is NeedEncryption.NeedEncryptEncryptError -> {
                    binding.checkNeedEncrypt.isChecked = true
                    binding.checkNeedStrongAuth.isChecked = true
                    binding.checkAllowDeviceCredential.isChecked = false
                    binding.inputLayout.visibility = View.VISIBLE
                    binding.inputLayout.hint = "Enter message to encrypt"
                    binding.inputText.setText(state.inputMsg)
                    binding.inputText.isEnabled = true
                    binding.cypherLayout.visibility = View.GONE
                    binding.cypherText.setText("")
                    binding.plainLayout.visibility = View.GONE
                    binding.biometricReg.visibility = View.VISIBLE
                    binding.biometricAuth.visibility = View.GONE

                    Toast.makeText(requireContext(), state.errString, Toast.LENGTH_SHORT).show()
                }
                is NeedEncryption.NeedEncryptionEncrypted -> {
                    if (state.encryptedMsg == null) {
                        viewModel.setNeedEncrypt(
                            binding.checkNeedEncrypt.isChecked,
                            binding.checkNeedStrongAuth.isChecked,
                            binding.checkAllowDeviceCredential.isChecked
                        )
                        return@observe
                    }

                    binding.checkNeedEncrypt.isChecked = true
                    binding.checkNeedStrongAuth.isChecked = true
                    binding.checkAllowDeviceCredential.isChecked = false
                    binding.inputLayout.visibility = View.VISIBLE
                    binding.inputText.setText(state.inputMsg)

                    binding.inputLayout.hint = "Message for validation"
                    binding.inputText.isEnabled = false
                    binding.biometricAuth.visibility = View.VISIBLE
                    binding.biometricReg.visibility = View.GONE
                    binding.plainLayout.visibility = View.GONE
                    binding.cypherLayout.visibility = View.VISIBLE
                    binding.cypherText.setText(
                        Base64.encodeToString(
                            state.encryptedMsg.ciphertext,
                            Base64.DEFAULT
                        )
                    )

                    Toast.makeText(requireContext(), "Biometric reg success", Toast.LENGTH_SHORT)
                        .show()
                }
                is NeedEncryption.NeedEncryptShowDecryptPrompt -> {
                    if (state.encryptedMsg == null) {
                        viewModel.setNeedEncrypt(
                            binding.checkNeedEncrypt.isChecked,
                            binding.checkNeedStrongAuth.isChecked,
                            binding.checkAllowDeviceCredential.isChecked
                        )
                        return@observe
                    }

                    binding.checkNeedEncrypt.isChecked = true
                    binding.checkNeedStrongAuth.isChecked = true
                    binding.checkAllowDeviceCredential.isChecked = false
                    binding.inputLayout.visibility = View.VISIBLE
                    binding.inputText.setText(state.inputMsg)

                    binding.inputLayout.hint = "Message for validation"
                    binding.inputText.isEnabled = false
                    binding.biometricAuth.visibility = View.VISIBLE
                    binding.biometricReg.visibility = View.GONE
                    binding.plainLayout.visibility = View.GONE
                    binding.cypherLayout.visibility = View.VISIBLE
                    binding.cypherText.setText(
                        Base64.encodeToString(
                            state.encryptedMsg.ciphertext,
                            Base64.DEFAULT
                        )
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        showBiometricAuthPrompt(state.encryptedMsg)
                    }
                }
                is NeedEncryption.NeedEncryptDecryptError -> {
                    binding.checkNeedEncrypt.isChecked = true
                    binding.checkNeedStrongAuth.isChecked = true
                    binding.checkAllowDeviceCredential.isChecked = false
                    binding.inputLayout.visibility = View.VISIBLE
                    binding.inputText.setText(state.inputMsg)

                    binding.inputLayout.hint = "Message for validation"
                    binding.inputText.isEnabled = false
                    binding.biometricAuth.visibility = View.VISIBLE
                    binding.biometricReg.visibility = View.GONE
                    binding.plainLayout.visibility = View.GONE
                    binding.cypherLayout.visibility = View.VISIBLE
                    binding.cypherText.setText(
                        Base64.encodeToString(
                            viewModel.encryptMsg!!.ciphertext,
                            Base64.DEFAULT
                        )
                    )

                    Toast.makeText(requireContext(), state.errString, Toast.LENGTH_SHORT).show()
                }
                is NeedEncryption.NeedEncryptionDecrypted -> {
                    binding.checkNeedEncrypt.isChecked = true
                    binding.checkNeedStrongAuth.isChecked = true
                    binding.checkAllowDeviceCredential.isChecked = false
                    binding.inputLayout.visibility = View.VISIBLE
                    binding.inputText.setText(state.inputMsg)

                    binding.inputLayout.hint = "Message for validation"
                    binding.inputText.isEnabled = false
                    binding.biometricAuth.visibility = View.VISIBLE
                    binding.biometricReg.visibility = View.GONE
                    binding.plainText.setText(state.decryptedMsg)
                    binding.plainLayout.visibility = View.VISIBLE
                    binding.cypherLayout.visibility = View.VISIBLE
                    binding.cypherText.setText(
                        Base64.encodeToString(
                            viewModel.encryptMsg!!.ciphertext,
                            Base64.DEFAULT
                        )
                    )

                    Toast.makeText(requireContext(), "Biometric auth success", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }

        binding.checkNeedEncrypt.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNeedEncrypt(
                isChecked,
                binding.checkNeedStrongAuth.isChecked,
                binding.checkAllowDeviceCredential.isChecked
            )
        }
        binding.checkNeedStrongAuth.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNeedEncrypt(
                binding.checkNeedEncrypt.isChecked && isChecked,
                isChecked,
                binding.checkAllowDeviceCredential.isChecked
            )
        }
        binding.checkAllowDeviceCredential.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNeedEncrypt(
                binding.checkNeedEncrypt.isChecked && !isChecked,
                binding.checkNeedStrongAuth.isChecked,
                isChecked
            )
        }

        binding.biometricReg.setOnClickListener {
            viewModel.startBiometricReg(binding.inputText.text.toString())
        }

        binding.biometricAuth.setOnClickListener {
            if (supportEncrypt()) {
                viewModel.onClickDecryptMessage(binding.inputText.text.toString())
            } else {
                onClickBiometrics()
            }
        }
    }

    private fun supportEncrypt(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && binding.checkNeedEncrypt.isChecked
    }

    private fun onClickBiometrics() {
        BiometricUtil.showBiometricPrompt(
            activity = requireActivity(),
            cryptoObject = null,
            allowDeviceCredential = binding.checkAllowDeviceCredential.isChecked,
            useStrongAuth = binding.checkNeedStrongAuth.isChecked,
            listener = object : BiometricAuthListener {
                override fun onBiometricAuthenticationError(errorCode: Int, errString: String) {
                    Toast.makeText(
                        requireContext(),
                        "Biometric Authentication Error: \n[$errorCode] $errString.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onBiometricAuthenticationSuccess(result: BiometricPrompt.AuthenticationResult) {
                    Toast.makeText(
                        requireContext(),
                        "Biometric Authentication Success",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showBiometricRegPrompt() {

        val cryptoObject = BiometricPrompt.CryptoObject(
            CryptographyUtil.getInitializedCipherForEncryption()
        )
        val message = binding.inputText.text.toString().trim()
        BiometricUtil.showBiometricPrompt(
            title = "Biometric Register",
            subtitle = "To use biometric auth, you need to reg first.",
            activity = requireActivity(),
            cryptoObject = cryptoObject,
            allowDeviceCredential = binding.checkAllowDeviceCredential.isChecked,
            useStrongAuth = binding.checkNeedStrongAuth.isChecked,
            listener = object : BiometricAuthListener {
                override fun onBiometricAuthenticationError(errorCode: Int, errString: String) {
                    viewModel.onBioRegAuthError(message, errorCode, errString)
                }

                override fun onBiometricAuthenticationSuccess(result: BiometricPrompt.AuthenticationResult) {
                    viewModel.onBioRegSuccess(input = message, result = result)
                }
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showBiometricAuthPrompt(encryptedMsg: EncryptedMessage?) {
        encryptedMsg?.iv?.let { it ->
            val cryptoObject = BiometricPrompt.CryptoObject(
                CryptographyUtil.getInitializedCipherForDecryption(it)
            )

            val message = binding.inputText.text.toString().trim()
            BiometricUtil.showBiometricPrompt(
                activity = requireActivity(),
                cryptoObject = cryptoObject,
                allowDeviceCredential = binding.checkAllowDeviceCredential.isChecked,
                useStrongAuth = binding.checkNeedStrongAuth.isChecked,
                listener = object : BiometricAuthListener {
                    override fun onBiometricAuthenticationError(errorCode: Int, errString: String) {
                        viewModel.onBioAuthError(message, errorCode, errString)
                    }

                    override fun onBiometricAuthenticationSuccess(result: BiometricPrompt.AuthenticationResult) {
                        viewModel.onBioAuthSuccess(input = message, result = result)
                    }
                }
            )
        }
    }
}