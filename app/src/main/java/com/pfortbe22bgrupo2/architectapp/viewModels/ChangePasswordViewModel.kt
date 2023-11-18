package com.pfortbe22bgrupo2.architectapp.viewModels

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.listeners.AuthResultListener

class ChangePasswordViewModel : ViewModel() {


    private val auth = Firebase.auth
    private val currentUser = auth.currentUser

    fun changePassword(oldPassword: String, newPassword: String,authResultListener: AuthResultListener) {
        var email = currentUser?.email
        if (email == null) email = ""
        val credential = EmailAuthProvider.getCredential(email, oldPassword)
        currentUser!!.reauthenticate(credential)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    currentUser.updatePassword(newPassword).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(ContentValues.TAG, "User password updated.")
                            auth.signOut()
                            authResultListener.onAuthSuccess()
                        }
                    }
                }else{
                    authResultListener.onAuthFailure("Contraseña Actual Incorrecta")
                }
            }
    }
}