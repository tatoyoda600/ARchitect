package com.pfortbe22bgrupo2.architectapp.utilities

import android.content.Context
import android.util.Log
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.ar.sceneform.math.Vector3
import com.pfortbe22bgrupo2.architectapp.types.Floor
import com.pfortbe22bgrupo2.architectapp.types.Int3
import com.pfortbe22bgrupo2.architectapp.types.Outline
import com.pfortbe22bgrupo2.architectapp.types.PosDir
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.position
import io.github.sceneview.ar.arcore.rotation
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.math.toFloat3
import io.github.sceneview.node.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max

internal const val CELL_CURSOR_MODEL = "models/pinkSquare.glb"
internal const val FLOOR_LOADING_MODEL = "models/outline_square.glb"
internal const val MIN_FLOOR_POINTS = 20 // A height must have at least this many points to be considered a valid floor
internal const val DELAY_MULTIPLIER: Long = 10 // Multiplies most async delays

abstract class FloorBasedTracking(
    checksPerSecond: Int,
    sceneView: ArSceneView,
    progressBar: CircularProgressIndicator,
    switchToDefaultLayout: () -> Unit,
    switchToPlacementLayout: () -> Unit
) : ARTracking(checksPerSecond, sceneView, progressBar, switchToDefaultLayout, switchToPlacementLayout) {
    internal var useFloorHeight = false // Changes point verification after a floor height has been discovered
    internal var floorHeight = Int.MAX_VALUE // The cell height of the floor

    internal var detectedFloor: Floor = Floor() // A grid representing the current confirmed floor area
    internal var placementNode: Node? = null // An empty node with an entire floor as children, which will be destroyed when the user decides to lock the floor in place
    internal var cellCrosshair: Node? = null
    internal var onPlacement: ((Node) -> Unit)? = null
    internal var updatePlacementPos: ((Node) -> Unit)? = null

    /** Renders a confirmed point. */
    internal abstract fun renderConfirmedPoint(position: Position, index: Int)

    /** Adds a point from the floor that's being loaded in. */
    internal abstract fun loadFloorPoint(position: Position)

    /** Takes care of setting up the AR scene. */
    override fun setup() {
        super.setup()
        cellCrosshair = renderer.render(CELL_CURSOR_MODEL, Position(), Rotation(), Scale(1f))
        cellCrosshair?.isVisible = false
    }

    /** Resets the AR state. */
    override fun reset() {
        useFloorHeight = false
        floorHeight = Int.MAX_VALUE
        detectedFloor = Floor()
        super.reset()
    }

    /** Confirm the current scan and start the conversion to a Floor, or update a previous Floor with the scan.
     *
     * If a floor is being loading in, it'll first use its points to create a Floor, or add to the previous Floor */
    fun confirm(onFinish: () -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            setPaused(true)
            val outlineAsync = async{ getFloorOutline() }
            val outlines: Outline = outlineAsync.await()
            delay(10 * DELAY_MULTIPLIER)

            val floor: Floor = if (detectedFloor.height != Int.MAX_VALUE) detectedFloor else Floor(mutableMapOf(), floorHeight)

            for (s in outlines.segments) {
                for (p in s) {
                    val rightToInside: Int3 = -p.prevDir
                    val angle1: Float = MathUtils.angleOf2DVector(rightToInside.x.toFloat(), rightToInside.z.toFloat())
                    val leftToInside: Int3 = p.nextDir
                    val angle2: Float = MathUtils.angleOf2DVector(leftToInside.x.toFloat(), leftToInside.z.toFloat()) - 360
                    floor.addPoint(convertAxisToIndex(p.position.x), convertAxisToIndex(p.position.z), emitEdge= (angle1 != 0f && angle2 != 0f && angle1 - angle2 < 360))
                }
            }

            delay(10 * DELAY_MULTIPLIER)

            val floorAsync = async{ floor.fillFloor() }
            floorAsync.await()

            delay(10 * DELAY_MULTIPLIER)
            detectedFloor = floor
            CoroutineScope(Dispatchers.Main).launch {
                onFinish()
                setPaused(false)
            }
        }
    }

    /** Clear all points not at floor height. */
    fun clearNonFloorPoints(): Int {
        setPaused(true)
        runBlocking {
            delay(10 * DELAY_MULTIPLIER)
        }

        var cleanUpCount = cleanUpCells(this::clearExcessPoints)

        runBlocking {
            delay(10 * DELAY_MULTIPLIER)
        }

        // Loop through every confirmed point
        for (point in confirmedPoints.lastIndex downTo 0) {
            val position = confirmedPoints[point].position
            val pointIndex = convertPosToIndexes(position)
            val cellPoints = points.get(pointIndex.x)?.get(pointIndex.y)?.get(pointIndex.z)

            // If this point is not at floorHeight
            if (pointIndex.y != floorHeight) {
                for (p in (cellPoints?.lastIndex?: -1)downTo 0) {
                    val cellPointsId = confirmedPoints[point].id
                    if (cellPointsId == cellPoints?.get(p)?.id) {
                        // Remove the point
                        if (cellPointsId != Int.MIN_VALUE) {
                            pointIds.remove(cellPointsId)
                        }
                        points.get(pointIndex.x)?.get(pointIndex.y)?.get(pointIndex.z)?.removeAt(p)
                        confirmedPoints.get(point).model?.detachFromScene(sceneView)
                        confirmedPoints.get(point).model?.parent = null
                        confirmedPoints.removeAt(point)
                        cleanUpCount++
                        break
                    }
                    runBlocking {
                        delay(1)
                    }
                }
            }
            else {
                renderConfirmedPoint(position, point)
            }
        }

        runBlocking {
            delay(10 * DELAY_MULTIPLIER)
        }

        // Remove all cells with Y different from the floor height
        for (xi in (points.keys.size?: 0) -1 downTo 0) {
            val xKey = points.keys.elementAt(xi)

            for (yi in (points.get(xKey)?.keys?.size?: 0) -1 downTo 0) {
                val yKey = points.get(xKey)?.keys?.elementAt(yi)

                if (yKey != floorHeight) {
                    for (zi in (points.get(xKey)?.get(yKey)?.keys?.size?: 0) -1 downTo 0) {
                        val zKey = points.get(xKey)?.get(yKey)?.keys?.elementAt(zi)
                        if ((points.get(xKey)?.get(yKey)?.get(zKey)?.size?: 0) -1 == 0) {
                            points.get(xKey)?.get(yKey)?.remove(zKey)
                        }
                    }

                    if ((points.get(xKey)?.get(yKey)?.size ?: 0) == 0) {
                        points.get(xKey)?.remove(yKey)
                    }
                }
            }

            if ((points.get(xKey)?.size ?: 0) == 0) {
                points.remove(xKey)
            }
        }

        return cleanUpCount
    }

    /** Get the floor height.
     *
     * Calculates the amount of confirmed points at each height.
     *
     * Returns a pair containing (Height with the most points; Amount of points at that height). */
    fun calculateFloorHeight(): Pair<Int,Int> {
        val pointsPerHeight = mutableMapOf<Int,Int>()
        for (p in confirmedPoints) {
            val y = convertAxisToIndex(p.position.y)
            if (!pointsPerHeight.containsKey(y)) {
                pointsPerHeight[y] = confirmedPointsAtHeight(y)
            }
        }

        var curMax: Int = -1
        var curHeight: Int = Int.MAX_VALUE
        for (p in pointsPerHeight) {
            if (p.value > curMax) {
                curMax = p.value
                curHeight = p.key
            }
            else if (p.value == curMax && p.key < curHeight) {
                curHeight = p.key
            }
        }

        return Pair(curHeight, curMax)
    }

    /** Given a height, calculates how many confirmed points are in cells at that height. */
    private fun confirmedPointsAtHeight(height: Int): Int {
        var floorChance = 0
        // Loop through every confirmed point
        for (point in confirmedPoints.lastIndex downTo 0) {
            // If the point's height is the same as the provided height
            if (convertAxisToIndex(confirmedPoints[point].position.y) == height) {
                floorChance++
            }
        }
        return floorChance
    }

    /** Calculates 4 partial/completed outlines, each starting from a different extreme of the grid.
     *
     * Completed outlines have the same first and last point. */
    private suspend fun getFloorOutline(): Outline {
        if (!useFloorHeight || floorHeight == Int.MAX_VALUE || confirmedPoints.size < MIN_FLOOR_POINTS) {
            Log.e("ARTracking", "Floor outline requires the floor height to be active")
            return Outline(emptyList())
        }

        val minX = Int3(
            points.keys.min(),
            floorHeight,
            points.get(points.keys.min())?.get(floorHeight)?.keys?.first() ?: Int.MAX_VALUE
        )
        val maxX = Int3(
            points.keys.max(),
            floorHeight,
            points.get(points.keys.max())?.get(floorHeight)?.keys?.first() ?: Int.MAX_VALUE
        )

        var minZ: Int3 = Int3(0, floorHeight, Int.MAX_VALUE)
        var maxZ: Int3 = Int3(0, floorHeight, Int.MIN_VALUE)
        for (xKey in points.keys) {
            if ((points.get(xKey)?.get(floorHeight)?.keys?.min() ?: minZ.z) < minZ.z) {
                minZ = Int3(
                    xKey,
                    floorHeight,
                    points.get(xKey)?.get(floorHeight)?.keys?.min() ?: minZ.z
                )
            }
            if ((points.get(xKey)?.get(floorHeight)?.keys?.max() ?: maxZ.z) > maxZ.z) {
                maxZ = Int3(
                    xKey,
                    floorHeight,
                    points.get(xKey)?.get(floorHeight)?.keys?.max() ?: maxZ.z
                )
            }
        }

        delay(10 * DELAY_MULTIPLIER)

        // Using the furthest point on each end of the X and Z axis
        //      find as many contiguous points as possible
        //      If the points all connect, then that's a completed outline

        // Minimum X point, moving towards -Z
        val pointList1: List<PosDir> = findConnectedPoints(
            minX,
            Int3(0,0,-1)
        )
        delay(10 * DELAY_MULTIPLIER)

        // Maximum X point, moving towards +Z
        val pointList2: List<PosDir> = findConnectedPoints(
            maxX,
            Int3(0,0,+1)
        )
        delay(10 * DELAY_MULTIPLIER)

        // Minimum Z point, moving towards -X
        val pointList3: List<PosDir> = findConnectedPoints(
            minZ,
            Int3(-1,0,0)
        )
        delay(10 * DELAY_MULTIPLIER)

        // Maximum Z point, moving towards +X
        val pointList4: List<PosDir> = findConnectedPoints(
            maxZ,
            Int3(+1,0,0)
        )

        return Outline(listOf(pointList1, pointList2, pointList3, pointList4))
    }

    /** Saves the confirmed floor in the database. */
    fun saveFloor(floorName: String) {
        setPaused(true)
        CoroutineScope(Dispatchers.IO).launch {
            // The camera's -X rotation is its yaw rotation
            Log.e("FLOOR", "Try to save floor (${floorName})")
            database.insertFloor(detectedFloor, lastFrame!!.camera.pose.position, -lastFrame!!.camera.pose.rotation.x, floorName)

            setPaused(false)
        }
    }

    /** Retrieves a floor from the database, for placing. */
    fun loadFloor(id: Int) {
        if (useFloorHeight) {
            setPaused(true)

            placementNode?.let {
                it.detachFromScene(sceneView)
                it.parent = null
                placementNode = null
            }

            CoroutineScope(Dispatchers.IO).launch {
                val floorData = database.getFloorByID(id)
                if (floorData != null) {
                    val floor = floorData.first
                    val cameraPosition = convertPosToIndexes(lastFrame!!.camera.pose.position)
                    val floorPosition = Int3(cameraPosition.x, floorHeight, cameraPosition.z)

                    val parentNode = renderer.createEmptyNode(
                        convertIndexesToCellCenter(floorPosition),
                        // Nodes use their Y rotation for yaw
                        Rotation(0f, floorData.second, 0f)
                    )

                    for (x in floor.grid) {
                        for (z in x.value) {
                            val pointPos = convertIndexesToCellCenter(Int3(x.key, 0, z.key))
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
                    Log.e("ARTracking", "Failed to Find Floor")
                    val ids = database.getFloorIDs()
                    for (i in ids) {
                        Log.d("DefaultARTracking", "Floor ID: ${i}")
                    }
                }

                setPaused(false)
            }
        }
    }

    /** Gets the point on the floor/wall that the device is looking at. */
    internal fun getLookPoint(showCellCursor: Boolean, useOnWall: Boolean): Position? {
        cellCrosshair?.isVisible = showCellCursor

        val camPose = lastFrame?.camera?.pose
        if (camPose != null) {
            val cameraPos = camPose.position
            // -Z is forwards
            val lookDir = camPose.rotateVector(Float3(0f, 0f, -1f).toFloatArray()).toFloat3()

            val hitPoint = MathUtils.rayFloorIntersection(cameraPos, lookDir, convertIndexToCellCenter(floorHeight))

            if (hitPoint != null) {
                var hitCell: Int3 = convertPosToIndexes(hitPoint)

                if (findFirstConfirmedPointInCell(hitCell) != null) {
                    if (showCellCursor) {
                        cellCrosshair?.position = convertIndexesToCellCenter(hitCell)
                    }
                    return hitPoint
                }
                else {
                    // Bresenham's line algorithm between hitPoint and camPos, to find the furthest valid cell in the look direction
                    // Advances from 1 point to another, stopping at every intersecting cell in a grid (Axis-aligned, with 1x1 cells)
                    val x1 = hitPoint.x * DICT_COORD_ZOOM
                    val z1 = hitPoint.z * DICT_COORD_ZOOM
                    val x2 = cameraPos.x * DICT_COORD_ZOOM
                    val z2 = cameraPos.z * DICT_COORD_ZOOM

                    val coords = MathUtils.bresenhamLine(x1, z1, x2, z2) { x: Float, z: Float ->
                        // Check if the current position is a valid cell
                        (points.get(floor(x).toInt())?.get(floorHeight)?.get(floor(z).toInt())?.size?: -1) > 0
                        && findFirstConfirmedPointInCell(Int3(
                            floor(x).toInt(),
                            floorHeight,
                            floor(z).toInt()
                        )) != null
                    }

                    if (coords != null) {
                        // hitCell is the furthest valid cell in the look direction
                        hitCell = Int3(floor(coords.x).toInt(), floorHeight, floor(coords.y).toInt())
                        if (showCellCursor) {
                            cellCrosshair?.position = convertIndexesToCellCenter(hitCell)
                        }

                        if (findFirstConfirmedPointInCell(hitCell) != null) {
                            val flatLookDir = Vector3(lookDir.x, 0f, lookDir.z).normalized()
                            val edgeDistance = 0.5f / max(abs(flatLookDir.x), abs(flatLookDir.z))

                            // floorHitPoint is the furthest point on hitCell in the look direction
                            val floorHitPoint: Position = convertIndexesToCellCenter(hitCell) + flatLookDir.scaled(edgeDistance).toFloat3() * DICT_COORD_UNZOOM

                            if (!useOnWall) {
                                return floorHitPoint
                            }
                            else {
                                // Get the intersection between the look ray and the imaginary wall rising from floorHitPoint
                                val distance = floorHitPoint - cameraPos
                                val ray = (hitPoint - cameraPos)
                                val denominator = if (distance.x > distance.y)
                                    ray.x / distance.x
                                else
                                    ray.z / distance.z

                                val wallHitPoint: Position = cameraPos + ray / denominator
                                return wallHitPoint
                            }
                        }
                    }
                }
            }
        }
        cellCrosshair?.let {
            it.position = Position()
            it.isVisible = false
        }
        return null
    }

    /** Renders a model from Firebase in the direction the device is facing. */
    internal open fun renderModel(modelCategory: String, modelName: String, scale: Float, allowWalls: Boolean, afterPlacement: (Node) -> Unit) {
        val lookPoint = getLookPoint(true, allowWalls)
        if (lookPoint != null) {
            renderer.renderFromFirebase(
                modelCategory,
                modelName,
                lookPoint,
                Rotation(),
                Scale(scale),
                { node ->
                    startPlacement(node, allowWalls, afterPlacement)
                },
                {
                    Log.e("Firebase", "Failed to render model from Firebase")
                }
            )
        }
    }

    /** Sets a node to be moved around with the camera for placing. */
    internal fun startPlacement(nodeToPlace: Node, allowWalls: Boolean, afterPlacement: (Node) -> Unit) {
        placementNode = nodeToPlace
        onPlacement = afterPlacement
        updatePlacementPos = { node ->
            val updatedPos = getLookPoint(true, allowWalls)
            if (updatedPos != null) {
                node.position = updatedPos + Position(0f, 0.05f, 0f)
            }
            val camPose = lastFrame?.camera?.pose
            if (camPose != null) {
                // The camera's -X rotation is its yaw rotation, unlike nodes which use their Y rotation for yaw
                node.rotation = Rotation(0f, -camPose.rotation.x, 0f)
            }
        }
        placementScreenLayout()
    }

    /** Places the object that was being located. */
    internal fun place() {
        placementNode?.let {
            onPlacement?.invoke(it)
            onPlacement = null
            placementNode = null
        }
        defaultScreenLayout()
    }
}