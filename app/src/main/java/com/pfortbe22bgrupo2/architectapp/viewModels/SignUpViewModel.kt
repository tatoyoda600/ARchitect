package com.pfortbe22bgrupo2.architectapp.viewModels

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.listeners.AuthResultListener

class SignUpViewModel: ViewModel() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore

    fun registerUser(email: String, password: String, userName: String, authResultListener: AuthResultListener) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userUid: String = user?.uid ?: ""
                    addUserToFirestore(email, userName, userUid)
                    authResultListener.onAuthSuccess()
                } else {
                    authResultListener.onAuthFailure("El correo electrónico ingresado ya está en uso por otro usuario")
                }
            }
    }

    private fun addUserToFirestore(email: String, userName: String, userUid: String) {
        val user = hashMapOf(
            "email" to email,
            "userName" to userName,
            "id" to userUid,
            "isAdmin" to false,
            "address" to null,
            "phoneNumber" to null,
            "profileImageUrl" to ""
        )
        db.collection("users").document(userUid).set(user)
    }
}


