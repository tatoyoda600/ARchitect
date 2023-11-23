package com.pfortbe22bgrupo2.architectapp.utilities

import android.content.Context
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.ViewManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.ar.core.PointCloud
import com.pfortbe22bgrupo2.architectapp.databinding.ModelPopupMenuBinding
import com.pfortbe22bgrupo2.architectapp.types.DesignSession
import com.pfortbe22bgrupo2.architectapp.types.DesignSessionProduct
import com.pfortbe22bgrupo2.architectapp.types.Floor
import com.pfortbe22bgrupo2.architectapp.types.Int3
import com.pfortbe22bgrupo2.architectapp.types.ModelPoint
import com.pfortbe22bgrupo2.architectapp.types.Point
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.ArFrame
import io.github.sceneview.ar.arcore.position
import io.github.sceneview.ar.arcore.rotation
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.math.toVector3
import io.github.sceneview.node.Node
import io.github.sceneview.renderable.Renderable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.abs

private const val CONFIRMED_POINT_MODEL = "models/square.glb"
private const val OUTLINE_POINT_MODEL = "models/outline_square.glb"
private const val ORANGE_POINT_MODEL = "models/orangeSquare.glb"
private const val PINK_POINT_MODEL = "models/pinkSquare.glb"

private const val ONE_POINT_PER_CELL = true // Points that fall into an occupied cell are discarded (Confirmed points will replace unconfirmed points)

// After this many non-confirmed points are registered, they are all purged
// 3000 when 1 point/cell, 5000 otherwise (Kotlin doesn't allow preprocessing branches for constants)
private const val MAX_EXCESS_POINTS = ONE_POINT_PER_CELL.compareTo(false) *
        3000 +
        (1 - ONE_POINT_PER_CELL.compareTo(false)) *
        5000

private const val EXCESS_POINTS_CLEAN_UP_STEP = 1000 // After this many non-confirmed points are registered, a clean up ocurrs
private const val CONFIRMED_POINTS_CLEAN_UP_STEP = 500 // After this many confirmed points are registered, a clean up ocurrs
private const val CLEAN_UP_CELL_MIN_POINTS = 2 // Cells with less than this many points will be emptied
private const val CLEAN_UP_CELL_MAX_POINTS = 4 // Cells with more than this many points will be culled

private const val MIN_CONFIDENCE = 0.3 // How confident the sensors have to be about a point to not discard it (0 - 1)
private const val MAX_CELL_DISTANCE = 1 // The radius of cells to check for neighbors
private const val MAX_DISTANCE = MAX_CELL_DISTANCE.toFloat() / DICT_COORD_ZOOM.toFloat() // MAX_CELL_DISTANCE but in distance units
private const val MIN_NEIGHBORS = 3 // The amount of neighbors needed for a point to be confirmed

private const val FLOOR_HEIGHT_CELL_LEEWAY = 1 // Make use of points that aren't at the floor height, but are close enough
private const val FLOOR_LEEWAY_MIN_NEIGHBORS = 5 // The amount of neighbors that the points given leeway must have to be used

private const val CONFIRMED_POINTS_FLOOR_CHECK_STEP = 50 // After this many confirmed points are registered, a floor check ocurrs
private const val FRAMES_PER_CURSOR_UPDATE = 3 // Limit how often the cell cursor is updated

class DefaultARTracking(
    checksPerSecond: Int,
    sceneView: ArSceneView,
    progressBar: CircularProgressIndicator,
    switchToDefaultLayout: () -> Unit,
    switchToPlacementLayout: () -> Unit,
    private var onFloorDetectedFunction: () -> Unit = fun() {} // A function to run when a floor height is discovered
) : FloorBasedTracking(checksPerSecond, sceneView, progressBar, switchToDefaultLayout, switchToPlacementLayout) {
    private var lastExcessCleanUpStep = 0
    private var lastConfirmedCleanUpStep = 0
    private var lastConfirmedFloorCheckStep = 0
    private var coloredFloor: Map<Int, Map<Int, Floor.CellState>>? = null // A grid representing the current colored floor area
    private var cursorFrameCount = 0

    private var designSession: DesignSession? = null
    private var arProducts: MutableMap<Node, DesignSessionProduct> = mutableMapOf()

    lateinit internal var actionsPopup: ViewGroup
    private var popupNode: Node? = null
    private var popupNodeAllowWalls: Boolean = false

    /** Takes care of setting up the AR scene. */
    override fun setup() {
        super.setup()
        val popupBinding = ModelPopupMenuBinding.inflate(LayoutInflater.from(sceneView.context))
        actionsPopup = popupBinding.root
        val root = sceneView.rootView
        if (root is ViewManager) {
            val wrapContent = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            root.addView(actionsPopup, wrapContent)
            hideActionsPopup()
            popupBinding.moveProductBtn.setOnClickListener {
                popupNode?.let {
                    startPlacement(it, popupNodeAllowWalls) { node ->
                        val allowWalls = popupNodeAllowWalls
                        node.onTap = { motionEvent: MotionEvent, renderable: Renderable? ->
                            popupNode = node
                            popupNodeAllowWalls = allowWalls
                            showActionsPopup(motionEvent.x, motionEvent.y)
                        }
                    }
                }
            }
            popupBinding.deleteProductBtn.setOnClickListener {
                popupNode?.let {
                    val product = arProducts.get(it)
                    if (product != null) {
                        designSession?.let {
                            CoroutineScope(Dispatchers.IO).launch {
                                database.removeProductFromDesign(product, it)
                            }
                        }
                    }

                    it.detachFromScene(sceneView)
                    it.parent = null
                    popupNode = null
                }
            }
            popupBinding.saveProductBtn.setOnClickListener {
                popupNode?.let {
                    saveDesign(it)
                }
            }
        }
    }

    /** Resets the AR state. */
    override fun reset() {
        lastExcessCleanUpStep = 0
        lastConfirmedCleanUpStep = 0
        lastConfirmedFloorCheckStep = 0
        super.reset()
    }

    /** Renders a confirmed point. */
    override fun renderConfirmedPoint(position: Position, index: Int) {
        // Log.d("FunctionNames", "renderConfirmedPoint")
        colorPoint(position, Floor.CellState.UNKNOWN)
    }

    /** Adds a point from the floor that's being loaded in. */
    override fun loadFloorPoint(position: Position) {
        // Log.d("FunctionNames", "loadFloorPoint")
        colorPoint(position, Floor.CellState.FILLED)
    }

    /** Process each AR frame.
     *
     * Discard repeated and not confident points. Classify points with enough neighbors as confirmed points.
     *
     * If a floor height has been found, use it to further filter the points that were detected. */
    override fun frameUpdate(arFrame: ArFrame) {
        // If an object is currently trying to be placed, update the position of the object along with the camera
        placementNode?.let { updatePlacementPos?.invoke(it) }

        if (useFloorHeight) {
            cursorFrameCount = (cursorFrameCount + 1) % FRAMES_PER_CURSOR_UPDATE
            if (cursorFrameCount == 0) {
                val lookPoint = getLookPoint(showCellCursor = true, useOnWall = true)
            }
        }

        val pointCloud: PointCloud = arFrame.frame.acquirePointCloud()
        if (pointCloud.ids != null && pointCloud.timestamp != lastFrame?.frame?.timestamp) {
            lastFrame = arFrame

            val pointCount: Int = pointCloud.ids.limit()
            for (i in 0 until pointCount) {
                if (!pointIds.contains(pointCloud.ids[i])) {
                    val index = i * 4
                    val position = Position(
                        pointCloud.points[index],
                        pointCloud.points[index + 1],
                        pointCloud.points[index + 2]
                    )
                    val confidence = pointCloud.points[index + 3]

                    if (confidence > MIN_CONFIDENCE) {
                        if (!useFloorHeight || convertAxisToIndex(position.y) == floorHeight) {
                            val point = Point(pointCloud.ids[i], position)
                            if (addPoint(point) >= MIN_NEIGHBORS) {
                                onConfirmedPoint(point)
                            }
                        }
                        // Not at floor height
                        else {
                            // Within leeway range
                            if (abs(convertAxisToIndex(position.y) - floorHeight).toFloat() < FLOOR_HEIGHT_CELL_LEEWAY) {
                                position.y = convertIndexToCellCenter(floorHeight)
                                val point = Point(pointCloud.ids[i], position)
                                if (addPoint(point, leeway = true) >= FLOOR_LEEWAY_MIN_NEIGHBORS) {
                                    onConfirmedPoint(point)
                                }
                                else {
                                    val keys = convertPosToIndexes(position)

                                    clearExcessPoints(keys.x, keys.y, keys.z)

                                    if ((points.get(keys.x)?.get(keys.y)?.get(keys.z)?.size ?: 0) == 0) {
                                        points.get(keys.x)?.get(keys.y)?.remove(keys.z)
                                        if ((points.get(keys.x)?.get(keys.y)?.size ?: 0) == 0) {
                                            points.get(keys.x)?.remove(keys.y)
                                            if ((points.get(keys.x)?.size ?: 0) == 0) {
                                                points.remove(keys.x)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (confirmedPoints.size > lastConfirmedCleanUpStep + CONFIRMED_POINTS_CLEAN_UP_STEP) {
                cleanUpConfirmedPoints()
            }

            if (pointIds.size - confirmedPoints.size > lastExcessCleanUpStep + EXCESS_POINTS_CLEAN_UP_STEP) {
                if (!ONE_POINT_PER_CELL) {
                    cleanUpCells(this@DefaultARTracking::cleanUpExcessPoints)
                    if (pointIds.size - confirmedPoints.size > lastExcessCleanUpStep + EXCESS_POINTS_CLEAN_UP_STEP) {
                        lastExcessCleanUpStep += EXCESS_POINTS_CLEAN_UP_STEP
                    }
                }

                if (pointIds.size - confirmedPoints.size > MAX_EXCESS_POINTS) {
                    cleanUpCells(this@DefaultARTracking::clearExcessPoints)
                }
            }
        }
    }

    /** When a verified point is found, add it to the confirmed points list and render the cell's tile. */
    override fun onConfirmedPoint(point: Point) {
        confirmedPoints += ModelPoint(
            if (!useFloorHeight)
                null
            else
                renderer.render(
                    modelPath = CONFIRMED_POINT_MODEL,
                    position = convertIndexesToCellCenter(convertPosToIndexes(point.position)),
                    rotation = Rotation()
                ),
            point.id,
            point.position
        )

        if (useFloorHeight) {
        }
        else {
            confirmedPoints += ModelPoint(
                null,
                point.id,
                point.position
            )
        }

        if (!useFloorHeight && confirmedPoints.size > lastConfirmedFloorCheckStep + CONFIRMED_POINTS_FLOOR_CHECK_STEP) {
            lastConfirmedFloorCheckStep += CONFIRMED_POINTS_FLOOR_CHECK_STEP
            val floor = calculateFloorHeight()
            if (floor.second > MIN_FLOOR_POINTS) {
                floorHeight = floor.first
                useFloorHeight = true
                clearNonFloorPoints()
                onFloorDetectedFunction()
                setPaused(false)
            }
        }
    }

    /** When a point is found, add it to the list and calculate its neighbor count. */
    private fun addPoint(point: Point, leeway: Boolean = false, skipNeighbors: Boolean = false): Int {
        if (point.id != Int.MIN_VALUE) {
            pointIds += point.id
        }
        val position = point.position
        val pointIndex = convertPosToIndexes(position)
        val cellPoints = points.get(pointIndex.x)?.get(pointIndex.y)?.get(pointIndex.z)

        // If each cell is limited to 1 point and the cell for this point already contains a point
        if (ONE_POINT_PER_CELL && (cellPoints?.size?: 0) > 0) {
            val cellPointsId = cellPoints?.elementAtOrNull(0)?.id
            // If the point in the cell is not a confirmed point
            if (cellPointsId != Int.MIN_VALUE && confirmedPoints.find { it.id == cellPointsId } == null) {
                // Remove the point in the cell
                pointIds.remove(cellPointsId)
                points.get(pointIndex.x)?.get(pointIndex.y)?.get(pointIndex.z)?.removeAt(0)
            }
            // If the point in the cell is a confirmed point
            else {
                // Discard this new point
                return -1
            }
        }

        points.getOrPut(pointIndex.x) { mutableMapOf() }
            .getOrPut(pointIndex.y) { mutableMapOf() }
            .getOrPut(pointIndex.z) { mutableListOf() }
            .add(point)

        if (skipNeighbors) {
            return -1
        }

        var neighborCount = -1 // Start at -1 since the point itself will be counted
        for (p in cellPoints?: emptyList()) {
            if ((p.position.xyz - position.xyz).toVector3().length() < MAX_DISTANCE) {
                neighborCount++
                if ((leeway && neighborCount > FLOOR_LEEWAY_MIN_NEIGHBORS) || neighborCount >= MIN_NEIGHBORS) return neighborCount
            }
        }

        for (scanX in convertAxisToIndex(position.x - MAX_DISTANCE)..convertAxisToIndex(position.x + MAX_DISTANCE)) {
            for (scanY in convertAxisToIndex(position.y - MAX_DISTANCE)..convertAxisToIndex(position.y + MAX_DISTANCE)) {
                for (scanZ in convertAxisToIndex(position.z - MAX_DISTANCE)..convertAxisToIndex(position.z + MAX_DISTANCE)) {
                    for (p in cellPoints?: emptyList()) {
                        if ((p.position.xyz - position.xyz).toVector3().length() < MAX_DISTANCE) {
                            neighborCount++
                            if ((leeway && neighborCount > FLOOR_LEEWAY_MIN_NEIGHBORS) || neighborCount >= MIN_NEIGHBORS) return neighborCount
                        }
                    }
                }
            }
        }
        return neighborCount
    }

    /** Clean up a cell with too many points, or clear one with too few. */
    private fun cleanUpExcessPoints(xKey: Int, yKey: Int, zKey: Int): Int {
        var cleanUpCount = 0

        val cellPoints = points.get(xKey)?.get(yKey)?.get(zKey)
        // If this cell has too many or too few points
        if (cellPoints?.size != null && cellPoints.size !in CLEAN_UP_CELL_MIN_POINTS..CLEAN_UP_CELL_MAX_POINTS) {
            for (p in (cellPoints.lastIndex?: -1)downTo 0) {
                val cellPointsId = cellPoints.get(p).id
                // If this point is not a confirmed one
                if (cellPointsId != Int.MIN_VALUE && confirmedPoints.find { it.id == cellPointsId } == null) {
                    // Remove the point
                    pointIds.remove(cellPointsId)
                    points.get(xKey)?.get(yKey)?.get(zKey)?.removeAt(p)
                    cleanUpCount++
                }
            }
        }

        return cleanUpCount
    }

    /** Clean up cells with more than 1 confirmed point. */
    private fun cleanUpConfirmedPoints(): Int {
        if (ONE_POINT_PER_CELL) return 0

        var cleanUpCount = 0
        // Loop through every confirmed point
        for (point in confirmedPoints.lastIndex downTo 0) {
            val position = confirmedPoints[point].position
            val pointIndex = convertPosToIndexes(position)
            val cellPoints = points.get(pointIndex.x)?.get(pointIndex.y)?.get(pointIndex.z)

            // If its cell contains more points
            if (cellPoints != null && cellPoints.size > 1) {
                // If there is a previous confirmed point in that cell
                if (findFirstConfirmedPointInCell(pointIndex) != point) {
                    // Remove the point
                    val cellPointsId = confirmedPoints.get(point).id
                    if (cellPointsId != Int.MIN_VALUE) {
                        pointIds.remove(cellPointsId)
                    }
                    points.get(pointIndex.x)?.get(pointIndex.y)?.get(pointIndex.z)?.removeAll { it.id == cellPointsId }
                    confirmedPoints.get(point).model?.detachFromScene(sceneView)
                    confirmedPoints.get(point).model?.parent = null
                    confirmedPoints.removeAt(point)
                    cleanUpCount++
                }
            }
        }

        if (confirmedPoints.size > lastConfirmedCleanUpStep + CONFIRMED_POINTS_CLEAN_UP_STEP) {
            lastConfirmedCleanUpStep += CONFIRMED_POINTS_CLEAN_UP_STEP
        }
        return cleanUpCount
    }

    /** Colors the cell tile that contains the given position.
     *
     * If the cell has no points, adds a new confirmed point (ID: INT_MIN) there. */
    private fun colorPoint(point: Position, state: Floor.CellState) {
        val pointIndexes = convertPosToIndexes(point)
        val confirmedIndex: Int? = findFirstConfirmedPointInCell(pointIndexes)

        if (confirmedIndex != null) {
            val position = confirmedPoints.get(confirmedIndex).model?.position?: convertIndexesToCellCenter(pointIndexes)
            val rotation = confirmedPoints.get(confirmedIndex).model?.rotation?: Rotation()

            val newModel: Node = renderer.render(
                modelPath = if (state === Floor.CellState.UNKNOWN)
                        CONFIRMED_POINT_MODEL
                    else
                        OUTLINE_POINT_MODEL,
                position = position,
                rotation = rotation
            )

            confirmedPoints.get(confirmedIndex).model?.detachFromScene(sceneView)
            confirmedPoints.get(confirmedIndex).model?.parent = null
            confirmedPoints.get(confirmedIndex).model = newModel
        }
        // If there are no confirmed points in the given cell
        else {
            // Creates a new confirmed point
            val newPoint: Point = Point(Int.MIN_VALUE, point)
            addPoint(newPoint, skipNeighbors = true)

            confirmedPoints += ModelPoint(
                renderer.render(
                    modelPath = if (state === Floor.CellState.UNKNOWN)
                        CONFIRMED_POINT_MODEL
                    else
                        OUTLINE_POINT_MODEL,
                    position = convertIndexesToCellCenter(pointIndexes),
                    rotation = Rotation()
                ),
                Int.MIN_VALUE,
                point
            )
        }
    }

    /** Colors all the cell tiles of a detected Floor. */
    fun paintFloor() {
        for (x in detectedFloor.grid) {
            for (z in x.value) {
                if (coloredFloor == null
                    //|| (coloredFloor?.get(x.key)?.get(z.key)?: Floor.CellState.UNKNOWN) == Floor.CellState.UNKNOWN // Regular
                    || (coloredFloor?.get(x.key)?.get(z.key)?: Floor.CellState.UNKNOWN) != z.value // For painting cells according to their floor cell state, for debugging purposes
                ) {
                    colorPoint(convertIndexesToCellCenter(Int3(x.key, floorHeight, z.key)), z.value)
                }
            }
        }
        coloredFloor = detectedFloor.getGridCopy()
    }

    fun renderModel(
        modelCategory: String,
        modelName: String,
        scale: Float,
        allowWalls: Boolean
    ) {
        super.renderModel(modelCategory, modelName, scale, allowWalls) { node ->
            node.onTap = { motionEvent: MotionEvent, renderable: Renderable? ->
                popupNode = node
                popupNodeAllowWalls = allowWalls
                showActionsPopup(motionEvent.x, motionEvent.y)
            }

            arProducts.put(node, DesignSessionProduct(count=0, modelCategory, modelName, node.position, node.rotation.y, scale, allowWalls))
        }
    }

    /** Saves/Updates the current design in the database. */
    private fun saveDesign(productNode: Node) {
        val arProduct = arProducts.get(productNode)

        if (arProduct != null) {
            if (designSession != null) {
                setPaused(true)
                CoroutineScope(Dispatchers.IO).launch {
                    database.updateDesign(arProduct, detectedFloor, designSession!!)

                    setPaused(false)
                }
            }
            else {
                val input = EditText(sceneView.context)
                input.inputType = InputType.TYPE_CLASS_TEXT

                val dialog = AlertDialog.Builder(sceneView.context)
                    .setTitle("Save Design")
                    .setView(input)
                    .setPositiveButton("Save", null)
                    .setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }
                    .show()

                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    val text = input.text.toString()
                    if (text.isNotBlank()) {
                        setPaused(true)
                        CoroutineScope(Dispatchers.IO).launch {
                            // The camera's -X rotation is its yaw rotation
                            designSession = database.saveDesign(
                                text,
                                arProduct,
                                detectedFloor,
                                lastFrame!!.camera.pose.position,
                                -lastFrame!!.camera.pose.rotation.x
                            )

                            setPaused(false)
                        }
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    fun loadDesign(id: Int) {
        if (useFloorHeight) {
            setPaused(true)

            placementNode?.let {
                it.detachFromScene(sceneView)
                it.parent = null
                placementNode = null
            }

            CoroutineScope(Dispatchers.IO).launch {
                val designData = database.getDesignByID(id)
                if (designData != null) {
                    val cameraPosition = convertPosToIndexes(lastFrame!!.camera.pose.position)
                    val floorPosition = Int3(cameraPosition.x, floorHeight, cameraPosition.z)
                    // Nodes use their Y rotation for yaw
                    val floorRotation = Rotation(0f, designData.originalCameraRotation, 0f)

                    val parentNode = renderer.createEmptyNode(
                        convertIndexesToCellCenter(floorPosition),
                        floorRotation
                    )

                    val productHolderNode = renderer.createEmptyNode(Position(), Rotation())
                    productHolderNode.parent = parentNode

                    // Place the furniture
                    for (product in designData.savedProducts) {
                        renderer.renderFromFirebase(
                            product.category,
                            product.name,
                            product.position,
                            Rotation(0f, product.rotation, 0f),
                            Scale(product.scale),
                            { node ->
                                //On Success
                                node.parent = productHolderNode
                                node.onTap = { motionEvent: MotionEvent, renderable: Renderable? ->
                                    popupNode = node
                                    popupNodeAllowWalls = product.allowWalls
                                    showActionsPopup(motionEvent.x, motionEvent.y)
                                }

                                arProducts.put(node, DesignSessionProduct(count=0, product.category, product.name, node.position, node.rotation.y, product.scale, product.allowWalls))
                            },
                            {
                                Log.e("Firebase", "Failed to render model from Firebase")
                            }
                        )
                    }

                    // Place the floor
                    for (x in designData.savedFloorIndexes.keys) {
                        for (z in designData.savedFloorIndexes.get(x)?: listOf()) {
                            val pointPos = convertIndexesToCellCenter(Int3(x, 0, z))
                            pointPos.y = 0f

                            // Changing the parent of a node doesn't change its local position, rotation, or scale, so the loaded position can just be used directly
                            val node: Node = renderer.render(
                                modelPath = FLOOR_LOADING_MODEL,
                                position = pointPos,
                                rotation = Rotation()
                            )
                            node.parent = parentNode
                        }
                    }

                    placementNode = parentNode
                    onPlacement = { node ->
                        for (product in node.children.get(0).children) {
                            val worldPos = product.worldPosition
                            val worldRot = product.worldRotation
                            product.parent = sceneView
                            product.position = worldPos
                            product.rotation = worldRot * -1f // For some reason the rotation gets flipped when set, so you've got to pre-flip it
                        }
                        for (child in node.children) {
                            loadFloorPoint(child.worldPosition)
                        }

                        node.detachFromScene(sceneView)
                        node.parent = null
                    }
                    updatePlacementPos = { node ->
                        val camPose = lastFrame?.camera?.pose
                        if (camPose != null) {
                            node.position = Position(camPose.position.x, convertIndexToCellCenter(floorHeight), camPose.position.z)
                            // The camera's -X rotation is its yaw rotation, unlike nodes which use their Y rotation for yaw
                            node.rotation = Rotation(0f, -camPose.rotation.x, 0f)
                        }
                    }
                    placementScreenLayout()
                }
                else {
                    Log.e("ARTracking", "Failed to Find Design")
                    val ids = database.getDesignIDs()
                    for (i in ids) {
                        Log.d("DefaultARTracking", "Design ID: ${i}")
                    }
                }

                setPaused(false)
            }
        }
    }

    /** Shows the node actions popup. */
    fun showActionsPopup(x: Float, y: Float) {
        actionsPopup.x = x - actionsPopup.width * 0.5f
        actionsPopup.y = y - actionsPopup.height * 0.5f
    }

    /** Hides the node actions popup. */
    fun hideActionsPopup() {
        showActionsPopup(-1000f, -1000f)
    }
}