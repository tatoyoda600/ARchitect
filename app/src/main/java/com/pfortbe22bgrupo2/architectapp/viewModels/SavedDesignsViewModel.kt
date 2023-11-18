package com.pfortbe22bgrupo2.architectapp.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.models.SavedDesign

class SavedDesignsViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val db = Firebase.firestore
    val savedDesignOptions = MutableLiveData<FirestoreRecyclerOptions<SavedDesign>>()

    fun deleteSavedDesign(savedDesignId: String) {
        db.collection("saved_designs").document(savedDesignId).delete()
    }

    fun getSavedDesignList() {
        val currentUser = auth.currentUser!!
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("saved_designs").whereEqualTo("userId", currentUser.uid)
            val options = FirestoreRecyclerOptions.Builder<SavedDesign>()
                .setQuery(query, SavedDesign::class.java)
                .build()
            savedDesignOptions.value = options
        }
    }

