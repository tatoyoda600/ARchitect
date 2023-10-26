package com.pfortbe22bgrupo2.architectapp.fragments

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.util.Patterns
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
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentLoginBinding
import com.pfortbe22bgrupo2.architectapp.viewModels.LoginViewModel

class LoginFragment: Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container,false)
        auth = Firebase.auth
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.secondLoginButton.setOnClickListener() {
            validateFields()
        }
        binding.loginPasswordTextInputLayout.setOnClickListener{
            binding.loginPasswordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        binding.forgotPasswordTextView.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment()
            findNavController().navigate(action)
        }

    }

    private fun validateFields() {
        val email = binding.logingEmailEditText.text.toString()
        val password = binding.loginPasswordEditText.text.toString()
        if (!email.isEmpty() || !password.isEmpty()) {
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (password.length >= 6) {
                    loginUser()
                }else{
                    binding.loginPasswordEditText.error = "6 Caracteres como Mimino"
                }
            }else{
                binding.logingEmailEditText.error = "Correo electrónico inválido"
            }
        }else{
            binding.logingEmailEditText.error = "Campo Obligatorio"
            binding.loginPasswordEditText.error = "Campo Obligatorio"
        }
    }

    private fun loginUser() {
        val email = binding.logingEmailEditText.text.toString()
        val password = binding.loginPasswordEditText.text.toString()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithEmail:success")
                    //val user = auth.currentUser
                    goToCatalogoActivity()
                } else {
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(requireActivity(), "Email o Contraseñas incorrectas", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun goToCatalogoActivity() {
        val action = LoginFragmentDirections.actionLoginFragmentToCatalogoActivity()
        findNavController().navigate(action)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        // TODO: Use the ViewModel
    }
}