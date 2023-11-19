package com.pfortbe22bgrupo2.architectapp.viewModels

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.entities.FurnitureModelData


class CatalogueViewModel: ViewModel() {

    val furnitureOptions = MutableLiveData<MutableList<FurnitureModelData>>()
    private val furnitureList = mutableListOf<FurnitureModelData>()
    private val db = Firebase.firestore

    fun getFurnitureList() {
        getBeds()
        getChairs()
        getSofas()
        getTables()
    }

    private fun getTables() {
        db.collection("models").document("tables").collection("datos")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val furniture = parseFurniture(document)
                    furnitureList.add(furniture)
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
                furnitureOptions.value = furnitureList
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    private fun getSofas() {
        db.collection("models").document("sofas").collection("datos")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val furniture = parseFurniture(document)
                    furnitureList.add(furniture)
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    private fun getChairs() {
        db.collection("models").document("chairs").collection("datos")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val furniture = parseFurniture(document)
                    furnitureList.add(furniture)
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    private fun getBeds() {
        db.collection("models").document("beds").collection("datos")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val furniture = parseFurniture(document)
                    furnitureList.add(furniture)
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    private fun parseFurniture(document: DocumentSnapshot): FurnitureModelData {
        val imageUrl = document.getString("image_url")?: ""
        val allowWalls = document.getBoolean("allow_walls")?: false
        val dimensionX = document.getLong("dimension_x")?.toInt() ?: 0
        val dimensionY = document.getLong("dimension_y")?.toInt() ?: 0
        val dimensionZ = document.getLong("dimension_z")?.toInt() ?: 0
        val link = document.getString("link")?: ""
        val scala = document.getLong("scala")?.toInt() ?: 0
        val name = document.getString("name")?: ""
        val description = document.getString("description")?: ""
        return FurnitureModelData(imageUrl, allowWalls, dimensionX, dimensionY, dimensionZ,link,scala,name,description)
    }

}