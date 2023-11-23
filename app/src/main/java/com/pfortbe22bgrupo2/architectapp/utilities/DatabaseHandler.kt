package com.pfortbe22bgrupo2.architectapp.utilities

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.database.AppDatabase
import com.pfortbe22bgrupo2.architectapp.database.DesignDao
import com.pfortbe22bgrupo2.architectapp.database.DesignProductsDao
import com.pfortbe22bgrupo2.architectapp.database.FloorDao
import com.pfortbe22bgrupo2.architectapp.database.FloorPointsDao
import com.pfortbe22bgrupo2.architectapp.entities.DesignEntity
import com.pfortbe22bgrupo2.architectapp.entities.DesignProductsEntity
import com.pfortbe22bgrupo2.architectapp.entities.FloorEntity
import com.pfortbe22bgrupo2.architectapp.entities.FloorPointsEntity
import com.pfortbe22bgrupo2.architectapp.entities.Product
import com.pfortbe22bgrupo2.architectapp.types.DesignSession
import com.pfortbe22bgrupo2.architectapp.types.DesignSessionProduct
import com.pfortbe22bgrupo2.architectapp.types.Floor
import io.github.sceneview.math.Position
import io.github.sceneview.math.compareTo

class DatabaseHandler(context: Context) {
    private val database: AppDatabase
    private val floorDao: FloorDao
    private val floorPointsDao: FloorPointsDao
    private val designDao: DesignDao
    private val designProductsDao: DesignProductsDao
    private val firestore: FirebaseFirestore

    init {
        // Log.d("FunctionNames", "init")
        database = AppDatabase.getAppDatabase(context)!!
        floorDao = database.floorDao()
        floorPointsDao = database.floorPointsDao()
        designDao = database.designDao()
        designProductsDao = database.designProductsDao()
        firestore = Firebase.firestore
    }

    fun getFloorIDs(): List<Int> {
        // Log.d("FunctionNames", "getFloorIDs")
        return floorDao.getFloorIDs()
    }

    fun getFloorByID(id: Int): Pair<Floor, Float>? {
        // Log.d("FunctionNames", "getFloorByID")
        val floor = floorDao.getFloorByID(id)
        if (floor != null) {
            val grid = getFloorPointsByID(id)
            return Pair(Floor(grid), floor.rotation)
        }
        return null
    }

    fun insertFloor(floor: Floor, cameraPosition: Position, rotation: Float, name: String): Int {
        // Log.d("FunctionNames", "insertFloor")
        try {
            // The camera's -X rotation is its yaw rotation
            val id = floorDao.insertFloor(FloorEntity(0, rotation, name))
            insertFloorPoints(id.toInt(), floor.grid, cameraPosition)
            return id.toInt()
        }
        catch (error: Exception) {
            return -1
        }
    }

    private fun getFloorPointsByID(id: Int): MutableMap<Int, MutableMap<Int, Floor.CellState>> {
        // Log.d("FunctionNames", "getFloorPointsByID")
        val output: MutableMap<Int, MutableMap<Int, Floor.CellState>> = mutableMapOf()
        val entities = floorPointsDao.getFloorPointsByID(id)

        for (points in entities) {

            output.getOrPut(ARTracking.convertAxisToIndex(points.x_pos)) { -> mutableMapOf() }
                .putIfAbsent(ARTracking.convertAxisToIndex(points.z_pos), Floor.CellState.FILLED)
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
                    ARTracking.convertIndexToCellCenter(xKey) - cameraPosition.x,
                    ARTracking.convertIndexToCellCenter(zKey) - cameraPosition.z
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

    fun updateFloor(floor: Floor, designSession: DesignSession) {
        var count = getFloorPointCountByID(designSession.floorId)
        for (x in floor.grid.keys) {
            for (z in floor.grid.get(x)?.keys?: setOf()) {
                if (designSession.savedFloorIndexes.get(x)?.contains(z) != true) {
                    val entity = FloorPointsEntity(
                        designSession.floorId,
                        count + 1,
                        ARTracking.convertIndexToCellCenter(x) - designSession.originalCameraPosition.x,
                        ARTracking.convertIndexToCellCenter(z) - designSession.originalCameraPosition.z
                    )
                    floorPointsDao.insertFloorPoint(entity)
                    designSession.savedFloorIndexes.getOrPut(x) { mutableListOf() }
                        .add(z)
                    count++
                }
            }
        }
    }

    fun saveDesign(
        name: String,
        product: DesignSessionProduct,
        floor: Floor,
        cameraPosition: Position,
        cameraRotation: Float
    ): DesignSession {
        val floorId = insertFloor(floor, cameraPosition, cameraRotation, name)
        val floorIndexes: MutableMap<Int, MutableList<Int>> = mutableMapOf()
        for (x in floor.grid.keys) {
            for (z in floor.grid.get(x)?.keys?: setOf()) {
                floorIndexes.getOrPut(x) { mutableListOf() }
                    .add(z)
            }
        }

        product.count = 1

        val designId = designDao.insertDesign(DesignEntity(id= 0, name, floorId, cameraRotation)).toInt()
        designProductsDao.insertDesignProduct(DesignProductsEntity(designId, product))

        val products: MutableList<DesignSessionProduct> = mutableListOf(product)
        return DesignSession(
            designId,
            floorId,
            name,
            cameraPosition,
            cameraRotation,
            floorIndexes,
            products,
            1
        )
    }

    fun updateDesign(
        product: DesignSessionProduct,
        floor: Floor,
        designSession: DesignSession
    ) {
        if (product.count == 0) {
            product.count = designSession.savedProducts.size + 1
        }
        updateFloor(floor, designSession)

        if (designSession.maxCount < product.count) {
            designSession.savedProducts.add(product)
            designSession.maxCount = product.count
            designProductsDao.insertDesignProduct(DesignProductsEntity(designSession.designId, product))
        }
        else {
            val previous = designSession.savedProducts.get(product.count - 1)
            if (previous.position != product.position || previous.rotation != product.rotation) {
                designSession.savedProducts.set(product.count - 1, product)
                designProductsDao.insertDesignProduct(DesignProductsEntity(designSession.designId, product))
            }
        }
    }

    fun removeProductFromDesign(product: DesignSessionProduct, designSession: DesignSession) {
        if (product.count > 0 && designSession.maxCount >= product.count) {
            designProductsDao.removeDesignProduct(DesignProductsEntity(designSession.designId, product))
            designSession.savedProducts.remove(product)
        }
    }

    fun getDesignIDs(): List<Int> {
        return designDao.getDesignIDs()
    }

    fun getDesignByID(id: Int): DesignSession? {
        val entity = designDao.getDesignByID(id)
        if (entity != null) {
            val grid = getFloorPointsByID(entity.floor_id)
            val floorIndexes: MutableMap<Int, MutableList<Int>> = mutableMapOf()
            for (x in grid.keys) {
                for (z in grid.get(x)?.keys?: setOf()) {
                    floorIndexes.getOrPut(x) { mutableListOf() }
                        .add(z)
                }
            }
            val list = designProductsDao.getDesignProductsByID(id)
            var maxCount = 0
            val products: MutableList<DesignSessionProduct> = mutableListOf()
            for (product in list) {
                products.add(DesignSessionProduct(
                    product.count,
                    product.model_category,
                    product.model_name,
                    Position(product.x_pos, 0f, product.z_pos),
                    product.rotation,
                    product.scale,
                    product.allow_walls
                ))
                maxCount = maxOf(maxCount, product.count)
            }
            return DesignSession(
                -1,
                -1,
                "",
                Position(),
                entity.rotation,
                floorIndexes,
                products,
                maxCount
            )
        }
        return null
    }
}