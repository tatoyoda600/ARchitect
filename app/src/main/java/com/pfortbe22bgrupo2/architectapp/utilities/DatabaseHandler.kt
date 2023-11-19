package com.pfortbe22bgrupo2.architectapp.utilities

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.database.AppDatabase
import com.pfortbe22bgrupo2.architectapp.database.FloorDao
import com.pfortbe22bgrupo2.architectapp.database.FloorPointsDao
import com.pfortbe22bgrupo2.architectapp.entities.FloorEntity
import com.pfortbe22bgrupo2.architectapp.entities.FloorPointsEntity
import com.pfortbe22bgrupo2.architectapp.entities.Product
import com.pfortbe22bgrupo2.architectapp.types.Floor
import io.github.sceneview.math.Position
import io.github.sceneview.node.Node

class DatabaseHandler(context: Context) {
    private val database: AppDatabase
    private val floorDao: FloorDao
    private val floorPointsDao: FloorPointsDao
    private val firestore: FirebaseFirestore

    init {
        // Log.d("FunctionNames", "init")
        database = AppDatabase.getAppDatabase(context)!!
        floorDao = database.floorDao()
        floorPointsDao = database.floorPointsDao()
        firestore = Firebase.firestore
    }

    fun getFloorIDs(): List<Int> {
        // Log.d("FunctionNames", "getFloorIDs")
        return floorDao.getFloorIDs()
    }

    fun getFloorByID(id: Int, coordZoom: Int): Pair<Floor, Float>? {
        // Log.d("FunctionNames", "getFloorByID")
        val floor = floorDao.getFloorByID(id)
        if (floor != null) {
            val grid = getFloorPointsByID(id, coordZoom)
            return Pair(Floor(grid), floor.rotation)
        }
        return null
    }

    fun insertFloor(floor: Floor, coordUnzoom: Float, cameraPosition: Position, rotation: Float): Boolean {
        // Log.d("FunctionNames", "insertFloor")
        try {
            // The camera's -X rotation is its yaw rotation
            val id = floorDao.insertFloor(FloorEntity(0, rotation))
            insertFloorPoints(id.toInt(), floor.grid, coordUnzoom, cameraPosition)
            return true
        }
        catch (error: Exception) {
            return false
        }
    }

    private fun getFloorPointsByID(id: Int, coordZoom: Int): MutableMap<Int, MutableMap<Int, Floor.CellState>> {
        // Log.d("FunctionNames", "getFloorPointsByID")
        val output: MutableMap<Int, MutableMap<Int, Floor.CellState>> = mutableMapOf()
        val entities = floorPointsDao.getFloorPointsByID(id)

        for (points in entities) {
            output.getOrPut((points.x_pos * coordZoom).toInt()) { -> mutableMapOf() }
                .putIfAbsent((points.z_pos * coordZoom).toInt(), Floor.CellState.FILLED)
        }

        return output
    }

    private fun getFloorPointCountByID(id: Int): Int {
        // Log.d("FunctionNames", "getFloorPointCountByID")
        return floorPointsDao.getFloorPointCountByID(id)?: 0
    }

    private fun insertFloorPoints(
        floorId: Int,
        grid: MutableMap<Int, MutableMap<Int, Floor.CellState>>,
        coordUnzoom: Float,
        cameraPosition: Position
    ) {
        // Log.d("FunctionNames", "insertFloorPoints")
        var count = getFloorPointCountByID(floorId)
        for (xKey in grid.keys) {
            for (zKey in grid.get(xKey)?.keys?: mutableSetOf()) {
                // Insert the point with the real (non-grid) coordinates, relative to the camera position
                val entity = FloorPointsEntity(
                    floorId,
                    count + 1,
                    xKey * coordUnzoom - cameraPosition.x,
                    zKey * coordUnzoom - cameraPosition.z
                )
                floorPointsDao.insertFloorPoint(entity)
                count++
            }
        }
    }

    fun getProductData(
        modelCategory: String,
       modelName: String,
       onSuccess: (Product) -> Unit,
       onFailure: () -> Unit
    ) {
        firestore.collection("models")
            .document(modelCategory)
            .collection("datos")
            .document(modelName)
            .get()
            .addOnSuccessListener { document: DocumentSnapshot ->
                Log.e("FIRESTORE", "SUCCESS")
                val data = document.data
                if (data != null) {
                    Log.e("FIRESTORE", "HAS DATA")
                    val scale = data.get("scale")
                    val allowWalls = data.get("allow_walls")
                    val imageURL = data.get("image_url")

                    if (
                        scale is Number
                        && allowWalls is Boolean
                        && imageURL is String
                    ) {
                        onSuccess(Product(
                            modelName,
                            "",
                            0.0,
                            imageURL,
                            modelCategory,
                            0,
                            "",
                            scale.toFloat(),
                            allowWalls
                        ))
                    }
                }
            }
            .addOnFailureListener {err ->
                Log.e("FIRESTORE", err.toString())
                err.message?.let { Log.e("FIRESTORE", it) }
                onFailure()
            }
    }
}