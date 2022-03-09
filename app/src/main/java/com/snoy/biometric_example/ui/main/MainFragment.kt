package com.snoy.biometric_example.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.snoy.biometric_example.databinding.MainFragmentBinding
import com.snoy.biometric_example.utils.BiometricAuthListener
import com.snoy.biometric_example.utils.BiometricUtil

//ref= https://developer.android.com/training/sign-in/biometric-auth
class MainFragment : Fragment(), BiometricAuthListener {

    companion object {
        fun newInstance() = MainFragment()
    }

    private var _binding: MainFragmentBinding? = null

    private val binding get() = _binding!!

    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
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

        viewModel.needStrongAuth.observe(viewLifecycleOwner) {
            binding.checkNeedStrongAuth.isChecked = it
            checkBiometric()
        }
        binding.checkNeedStrongAuth.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNeedStrongAuth(isChecked)
        }
        viewModel.allowDeviceCredential.observe(viewLifecycleOwner) {
            binding.checkAllowDeviceCredential.isChecked = it
        }
        binding.checkAllowDeviceCredential.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setAllowDeviceCredential(isChecked)
        }

        binding.biometricAuth.setOnClickListener { onClickBiometrics() }
    }

    private fun checkBiometric() {
        val isBiometricReady: Boolean
        if (binding.checkNeedStrongAuth.isChecked) {
            isBiometricReady = BiometricUtil.isBiometricStrongReady(requireContext())
            Toast.makeText(
                requireContext(),
                "Biometric Strong is ${if (isBiometricReady) "" else "not "}ready.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            isBiometricReady = BiometricUtil.isBiometricReady(requireContext())
            Toast.makeText(
                requireContext(),
                "Biometric is ${if (isBiometricReady) "" else "not "}ready.", Toast.LENGTH_SHORT
            ).show()
        }

        binding.biometricAuth.visibility = if (isBiometricReady) View.VISIBLE else View.GONE
    }

    private fun onClickBiometrics() {
        BiometricUtil.showBiometricPrompt(
            negativeButton = requireContext().getString(android.R.string.cancel),
            activity = requireActivity(),
            listener = this,
            cryptoObject = null,
            allowDeviceCredential = binding.checkAllowDeviceCredential.isChecked,
            useStrongAuth = binding.checkNeedStrongAuth.isChecked
        )
    }

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