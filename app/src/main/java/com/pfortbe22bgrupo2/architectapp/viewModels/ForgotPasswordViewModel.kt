package com.pfortbe22bgrupo2.architectapp.viewModels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.listeners.AuthResultListener

class ForgotPasswordViewModel : ViewModel() {

    private val auth = Firebase.auth
    fun sendPasswordResetEmail(emailAddress: String, authResultListener: AuthResultListener) {
        auth.sendPasswordResetEmail(emailAddress)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    authResultListener.onAuthSuccess()
                }else{
                    authResultListener.onAuthFailure("Error al envia email para restablecer contraseña")
                }
            }
    }

}