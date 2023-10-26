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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentForgotPasswordBinding
import com.pfortbe22bgrupo2.architectapp.viewModels.ForgotPasswordViewModel

class ForgotPasswordFragment : Fragment() {

    companion object {
        fun newInstance() = ForgotPasswordFragment()
    }

    private lateinit var viewModel: ForgotPasswordViewModel
    lateinit var binding: FragmentForgotPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForgotPasswordBinding.inflate(inflater,container,false)
        auth = Firebase.auth
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.changePasswordButton.setOnClickListener {
            val verificationEmail = binding.forgotPasswordEmailEditText.text
            if (!verificationEmail.isNullOrEmpty()){
                sendChangePasswordEmail()
            } else{
                binding.forgotPasswordEmailEditText.error = "Campo Obligatorio"
            }
        }
    }

    private fun goToLoginFragment() {
        findNavController().navigateUp()
    }

    private fun sendChangePasswordEmail() {
        val emailAddress = binding.forgotPasswordEmailEditText.text.toString()
        auth.sendPasswordResetEmail(emailAddress)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email sent.")
                    Toast.makeText(requireActivity(), "Email enviado, verifica tu casilla", Toast.LENGTH_LONG).show()
                    goToLoginFragment()
                }
            }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ForgotPasswordViewModel::class.java)
        // TODO: Use the ViewModel
    }

}