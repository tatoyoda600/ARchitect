package com.pfortbe22bgrupo2.architectapp.viewModels

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.entities.FurnitureModelData


class CatalogueViewModel: ViewModel() {

    private val _furnitureOptions = MutableLiveData<MutableList<FurnitureModelData>>()
    val furnitureOptions : LiveData<MutableList<FurnitureModelData>> get() = _furnitureOptions
    private var furnitureList = mutableListOf<FurnitureModelData>()
    private var filteredFurnitureList = mutableListOf<FurnitureModelData>()
    private val db = Firebase.firestore
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    init {
        loadFurnitureList()
    }

    fun loadFurnitureList() {
        _isLoading.value = true
        getFurnitureList()
    }

    private fun getFurnitureList() {
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
                _furnitureOptions.value = furnitureList
                _isLoading.value = false
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
                _isLoading.value = false
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
        val tag = document.getString("tag")?: ""

        return FurnitureModelData(furnitureType, document.id, imageUrl, allowWalls, dimensionX, dimensionY, dimensionZ, link, scale.toFloat(), name, description, tag)
    }

    fun filterFurnitureByTag(category: String) {
        filteredFurnitureList.clear()
        filteredFurnitureList = furnitureList.filter { item -> item.tag.lowercase() == category.lowercase() } as MutableList<FurnitureModelData>
        _furnitureOptions.value = filteredFurnitureList
    }

    fun removeElem(furniture: FurnitureModelData) {
        filteredFurnitureList.clear()
        db.collection("models").document(furniture.furnitureType).collection("datos").document(furniture.id)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
        filteredFurnitureList = furnitureList.filter { item -> item.name.lowercase() != furniture.name.lowercase() } as MutableList<FurnitureModelData>
        _furnitureOptions.value = filteredFurnitureList
    }

}