package com.pfortbe22bgrupo2.architectapp.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.pfortbe22bgrupo2.architectapp.activities.MainActivity
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentChangePasswordBinding
import com.pfortbe22bgrupo2.architectapp.listeners.AuthResultListener
import com.pfortbe22bgrupo2.architectapp.viewModels.ChangePasswordViewModel

class ChangePasswordFragment : Fragment() {

    companion object {
        fun newInstance() = ChangePasswordFragment()
    }

    private lateinit var changePasswordViewModel: ChangePasswordViewModel
    private lateinit var binding: FragmentChangePasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangePasswordBinding.inflate(inflater,container,false)
        //auth = Firebase.auth
        //currentUser = auth.currentUser!!
        changePasswordViewModel = ViewModelProvider(this).get(ChangePasswordViewModel::class.java)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.editPasswordButton.setOnClickListener(){
            changePassword()
        }

        binding.editForgotPasswordTextView.setOnClickListener(){
            navToForgotPassword()
        }
    }

    private fun changePassword() {
        val oldPassword = binding.editOldPasswordEditText.text.toString()
        val newPassword = binding.editPasswordEditText.text.toString()
        val repeatNewPassword = binding.editRepeatPasswordEditText.text.toString()
        val isValidate: Boolean = validateFields(oldPassword,newPassword, repeatNewPassword)
        if (isValidate){
            changePasswordViewModel.changePassword(oldPassword,newPassword,object : AuthResultListener{
                override fun onAuthSuccess() {
                    navToMainActivity()
                }
                override fun onAuthFailure(errorMessage: String) {
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun validateFields(oldPassword:String, newPassword: String, repeatNewPassword: String): Boolean {
        val newPasswordValid = newPassword.length >= 6
        val repeatNewPasswordValid = repeatNewPassword.length >= 6
        val oldPasswordPasswordValid = oldPassword.length >=6
        if (oldPassword.isEmpty()) binding.editOldPasswordEditText.error = "Debes ingresar tu contraseña actual"
        if (newPassword.isEmpty()) binding.editPasswordEditText.error = "Debes ingresar tu nueva contraseña"
        if (repeatNewPassword.isEmpty()) binding.editRepeatPasswordEditText.error = "Debes ingresar tu nueva contraseña"
        if (!newPasswordValid) binding.editPasswordEditText.error = "Contraseña muy corta"
        if (!repeatNewPasswordValid) binding.editRepeatPasswordEditText.error = "Contraseña muy corta"
        var isValidate = false
        if (newPassword != repeatNewPassword){
            binding.editPasswordEditText.error = "Las contraseñas deben ser iguales"
            binding.editRepeatPasswordEditText.error = "Las contraseñas deben ser iguales"
        }else{
            if (newPasswordValid && repeatNewPasswordValid && oldPasswordPasswordValid){
                isValidate = true
            }
        }
        return isValidate
    }


    private fun navToForgotPassword() {
        val action = ChangePasswordFragmentDirections.actionChangePasswordFragmentToForgotPasswordFragment2()
        findNavController().navigate(action)
    }


    private fun navToMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("fragmentToLoad", "LoginFragment")
        startActivity(intent)
    }

}