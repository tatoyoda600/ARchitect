package com.pfortbe22bgrupo2.architectapp.utilities

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DatabaseHandler(context: Context) {
    private val database: AppDatabase
    private val floorDao: FloorDao
    private val floorPointsDao: FloorPointsDao
    private val designDao: DesignDao
    private val designProductsDao: DesignProductsDao
    private val firestore: FirebaseFirestore
    private val userId: String

    init {
        // Log.d("FunctionNames", "init")
        database = AppDatabase.getAppDatabase(context)!!
        floorDao = database.floorDao()
        floorPointsDao = database.floorPointsDao()
        designDao = database.designDao()
        designProductsDao = database.designProductsDao()
        firestore = Firebase.firestore
        userId = Firebase.auth.currentUser?.uid?: "INVALID"
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
            Log.e("FLOOR", "New Floor! ${name}")
            return id.toInt()
        }
        catch (error: Exception) {
            Log.e("FLOOR", "Failed: ${error.message.toString()}")
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
                            modelCategory,
                            "",
                            0.0,
                            imageURL,
                            "",
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
        cameraRotation: Float,
        onSuccess: (DesignSession) -> Unit,
        onFailure: () -> Unit
    ) {
        val floorId = insertFloor(floor, cameraPosition, cameraRotation, name)
        val floorIndexes: MutableMap<Int, MutableList<Int>> = mutableMapOf()
        for (x in floor.grid.keys) {
            for (z in floor.grid.get(x)?.keys?: setOf()) {
                floorIndexes.getOrPut(x) { mutableListOf() }
                    .add(z)
            }
        }

        try {
            val designId = designDao.insertDesign(DesignEntity(id= 0, name, floorId, cameraRotation)).toInt()

            product.count = 1
            designProductsDao.insertDesignProduct(DesignProductsEntity(designId, product))

            val products: MutableList<DesignSessionProduct> = mutableListOf(product)

            val documentValues = hashMapOf(
                "category" to product.category,
                "name" to product.name,
                "rotation" to product.rotation,
                "x_pos" to product.position.x,
                "z_pos" to product.position.z,
                "scale" to product.scale,
                "allow_walls" to product.allowWalls
            )
            firestore.collection("designs")
                .document(userId)
                .collection("saved")
                .document(name)
                .collection("productos")
                .document(product.count.toString())
                .set(documentValues, SetOptions.merge())
                .addOnSuccessListener {
                    firestore.collection("designs")
                        .document(userId)
                        .collection("saved")
                        .document(name)
                        .set(hashMapOf("date" to Timestamp.now()))

                    onSuccess(DesignSession(
                        designId,
                        floorId,
                        name,
                        cameraPosition,
                        cameraRotation,
                        floorIndexes,
                        products,
                        1
                    ))
                }
                .addOnFailureListener {err ->
                    Log.e("FIRESTORE", err.toString())
                    err.message?.let { Log.e("FIRESTORE", it) }
                    onFailure()
                }
        }
        catch (e: Exception) {
            Log.e("FIRESTORE", e.toString())
            e.message?.let { Log.e("FIRESTORE", it) }
        }
    }

    fun updateDesign(
        product: DesignSessionProduct,
        floor: Floor,
        designSession: DesignSession,
        onSuccess: () -> Unit,
        onFailure: () -> Unit
    ) {
        if (product.count == 0) {
            product.count = designSession.savedProducts.size + 1
        }
        updateFloor(floor, designSession)

        try {
            if (designSession.maxCount < product.count) {
                designSession.savedProducts.add(product)
                designSession.maxCount = product.count
                designProductsDao.insertDesignProduct(
                    DesignProductsEntity(
                        designSession.designId,
                        product
                    )
                )
            } else {
                val previous = designSession.savedProducts.get(product.count - 1)
                if (previous.position != product.position || previous.rotation != product.rotation) {
                    designSession.savedProducts.set(product.count - 1, product)
                    designProductsDao.insertDesignProduct(
                        DesignProductsEntity(
                            designSession.designId,
                            product
                        )
                    )
                } else {
                    // This product was already saved with the same position and rotation
                    return
                }
            }

            val documentValues = hashMapOf(
                "category" to product.category,
                "name" to product.name,
                "rotation" to product.rotation,
                "x_pos" to product.position.x,
                "z_pos" to product.position.z,
                "scale" to product.scale,
                "allow_walls" to product.allowWalls
            )

            firestore.collection("designs")
                .document(userId)
                .collection("saved")
                .document(designSession.name)
                .collection("productos")
                .document(product.count.toString())
                .set(documentValues, SetOptions.merge())
                .addOnSuccessListener {
                    onSuccess()
                }
                .addOnFailureListener { err ->
                    Log.e("FIRESTORE", err.toString())
                    err.message?.let { Log.e("FIRESTORE", it) }
                    onFailure()
                }
        }
        catch (e: Exception) {
            Log.e("FIRESTORE", e.toString())
            e.message?.let { Log.e("FIRESTORE", it) }
        }
    }

    fun removeProductFromDesign(product: DesignSessionProduct, designSession: DesignSession) {
        if (product.count > 0 && designSession.maxCount >= product.count) {
            try {
                designProductsDao.removeDesignProduct(DesignProductsEntity(designSession.designId, product))
                designSession.savedProducts.remove(product)

                firestore.collection("designs")
                    .document(userId)
                    .collection("saved")
                    .document(designSession.name)
                    .collection("productos")
                    .document(product.count.toString())
                    .delete()
            }
            catch (e: Exception) {
                Log.e("FIRESTORE", e.toString())
                e.message?.let { Log.e("FIRESTORE", it) }
            }
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

    fun getAllFloors(): Map<String, Int> {
        val output: MutableMap<String, Int> = mutableMapOf()
        val floors = floorDao.getAllFloors()

        for (floor in floors) {
            Log.e("FLOOR", "Floor: ${floor.name}")
            output.put(floor.name, floor.id)
        }

        return output
    }

    fun getAllDesigns(): Map<String, Int> {
        val output: MutableMap<String, Int> = mutableMapOf()
        val designs = designDao.getAllDesigns()

        for (design in designs) {
            output.put(design.name, design.id)
        }

        return output
    }

    fun getRemoteDesigns(onSuccess: (String, Int) -> Unit) {
        // Get the locally stored designs
        val savedDesigns = getAllDesigns().toMutableMap()

        // Get the remote designs
        firestore.collection("designs")
            .document(userId)
            .collection("saved")
            .get()
            .addOnSuccessListener { designs ->
                CoroutineScope(Dispatchers.IO).launch {
                    Log.e("FIRESTORE", "Got designs (${designs.documents.size})")

                    for(doc in designs.documents) {
                        Log.e("FIRESTORE", "(${doc.id})")
                        var designId: Int

                        val tempId = savedDesigns.get(doc.id)
                        if (tempId != null) {
                            // If the design is already saved, get the index for updating
                            designId = tempId
                        }
                        else {
                            // If the design isn't already saved, insert an empty floor and design
                            val floorId = insertFloor(Floor(), Position(), 0f, doc.id)
                            designId = designDao.insertDesign(DesignEntity(id= 0, doc.id, floorId, 0f)).toInt()
                        }

                        // Get the remote design's products
                        firestore.collection("designs")
                            .document(userId)
                            .collection("saved")
                            .document(doc.id)
                            .collection("productos")
                            .get()
                            .addOnSuccessListener { products ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    Log.e("FIRESTORE", "Got products")

                                    for (product in products.documents) {
                                        val count = product.id.toIntOrNull()
                                        if (count != null) {
                                            product.data?.let {
                                                val category = it.get("category")
                                                val name = it.get("name")
                                                val rotation = it.get("rotation")
                                                val x_pos = it.get("x_pos")
                                                val z_pos = it.get("z_pos")
                                                val scale = it.get("scale")
                                                val allow_walls = it.get("allow_walls")

                                                if (
                                                    category is String
                                                    && name is String
                                                    && rotation is Number
                                                    && x_pos is Number
                                                    && z_pos is Number
                                                    && scale is Number
                                                    && allow_walls is Boolean
                                                ) {
                                                    // Insert the remote design's product into the local database
                                                    designProductsDao.insertDesignProduct(
                                                        DesignProductsEntity(
                                                            designId,
                                                            DesignSessionProduct(
                                                                count,
                                                                category,
                                                                name,
                                                                Position(
                                                                    x_pos.toFloat(),
                                                                    0f,
                                                                    z_pos.toFloat()
                                                                ),
                                                                rotation.toFloat(),
                                                                scale.toFloat(),
                                                                allow_walls
                                                            )
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            .addOnFailureListener { err ->
                                Log.e("FIRESTORE", err.toString())
                                err.message?.let { Log.e("FIRESTORE", it) }
                            }

                        // Notify design name and ID
                        onSuccess(doc.id, designId)
                    }
                }
            }
            .addOnFailureListener { err ->
                Log.e("FIRESTORE", err.toString())
                err.message?.let { Log.e("FIRESTORE", it) }
            }
    }

    /**En este metodo se crea un post con todos sus campos y colecciones interiores
     * @param downloadUrl: String de link para descarga de imagen post
     * @param description: Descripcion de post
     * @param title: Titulo de Post
     * @param user: Nombre del usuario que hace el post*/
    fun crearPost(downloadUrl: String, description: String, title: String, user: String) {
        val collectionRef = firestore.collection("posts")
        val newDocumentRef = collectionRef.document()

        val data = hashMapOf(
            "description" to description,
            "title" to title,
            "user" to user,
            "image" to downloadUrl,
            "likesCount" to 0
        )

        newDocumentRef.set(data)
            .addOnSuccessListener {
                // La creación del documento fue exitosa

                // Ahora, agregar una subcolección al documento
                newDocumentRef.collection("comments")

                newDocumentRef.collection("likes")
            }
    }

    fun getUserName(
        uid: String,
        onSuccess: (String) -> Unit,
        onFailure: () -> Unit)
    {
        val docRef = firestore.collection("users").document(uid)

        docRef.get()
            .addOnSuccessListener { document: DocumentSnapshot ->
                val data = document.data
                if (data != null) {
                    val userName = data.get("userName").toString()
                    onSuccess(userName)
                }
            }
            .addOnFailureListener {err ->
                Log.e("FIRESTORE", err.toString())
                err.message?.let { Log.e("FIRESTORE", it) }
                onFailure()
            }
    }


}