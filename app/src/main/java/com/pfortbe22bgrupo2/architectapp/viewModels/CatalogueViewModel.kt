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
    private var furnitureList = mutableListOf<FurnitureModelData>()
    private var filteredFurnitureList = mutableListOf<FurnitureModelData>()
    private val db = Firebase.firestore

    fun getFurnitureList() {
        furnitureList.clear()
        getFurnitureOfType("beds")
        getFurnitureOfType("chairs")
        getFurnitureOfType("sofas")
        getFurnitureOfType("tables")
    }

    private fun getFurnitureOfType(furnitureType: String) {
        db.collection("models")
            .document(furnitureType)
            .collection("datos")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val furniture = parseFurniture(document, furnitureType)
                    furnitureList.add(furniture)
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
                furnitureOptions.value = furnitureList
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    private fun parseFurniture(document: DocumentSnapshot, furnitureType: String): FurnitureModelData {
        val imageUrl = document.getString("image_url")?: ""
        val allowWalls = document.getBoolean("allow_walls")?: false
        val dimensionX = document.getLong("dimension_x")?.toInt() ?: 0
        val dimensionY = document.getLong("dimension_y")?.toInt() ?: 0
        val dimensionZ = document.getLong("dimension_z")?.toInt() ?: 0
        val link = document.getString("link")?: ""
        val scale = document.getDouble("scale")?: 0.0
        val name = document.getString("name")?: ""
        val description = document.getString("description")?: ""
        val category = document.getString("category")?: ""

        return FurnitureModelData(furnitureType, document.id, imageUrl, allowWalls, dimensionX, dimensionY, dimensionZ, link, scale.toFloat(), name, description, category)
    }

    fun filterFurnitureByCategory(category: String) {
        filteredFurnitureList.clear()
        filteredFurnitureList = furnitureList.filter { item -> item.category.lowercase() == category.lowercase() } as MutableList<FurnitureModelData>
        furnitureOptions.value = filteredFurnitureList
    }


}