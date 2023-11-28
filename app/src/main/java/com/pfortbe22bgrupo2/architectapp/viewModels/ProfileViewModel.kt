package com.pfortbe22bgrupo2.architectapp.viewModels

import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.entities.UserProfileData
import kotlinx.coroutines.launch


class ProfileViewModel: ViewModel() {


    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private var currentUser = auth.currentUser!!

    val currentUserData = MutableLiveData<UserProfileData?>()

    fun fetchUserData() {
        val docRef = db.collection("users").document(currentUser.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userName = document.data?.get("userName") as? String
                    val email = document.data?.get("email") as? String
                    val address = document.data?.get("address") as? String
                    val phone = document.data?.get("phoneNumber") as? String
                    val uri = document.data?.get("photoURL") as String
                    val isAdmin = document.data?.get("isAdmin") as? Boolean

                    val data = UserProfileData(userName,email,address,phone,uri,isAdmin)
                    currentUserData.postValue(data)
                    Log.d(ContentValues.TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    currentUserData.postValue(null)
                    Log.d(ContentValues.TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }
    }

    fun deleteUser() {
        viewModelScope.launch(){
            deleteUserFromAuth()
            deleteUserFromFirestore()
        }
    }

    private fun deleteUserFromAuth() {
        currentUser.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "User account deleted.")
                }
            }
    }

    private fun deleteUserFromFirestore() {
        db.collection("users").document(currentUser.uid)
            .delete()
            .addOnSuccessListener { Log.d(ContentValues.TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error deleting document", e) }
    }

    fun signOut() {
        auth.signOut()
    }


}

