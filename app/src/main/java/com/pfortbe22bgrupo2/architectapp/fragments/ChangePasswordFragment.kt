package com.pfortbe22bgrupo2.architectapp.fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.activities.MainActivity
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentChangePasswordBinding
import com.pfortbe22bgrupo2.architectapp.viewModels.ChangePasswordViewModel

class ChangePasswordFragment : Fragment() {

    companion object {
        fun newInstance() = ChangePasswordFragment()
    }

    private lateinit var viewModel: ChangePasswordViewModel
    private lateinit var binding: FragmentChangePasswordBinding
    private lateinit var auth: FirebaseAuth
    //private val db = Firebase.firestore
    private lateinit var currentUser: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChangePasswordBinding.inflate(inflater,container,false)
        auth = Firebase.auth
        currentUser = auth.currentUser!!
        return binding.root
    }

/*    if (reauthTask.isSuccessful) {
        // Reautenticación exitosa, ahora actualiza la contraseña
        currentUser.updatePassword(newPassword)
            .addOnCompleteListener { updatePasswordTask ->
                if (updatePasswordTask.isSuccessful) {
                    // Contraseña actualizada exitosamente
                    Log.d(TAG, "User password updated.")
                    auth.signOut()
                    navToMainActivity()
                } else {
                    // Error al actualizar la contraseña
                    Log.d(TAG, "Failed to update user password. ${updatePasswordTask.exception}")
                    // Manejar el error o proporcionar retroalimentación al usuario
                }
            }
    } else {
        // Error en la reautenticación
        Log.d(TAG, "Failed to reauthenticate user. ${reauthTask.exception}")
        Toast.makeText(requireContext(), "Contraseña Incorrecta", Toast.LENGTH_SHORT).show()
    }
}*/

    override fun onStart() {
        super.onStart()
        binding.editPasswordButton.setOnClickListener(){
            changePassword()
        }

        binding.editForgotPasswordTextView.setOnClickListener(){
            navToForgorPassword()
        }
    }

    private fun changePassword() {
        var email = currentUser.email
        if (email == null) email = ""
        val oldPassword = binding.editOldPasswordEditText.text.toString()
        val newPassword = binding.editPasswordEditText.text.toString()
        val repeatNewPassword = binding.editRepeatPasswordEditText.text.toString()
        val isValidate: Boolean = validateFields(oldPassword,newPassword, repeatNewPassword)
        if (isValidate){
            val credential = EmailAuthProvider.getCredential(email, oldPassword)
            currentUser.reauthenticate(credential)
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        currentUser.updatePassword(newPassword).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(TAG, "User password updated.")
                                auth.signOut()
                                navToMainActivity()
                            }
                        }
                    }else{
                        Toast.makeText(requireContext(), "Contraseña Actual Incorrecta", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }


    private fun validateFields(oldPassword:String, newPassword: String, repeatNewPassword: String): Boolean {
        val newPasswordValid = newPassword.length >= 6
        val repeatNewPasswordValid = repeatNewPassword.length >= 6
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
            if (newPasswordValid && repeatNewPasswordValid){
                isValidate = true
            }
        }
        return isValidate
    }


    private fun navToForgorPassword() {
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