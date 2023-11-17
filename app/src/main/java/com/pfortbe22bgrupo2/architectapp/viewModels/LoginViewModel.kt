package com.pfortbe22bgrupo2.architectapp.viewModels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.listeners.AuthResultListener

class LoginViewModel: ViewModel() {

    private val auth = Firebase.auth

    fun loginUser(email: String, password: String, authResultListener: AuthResultListener) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    authResultListener.onAuthSuccess()
                } else {
                    authResultListener.onAuthFailure("Email o Contraseña incorrectos")
                }
            }
    }
}

