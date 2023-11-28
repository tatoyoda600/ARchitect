package com.pfortbe22bgrupo2.architectapp.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentForgotPasswordBinding
import com.pfortbe22bgrupo2.architectapp.listeners.AuthResultListener
import com.pfortbe22bgrupo2.architectapp.viewModels.ForgotPasswordViewModel

class ForgotPasswordFragment : Fragment() {

    companion object {
        fun newInstance() = ForgotPasswordFragment()
    }

    private lateinit var forgotPasswordViewModel: ForgotPasswordViewModel
    lateinit var binding: FragmentForgotPasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgotPasswordBinding.inflate(inflater,container,false)
        forgotPasswordViewModel = ViewModelProvider(this).get(ForgotPasswordViewModel::class.java)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.sendEmailResetPasswordButton.setOnClickListener {
            val verificationEmail = binding.resetPasswordEmailEditText.text
            if (!verificationEmail.isNullOrEmpty()){
                sendChangePasswordEmail()
            } else{
                binding.resetPasswordEmailEditText.error = "Campo Obligatorio"
            }
        }
    }

    private fun goToLoginFragment() {
        findNavController().navigateUp()
    }

    private fun sendChangePasswordEmail() {
        val emailAddress = binding.resetPasswordEmailEditText.text.toString()
        forgotPasswordViewModel.sendPasswordResetEmail(emailAddress, object : AuthResultListener{
            override fun onAuthSuccess() {
                Toast.makeText(requireActivity(), "Email enviado, verifica tu casilla", Toast.LENGTH_LONG).show()
                goToLoginFragment()
            }
            override fun onAuthFailure(errorMessage: String) {
                Log.d(TAG, errorMessage)
            }
        })
    }

}