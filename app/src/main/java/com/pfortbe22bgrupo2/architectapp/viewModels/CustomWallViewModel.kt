package com.pfortbe22bgrupo2.architectapp.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.models.CustomWall


class CustomWallViewModel : ViewModel() {


    private val auth = Firebase.auth
    private val db = Firebase.firestore
    val customWallOptions = MutableLiveData<FirestoreRecyclerOptions<CustomWall>>()


    fun deleteSavedDesign(savedDesignId: String) {
        db.collection("custom_walls").document(savedDesignId).delete()
    }

    fun getCustomWallList() {
        val currentUser = auth.currentUser!!
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("custom_walls").whereEqualTo("userId", currentUser.uid)
        val options = FirestoreRecyclerOptions.Builder<CustomWall>()
            .setQuery(query, CustomWall::class.java)
            .build()
        customWallOptions.value = options
    }

}