package com.pfortbe22bgrupo2.architectapp.fragments

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentSignUpBinding
import com.pfortbe22bgrupo2.architectapp.listeners.AuthResultListener
import com.pfortbe22bgrupo2.architectapp.viewModels.SignUpViewModel

class SignUpFragment: Fragment() {

    companion object {
        fun newInstance() = SignUpFragment()
    }

    private lateinit var signUpViewModel: SignUpViewModel
    private lateinit var binding: FragmentSignUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater,container,false)
        signUpViewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

       binding.secondSignUpButton.setOnClickListener() {
           validateFields()
        }

        binding.resgisterPasswordTextInputLayout.setOnClickListener{
            binding.resgisterPasswordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

    }

    private fun validateFields() {
        val email = binding.registerEmailEditText.text.toString()
        val userName = binding.registerUserNameEditText.text.toString()
        val password = binding.resgisterPasswordEditText.text.toString()
        val repeatPassword = binding.resgisterRepeatPasswordEditText.text.toString()
        if (!email.isEmpty() || !password.isEmpty() || !userName.isEmpty()) {
            if (userName.length >= 6) {
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (password.length >= 6){
                        if(password == repeatPassword){
                            registerUser()
                        }else{
                            binding.resgisterPasswordEditText.error = "Contraseñas Diferentes"
                            binding.resgisterRepeatPasswordEditText.error = "Contraseñas Diferentes"
                        }
                    }else{
                        binding.resgisterPasswordEditText.error = "6 Caracteres como Minimo"
                    }
                }else{
                    binding.registerEmailEditText.error = "Correo electrónico inválido"
                }
            }else{
                binding.registerUserNameEditText.error = "6 Caracteres como Minimo"
            }
        }else{
            binding.registerUserNameEditText.error = "Campo Obligatorio"
            binding.registerEmailEditText.error = "Campo Obligatorio"
            binding.resgisterPasswordEditText.error = "Campo Obligatorio"
            binding.resgisterRepeatPasswordEditText.error = "Campo Obligatorio"
        }
    }

    private fun registerUser() {
        val email = binding.registerEmailEditText.text.toString()
        val password = binding.resgisterPasswordEditText.text.toString()
        val userName = binding.registerUserNameEditText.text.toString()
        signUpViewModel.registerUser(email,password,userName,object : AuthResultListener{
            override fun onAuthSuccess() {
                goToLoginFragment()
            }
            override fun onAuthFailure(errorMessage: String) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage(errorMessage)
                    .setTitle("Error de registro")
                    .setPositiveButton("OK", null)
                val dialog = builder.create()
                dialog.show()
            }

        })
    }

    private fun goToLoginFragment() {
        val action = SignUpFragmentDirections.actionSignUpFragmentToCatalogoActivity()
        findNavController().navigate(action)
    }


}